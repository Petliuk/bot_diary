package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.pages_handler.comands.BotService;
import com.example.bot_diary.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompletedTasksCommandHandler {

    @Autowired
    private BotService botService;

    @Autowired
    private TaskService taskService;

    public void handle(Update update) throws TelegramApiException {
        long chatId = update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();
        List<Task> tasks = taskService.findTasksByStatusAndUserChatId(TaskStatus.COMPLETED,chatId);

        if (tasks.isEmpty()) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Виконаних задач немає.");
            botService.sendMessage(message);
        } else {
            for (Task task : tasks) {
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();

                InlineKeyboardButton revertButton = new InlineKeyboardButton();
                revertButton.setText("Повернути");
                revertButton.setCallbackData("REVERT_TASK_" + task.getId());
                rowInline.add(revertButton);

                InlineKeyboardButton deleteButton = new InlineKeyboardButton();
                deleteButton.setText("Видалити");
                deleteButton.setCallbackData("DELETE_TASK_" + task.getId());
                rowInline.add(deleteButton);

                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);

                StringBuilder response = new StringBuilder();
                response.append("ID: ").append(task.getId())
                        .append("\nОпис: ").append(task.getDescription())
                        .append("\nСтатус: ").append(task.getStatus().getDisplayName())
                        .append("\n");

                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText(response.toString());
                message.setReplyMarkup(markupInline);
                botService.sendMessage(message);
            }
        }
    }

    public void handleCallbackQuery(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        if (callbackData.startsWith("REVERT_TASK_")) {
            Long taskId = Long.parseLong(callbackData.split("_")[2]);
            taskService.revertTask(taskId);
            String response = "Задача з ID " + taskId + " була повернута у список невиконаних.";
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
            message.setText(response);
            botService.sendMessage(message);
        } else if (callbackData.startsWith("DONE_TASK_")) {
            Long taskId = Long.parseLong(callbackData.split("_")[2]);
            markTaskAsCompleted(taskId);
            String response = "Задача з ID " + taskId + " була відмічена як виконана.";
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
            message.setText(response);
            botService.sendMessage(message);
        }
    }

    private void markTaskAsCompleted(Long taskId) {
        Task task = taskService.findTaskById(taskId);
        if (task != null) {
            task.setStatus(TaskStatus.COMPLETED);
            taskService.saveTask(task);
        }
    }
}