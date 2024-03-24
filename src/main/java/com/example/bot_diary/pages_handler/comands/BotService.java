package com.example.bot_diary.pages_handler.comands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotService {

    void sendMessage(Long chatId, String text);
    void sendMessage(SendMessage message) throws TelegramApiException;
    void editMessage(EditMessageText editMessageText) throws TelegramApiException;
}
