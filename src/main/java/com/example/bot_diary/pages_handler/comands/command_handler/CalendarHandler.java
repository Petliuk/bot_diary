package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Component
@RequiredArgsConstructor
public class CalendarHandler {

    private final TaskService taskService;
    private final MessageService messageService;

    public final Map<Long, YearMonth> selectedYearMonths = new HashMap<>();


    public void handleCalendarCommand(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        YearMonth currentMonth = YearMonth.now();
        SendMessage calendarMessage = generateCalendarMessage(chatId, currentMonth);
        messageService.sendMessage(calendarMessage);
    }

    public SendMessage generateCalendarMessage(long chatId, YearMonth currentMonth) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        String text = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.US) + " " + currentMonth.getYear();
        message.setText(text);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.add(createWeekDaysHeader());
        keyboardRows.addAll(createDaysButtons(currentMonth, chatId));
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

    private List<List<InlineKeyboardButton>> createDaysButtons(YearMonth currentMonth, long chatId) {
        List<LocalDate> datesWithTasks = taskService.findDatesWithTasks(currentMonth, chatId);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        LocalDate date = currentMonth.atDay(1);
        int lengthOfMonth = currentMonth.lengthOfMonth();
        int dayOfWeek = date.getDayOfWeek().getValue();

        List<InlineKeyboardButton> weekRow = new ArrayList<>();
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
            String dayButtonText = String.format("%02d", dayOfMonth);
            if (datesWithTasks.contains(date)) {
                dayButtonText = "✓ " + dayButtonText;
            }
            dayButton.setText(dayButtonText);
            dayButton.setCallbackData("DAY" + dayOfMonth);
            weekRow.add(dayButton);

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

    public void handleMonthChange(String callbackData, long chatId, int messageId) throws TelegramApiException {
        YearMonth selectedMonth = YearMonth.parse(callbackData.split("_")[2]);
        selectedYearMonths.put(chatId, selectedMonth);

        SendMessage newCalendarMessage = generateCalendarMessage(chatId, selectedMonth);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);
        editMessageText.setText(newCalendarMessage.getText());
        editMessageText.setReplyMarkup((InlineKeyboardMarkup) newCalendarMessage.getReplyMarkup());

        messageService.editMessage(editMessageText);
    }
}
