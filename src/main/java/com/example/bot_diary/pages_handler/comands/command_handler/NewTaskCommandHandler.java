package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.pages_handler.comands.BotService;
import com.example.bot_diary.pages_handler.comands.CommandHandler;
import com.example.bot_diary.service.MessageService;
import com.example.bot_diary.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

@Component
public class TaskCommandHandler {

    private final TaskService taskService;
    private final MessageService messageService;
    private final BotService botService;
    private final Map<Long, CommandHandler.UserState> userStates;

    @Autowired
    public TaskCommandHandler(TaskService taskService, MessageService messageService, BotService botService, Map<Long, CommandHandler.UserState> userStates) {
        this.taskService = taskService;
        this.messageService = messageService;
        this.botService = botService;
        this.userStates = userStates;
    }

    public void handle(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        switch (messageText) {
            case "/newtask":
                userStates.put(chatId, CommandHandler.UserState.AWAITING_TASK_DESCRIPTION);
                messageService.sendMessage(chatId, "Напишіть нову задачу:");
                break;
            case "/alltasks":
                List<Task> tasks = taskService.findAllTasks();
                StringBuilder response = new StringBuilder();
                if (tasks.isEmpty()) {
                    response.append("Задачі відсутні.");
                } else {
                    for (Task task : tasks) {
                        response.append("ID: ").append(task.getId())
                                .append("\nОпис: ").append(task.getDescription())
                                .append("\nСтатус: ").append(task.getStatus().getDisplayName())
                                .append("\n\n");
                    }
                }
                messageService.sendMessage(chatId, response.toString());
                break;
        }
    }
}