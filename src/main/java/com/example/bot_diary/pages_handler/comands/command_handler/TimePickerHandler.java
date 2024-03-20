package com.example.bot_diary.pages_handler.comands.command_handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


@Component
public class TimePickerHandler {
    // Спочатку вибір години
    public SendMessage createHourPickerMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Виберіть годину:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            InlineKeyboardButton hourButton = new InlineKeyboardButton();
            hourButton.setText(String.format("%02d", hour) + " год");
            hourButton.setCallbackData("HOUR_" + hour);
            if (hour % 6 == 0) {
                keyboardRows.add(new ArrayList<>());
            }
            keyboardRows.get(hour / 6).add(hourButton);
        }

        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;
    }

    public SendMessage createMinutePickerMessage(long chatId, int chosenHour) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(String.format("Вибрано %02d год. Виберіть хвилини:", chosenHour));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        for (int minute = 0; minute < 60; minute += 5) {
            InlineKeyboardButton minuteButton = new InlineKeyboardButton();
            minuteButton.setText(String.format("%02d", minute) + " хв");
            minuteButton.setCallbackData("MINUTE_" + chosenHour + "_" + minute);
            if (minute % 30 == 0) {
                keyboardRows.add(new ArrayList<>());
            }
            keyboardRows.get(minute / 30).add(minuteButton);
        }

        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;
    }

}


