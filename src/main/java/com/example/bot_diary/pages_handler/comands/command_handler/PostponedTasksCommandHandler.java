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
public class PostponedTasksCommandHandler {

    @Autowired
    private BotService botService;

    @Autowired
    private TaskService taskService;

    public void handle(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        List<Task> tasks = taskService.findAllTasksByStatus(TaskStatus.POSTPONED);
        if (tasks.isEmpty()) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Відкладених задач немає.");
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

       /* if (tasks.isEmpty()) {
            response.append("Відкладених задач немає.");
        } else {
            for (Task task : tasks) {
                response.append("ID: ").append(task.getId())
                        .append("\nОпис: ").append(task.getDescription())
                        .append("\nСтатус: ").append(task.getStatus().getDisplayName())
                        .append("\n\n");
            }
        }

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(response.toString());
        botService.sendMessage(message);
    }*/

    public void handleCallbackQuery(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();

            Long taskId = Long.parseLong(callbackData.split("_")[2]);
            taskService.postponeTask(taskId);
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
            message.setText("Задача з ID " + taskId + " була відкладена.");
            botService.sendMessage(message);

    }
}
