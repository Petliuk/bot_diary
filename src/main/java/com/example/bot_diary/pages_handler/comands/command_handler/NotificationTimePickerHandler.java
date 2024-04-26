package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.job.NotificationScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NotificationTimePickerHandler {

    @Autowired
    private MessageService messageService;

    private final Map<Long, Integer> notificationHours = new HashMap<>();
    private final Map<Long, Integer> notificationMinutes = new HashMap<>();

    @Autowired
    private NewTaskCommandHandler newTaskCommandHandler;
    public SendMessage createHourPickerMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Виберіть за скільки годин вам прийде сповіщення:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.format("%02d:00", hour));
            button.setCallbackData("HOURS_" + hour);
            if (hour % 6 == 0) rows.add(new ArrayList<>());
            rows.get(hour / 6).add(button);
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }

    public SendMessage createMinutePickerMessage(long chatId, int chosenHour) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(String.format("Вибрано %02d годин. Виберіть хвилини:", chosenHour));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int minute = 0; minute < 60; minute += 5) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.format("%02d хв", minute));
            button.setCallbackData("MINUTES_" + chosenHour + "_" + minute);
            if (minute % 30 == 0) rows.add(new ArrayList<>());
            rows.get(minute / 30).add(button);
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }
    public void handleHourSelection(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        int chosenHour = Integer.parseInt(callbackData.substring(6));
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        notificationHours.put(chatId, chosenHour);  // Store chosen hours

        SendMessage minutePickerMessage = createMinutePickerMessage(chatId, chosenHour);
        messageService.sendMessage(minutePickerMessage);
    }

    public void handleMinuteSelection(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String[] parts = callbackData.split("_");
        int chosenHour = Integer.parseInt(parts[1]);
        int chosenMinute = Integer.parseInt(parts[2]);

        // Зберігання вибраних хвилин, реальне оновлення задачі відкладається
        notificationMinutes.put(chatId, chosenMinute);

        LocalDate date = newTaskCommandHandler.getSelectedDates().get(chatId);
        if (date == null) {
            messageService.sendMessage(chatId, "Дата не була встановлена.");
            return;
        }

        // Оновлення методу на updateNotificationTime
        newTaskCommandHandler.updateNotificationTime(chatId, date, chosenHour, chosenMinute);
    }
}