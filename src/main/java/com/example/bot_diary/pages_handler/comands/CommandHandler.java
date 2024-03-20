package com.example.bot_diary.pages_handler.comands;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CommandHandler {
    void handle(Update update) throws TelegramApiException;

}
