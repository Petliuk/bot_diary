package com.example.bot_diary.pages_handler.comands.command_handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Component
public class TimePickerHandler {

    @Autowired
    private MessageService messageService;

    @Autowired
    private NewTaskCommandHandler newTaskCommandHandler;

    public SendMessage createHourPickerMessage(long chatId) {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        // Припустимо, selectedDates - це карта, де зберігаються обрані дати
        LocalDate selectedDate = newTaskCommandHandler.getSelectedDates().get(chatId);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Виберіть годину:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            String hourText = String.format("%02d", hour);
            InlineKeyboardButton hourButton = new InlineKeyboardButton();

            // Перевіряємо, чи вибрана дата є сьогоднішнім днем і чи година вже минула
            if (selectedDate != null && selectedDate.equals(today) && now.getHour() > hour) {
                hourText = "✘ " + hourText;
                hourButton.setCallbackData("PAST_HOUR");
            } else {
                hourButton.setCallbackData("HOUR_" + hour);
            }

            hourButton.setText(hourText + " год");
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
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        LocalDate selectedDate = newTaskCommandHandler.getSelectedDates().get(chatId); // Отримання обраної дати

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(String.format("Вибрано %02d год. Виберіть хвилини:", chosenHour));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        for (int minute = 0; minute < 60; minute += 5) {
            String minuteText = String.format("%02d", minute);
            InlineKeyboardButton minuteButton = new InlineKeyboardButton();

            // Додаткова перевірка для вибору хвилин
            if (selectedDate != null && !selectedDate.isAfter(today) &&
                    (chosenHour < now.getHour() || (chosenHour == now.getHour() && minute <= now.getMinute()))) {
                minuteText = "✘ " + minuteText; // Минулі хвилини позначаються знаком "✘"
                minuteButton.setCallbackData("PAST_MINUTE"); // Користувач не може вибрати минулі хвилини
            } else {
                minuteButton.setCallbackData("MINUTE_" + chosenHour + "_" + minute); // Майбутні хвилини можна вибрати
            }
            minuteButton.setText(minuteText + " хв");
            if (minute % 30 == 0) {
                keyboardRows.add(new ArrayList<>());
            }
            keyboardRows.get(minute / 30).add(minuteButton);
        }

        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;
    }
    public void handleHourSelection(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        int chosenHour = Integer.parseInt(callbackData.substring(5));
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        SendMessage minutePickerMessage = createMinutePickerMessage(chatId, chosenHour);

        messageService.sendMessage(minutePickerMessage);    // Можу бути помилка
    }

    public void handleMinuteSelection(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        String[] parts = callbackData.split("_");
        int chosenHour = Integer.parseInt(parts[1]);
        int chosenMinute = Integer.parseInt(parts[2]);
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        newTaskCommandHandler.saveTaskWithNotificationTime(chatId, chosenHour, chosenMinute);
    }

}


