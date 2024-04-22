package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.service.TaskService;
import com.example.bot_diary.utilities.UserButtons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CompletedTasksCommandHandler {

    private final MessageService messageService;
    private final TaskService taskService;

    public void handle(Update update) throws TelegramApiException {
        long chatId = update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();
        List<Task> tasks = taskService.findTasksByStatusAndUserChatId(TaskStatus.COMPLETED, chatId);

        if (!tasks.isEmpty()) {
            UserButtons.sendTasksWithCustomButtons(tasks, chatId, messageService, task -> List.of(
                    UserButtons.createButton("Повернути", "REVERT_TASK_" + task.getId()),
                    UserButtons.createButton("Видалити", "DELETE_TASK_" + task.getId())
            ));
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Виконаних задач немає.");
            messageService.sendMessage(message);
        }
    }

    public void handleCallbackQuery(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        if (callbackData.startsWith("REVERT_TASK_")) {
            Long taskId = Long.parseLong(callbackData.split("_")[2]);
            Task task = taskService.findTaskById(taskId);
            if (task != null) {
                taskService.revertTask(taskId);
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
                message.setText("Задача з ID " + taskId + " була повернута у список невиконаних.");
                messageService.sendMessage(message);
            } else {
                messageService.sendMessage(update.getCallbackQuery().getMessage().getChatId(), "Ця задача вже не існує.");
            }
        } else if (callbackData.startsWith("DONE_TASK_")) {
            Long taskId = Long.parseLong(callbackData.split("_")[2]);
            markTaskAsCompleted(taskId, update);
        }
    }

    private void markTaskAsCompleted(Long taskId, Update update) throws TelegramApiException {
        Task task = taskService.findTaskById(taskId);
        if (task != null) {
            task.setStatus(TaskStatus.COMPLETED);
            taskService.saveTask(task);
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
            message.setText("Задача з ID " + taskId + " була відмічена як виконана.");
            messageService.sendMessage(message);
        } else {
            messageService.sendMessage(update.getCallbackQuery().getMessage().getChatId(), "Ця задача вже не існує.");
        }
    }

}