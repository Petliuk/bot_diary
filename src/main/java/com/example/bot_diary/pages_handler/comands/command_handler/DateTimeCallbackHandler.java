package com.example.bot_diary.pages_handler.comands.command_handler;


import com.example.bot_diary.models.UserState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class DateTimeCallbackHandler {

    @Autowired
    private DateTimeUpdateHandler dateTimeUpdateHandler;

    @Autowired
    private NotificationTimePickerHandler notificationTimePickerHandler;


    public void handleCalendarDays(Update update, UserState currentState) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        if (currentState == UserState.DATETIME_UPDATE) {
            if (callbackData.startsWith("CAL_DAYS_") && callbackData.length() > "CAL_DAYS_".length()) {
                dateTimeUpdateHandler.handleDateSelection(update, Integer.parseInt(callbackData.substring("CAL_DAYS_".length())));
            } else {
                System.err.println("Invalid callback data length for CAL_DAYS: " + callbackData);
            }
        } else {
            notificationTimePickerHandler.handleCalendarDaySelection(update);
        }
    }

    public void handleHourSelection(Update update, UserState currentState) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        String[] parts = callbackData.split("_");
        if (currentState == UserState.DATETIME_UPDATE) {
            if (parts.length > 2) {
                dateTimeUpdateHandler.handleHourSelection(update, Integer.parseInt(parts[2]));
            } else {
                System.err.println("Invalid callback data format for SELECT_HOURS: " + callbackData);
            }
        } else {
            notificationTimePickerHandler.handleHourSelection(update);
        }
    }

    public void handleMinuteSelection(Update update, UserState currentState) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        String[] parts = callbackData.split("_");
        if (currentState == UserState.DATETIME_UPDATE) {
            if (parts.length > 3) {
                dateTimeUpdateHandler.handleMinuteSelection(update, Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            } else {
                System.err.println("Invalid callback data format for SELECT_MINUTES: " + callbackData);
            }
        } else {
            notificationTimePickerHandler.handleMinuteSelection(update);
        }
    }

}