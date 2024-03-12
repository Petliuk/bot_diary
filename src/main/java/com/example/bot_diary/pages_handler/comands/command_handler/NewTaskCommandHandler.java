package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.models.User;
import com.example.bot_diary.service.MessageService;
import com.example.bot_diary.service.TaskService;
import com.example.bot_diary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

@Component
public class NewTaskCommandHandler {
    public enum UserState {
        NONE,
        AWAITING_TASK_DESCRIPTION
    }

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private MessageService messageService;

    private final Map<Long, UserState> userStates = new HashMap<>();

    public Map<Long, UserState> getUserStates() {
        return userStates;
    }

    public void initiateNewTaskCreation(long chatId) {
        userStates.put(chatId, UserState.AWAITING_TASK_DESCRIPTION);
        messageService.sendMessage(chatId, "Напишіть опис нової задачі:");
    }

    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        UserState currentState = userStates.getOrDefault(chatId, UserState.NONE);

        if (currentState != UserState.AWAITING_TASK_DESCRIPTION) {
            // Якщо ні, ініціюємо створення нової задачі
            initiateNewTaskCreation(chatId);
        } else {
            // Якщо так, продовжуємо зі створенням задачі
            createTask(update);
        }
      /*  if (currentState == UserState.AWAITING_TASK_DESCRIPTION) {
            createTask(update);
        }*/
    }

    private void createTask(Update update) {
        long chatId = update.getMessage().getChatId();
        User user = userService.findOrCreateUser(chatId);
        Task task = new Task();
        task.setDescription(update.getMessage().getText());
        task.setUser(user);
        task.setStatus(TaskStatus.NOT_COMPLETED);
        taskService.saveTask(task);
        userStates.put(chatId, UserState.NONE);
        messageService.sendMessage(chatId, "Ваша задача збережена.");
    }

}