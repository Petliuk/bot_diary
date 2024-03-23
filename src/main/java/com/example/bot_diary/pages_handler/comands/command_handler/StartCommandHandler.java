package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.pages_handler.comands.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
//@RequiredArgsConstructor
public class StartCommandHandler{

    public static final String CREATE_TASK ="CREATE_TASK";
    static final String MY_TASKS = "MY_TASKS";

    private final MessageService messageService;

    public StartCommandHandler(@Lazy MessageService messageService) {
        this.messageService = messageService;
    }


    public void handle(Update update) throws TelegramApiException {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        long chatId = update.getMessage().getChatId();

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

        messageService.sendMessage(message);
    }
}