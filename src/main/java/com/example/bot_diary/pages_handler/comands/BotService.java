package com.example.bot_diary.pages_handler.comands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface Command {

    void sendMessage(Long chatId, String text); // Відправлення простого текстового повідомлення
    void sendMessage(SendMessage message) throws TelegramApiException; // Відправлення повідомлення з використанням об'єкта SendMessage
    void editMessage(EditMessageText editMessageText) throws TelegramApiException;
}
