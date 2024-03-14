package com.example.bot_diary.pages_handler.callback;

/*import com.example.bot_diary.models.Task;
import com.example.bot_diary.pages_handler.comands.BotService;
import com.example.bot_diary.pages_handler.comands.CommandHandler;
import com.example.bot_diary.service.MessageService;
import com.example.bot_diary.service.TaskService;
import com.example.bot_diary.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component*/
public class Callback {
/*    private BotService command;
    private final UserService userService;
    private final TaskService taskService;
    private final MessageService messageService;

    @Autowired
    public Callback(UserService userService, TaskService taskService, MessageService messageService, BotService command) {
        this.userService = userService;
        this.taskService = taskService;
        this.messageService = messageService;
        this.command = command;
    }

    @Autowired
    public void setCommand(@Lazy BotService command) {
        this.command = command;
    }

    static final String CREATE_TASK = "CREATE_TASK";
    static final String MY_TASKS = "MY_TASKS";
    public void sendStartCommand(long chatId) throws TelegramApiException {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton createTaskButton = new InlineKeyboardButton();
        createTaskButton.setText("Створити задачу");
        createTaskButton.setCallbackData(CREATE_TASK);

        InlineKeyboardButton myTasksButton = new InlineKeyboardButton();
        myTasksButton.setText("Мої завдання");
        myTasksButton.setCallbackData(MY_TASKS);

        rowInline.add(createTaskButton);
        rowInline.add(myTasksButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Виберіть пункт:");
        message.setReplyMarkup(markupInline);

        message.getText();
        if (!message.getText().isEmpty()) {
            command.sendMessage(message);
        } else {
            log.error("Cannot send message with empty text to chatId: " + chatId);
        }

        // Тепер використовується command замість messageService
    }
*//*    public void handleCallbackQuery(String callbackData, long chatId, long messageId) throws TelegramApiException {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId((int) messageId);

        if (callbackData.equals(CREATE_TASK)) {
            editMessageText.setText("You pressed YES");
        } else if (callbackData.equals(MY_TASKS)) {
            editMessageText.setText("You pressed NO");
        }

        command.editMessage(editMessageText);
    }*//*
   public void handleCallbackQuery(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        switch (callbackData) {
            case CREATE_TASK:
                SendMessage createTaskMessage = new SendMessage();
                createTaskMessage.setChatId(String.valueOf(chatId));
                createTaskMessage.setText("Напишіть нову задачу:");
                command.sendMessage(createTaskMessage);
                break;
            case MY_TASKS:
                sendAllTasks(chatId);
                break;
            // ...
        }
    }
    public enum UserState {
        NONE,
        AWAITING_TASK_DESCRIPTION
    }

    private final Map<Long, CommandHandler.UserState> userStates = new HashMap<>();

    private void sendAllTasks(Long chatId) throws TelegramApiException {
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

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(response.toString());
        message.getText();
        if (!message.getText().isEmpty()) {
            command.sendMessage(message);
        } else {
            log.error("Cannot send message with empty text to chatId: " + chatId);
        }// Correctly use the sendMessage from BotService
    }*/
}
