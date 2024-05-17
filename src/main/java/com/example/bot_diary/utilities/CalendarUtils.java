package com.example.bot_diary.utilities;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class CalendarUtils {

        public static List<InlineKeyboardButton> createWeekDaysHeader() {
            String[] weekDays = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд"};
            List<InlineKeyboardButton> weekDaysRow = new ArrayList<>();
            for (String day : weekDays) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(day);
                button.setCallbackData("dayHeader");
                weekDaysRow.add(button);
            }
            return weekDaysRow;
        }

        public static InlineKeyboardButton createButton(String text, String callbackData) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(text);
            button.setCallbackData(callbackData);
            return button;
        }

        public static List<InlineKeyboardButton> createNavigationRow(YearMonth currentMonth) {
            InlineKeyboardButton previousMonthButton = new InlineKeyboardButton();
            previousMonthButton.setText("<<<");
            previousMonthButton.setCallbackData("PREVIOUS_MONTH_" + currentMonth.minusMonths(1).toString());

            InlineKeyboardButton currentMonthButton = new InlineKeyboardButton();
            currentMonthButton.setText(currentMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.US) + " " + currentMonth.getYear());
            currentMonthButton.setCallbackData("IGNORE");

            InlineKeyboardButton nextMonthButton = new InlineKeyboardButton();
            nextMonthButton.setText(">>>");
            nextMonthButton.setCallbackData("NEXT_MONTH_" + currentMonth.plusMonths(1).toString());

            List<InlineKeyboardButton> navigationRow = new ArrayList<>();
            navigationRow.add(previousMonthButton);
            navigationRow.add(currentMonthButton);
            navigationRow.add(nextMonthButton);

            return navigationRow;
        }

        public static InlineKeyboardMarkup createInlineKeyboardMarkup(List<List<InlineKeyboardButton>> keyboardRows) {
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(keyboardRows);
            return inlineKeyboardMarkup;
        }
    }
