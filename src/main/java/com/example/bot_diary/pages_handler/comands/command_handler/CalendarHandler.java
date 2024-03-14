package com.example.bot_diary.pages_handler.comands.command_handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class CalendarHandler {

    public SendMessage generateCalendarMessage(long chatId, YearMonth currentMonth) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        String text = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.US) + " " + currentMonth.getYear();
        message.setText(text);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.add(createWeekDaysHeader());

        keyboardRows.addAll(createDaysButtons(currentMonth));

        keyboardRows.add(createNavigationRow(currentMonth));

        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;
    }

    private List<InlineKeyboardButton> createWeekDaysHeader() {
        String[] weekDays = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
        List<InlineKeyboardButton> weekDaysRow = new ArrayList<>();
        for (String day : weekDays) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(day);
            button.setCallbackData("dayHeader");
            weekDaysRow.add(button);
        }
        return weekDaysRow;
    }

    private List<List<InlineKeyboardButton>> createDaysButtons(YearMonth currentMonth) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        LocalDate date = currentMonth.atDay(1);
        int lengthOfMonth = currentMonth.lengthOfMonth();
        // Use getDayOfWeek().getValue() directly
        int dayOfWeek = date.getDayOfWeek().getValue();

        List<InlineKeyboardButton> weekRow = new ArrayList<>();
        // Padding for days of the week
        // Subtract 1 because in Java, Monday is 1 and Sunday is 7
        for (int i = 1; i < dayOfWeek; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(" ");
            button.setCallbackData("empty");
            weekRow.add(button);
        }

        for (int dayOfMonth = 1; dayOfMonth <= lengthOfMonth; dayOfMonth++) {
            date = currentMonth.atDay(dayOfMonth);
            dayOfWeek = date.getDayOfWeek().getValue();

            InlineKeyboardButton dayButton = new InlineKeyboardButton();
            dayButton.setText(String.format("%02d", dayOfMonth));
            dayButton.setCallbackData("DAY" + dayOfMonth);
            weekRow.add(dayButton);

            // When dayOfWeek is 7 (Sunday), start a new row
            if (dayOfWeek == 7 || dayOfMonth == lengthOfMonth) {
                rows.add(weekRow);
                weekRow = new ArrayList<>();
            }
        }

        return rows;
    }
    private List<InlineKeyboardButton> createNavigationRow(YearMonth currentMonth) {
        InlineKeyboardButton previousMonthButton = new InlineKeyboardButton();
        previousMonthButton.setText("<<<");
        // Use the previous month for the callback data
        previousMonthButton.setCallbackData("PREVIOUS_MONTH_" + currentMonth.minusMonths(1).toString());

        InlineKeyboardButton currentMonthButton = new InlineKeyboardButton();
        currentMonthButton.setText(currentMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.US) + " " + currentMonth.getYear());
        currentMonthButton.setCallbackData("IGNORE");

        InlineKeyboardButton nextMonthButton = new InlineKeyboardButton();
        nextMonthButton.setText(">>>");
        // Use the next month for the callback data
        nextMonthButton.setCallbackData("NEXT_MONTH_" + currentMonth.plusMonths(1).toString());

        List<InlineKeyboardButton> navigationRow = new ArrayList<>();
        navigationRow.add(previousMonthButton);
        navigationRow.add(currentMonthButton);
        navigationRow.add(nextMonthButton);

        return navigationRow;
    }
}