package com.example.bot_diary.pages_handler.comands.command_handler;
import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.UserState;
import com.example.bot_diary.service.TaskService;
import com.example.bot_diary.utilities.CalendarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

import static com.example.bot_diary.utilities.UserButtons.createButton;

@Component
public class DateTimeUpdateHandler {

        @Autowired
        private TaskService taskService;

        @Autowired
        private MessageService messageService;

        @Autowired
        private  NewTaskCommandHandler newTaskCommandHandler;

        private final Map<Long, LocalDate> selectedDates = new HashMap<>();
        private final Map<Long, Integer> selectedHours = new HashMap<>();
        private final Map<Long, Long> taskIds = new HashMap<>();

        public void initiateDateTimeUpdate(CallbackQuery callbackQuery, long taskId) throws TelegramApiException {
            Long chatId = callbackQuery.getMessage().getChatId();
            taskIds.put(chatId, taskId);
            newTaskCommandHandler.getUserStates().put(chatId, UserState.DATETIME_UPDATE); // Змінюємо стан користувача на DATETIME_UPDATE
            promptForDateSelection(chatId);
        }

        private void promptForDateSelection(long chatId) throws TelegramApiException {
            messageService.sendMessage(generateCalendarMessage(chatId, YearMonth.now()));
        }

        public void handleDateSelection(Update update, int dayOfMonth) throws TelegramApiException {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            YearMonth currentMonth = YearMonth.now(); // update this if the month can be changed
            LocalDate selectedDate = currentMonth.atDay(dayOfMonth);
            selectedDates.put(chatId, selectedDate);
            showHourSelection(chatId);
        }

        public void showHourSelection(long chatId) throws TelegramApiException {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Виберіть годину:");

            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            for (int hour = 0; hour < 24; hour++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.format("%02d:00", hour));
                button.setCallbackData("SELECT_HOURS_" + hour);

                int rowIndex = hour / 6;
                while (rows.size() <= rowIndex) {
                    rows.add(new ArrayList<>());
                }
                rows.get(rowIndex).add(button);
            }

            inlineKeyboard.setKeyboard(rows);
            message.setReplyMarkup(inlineKeyboard);
            messageService.sendMessage(message);
        }

        public void handleHourSelection(Update update, int hour) throws TelegramApiException {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            selectedHours.put(chatId, hour);
            showMinuteSelection(chatId, hour);
        }

        public void showMinuteSelection(long chatId, int hour) throws TelegramApiException {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(String.format("Вибрано %02d годин. Виберіть хвилини:", hour));

            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            for (int minute = 0; minute < 60; minute += 5) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.format("%02d", minute));
                button.setCallbackData("SELECT_MINUTES_" + hour + "_" + minute);
                int rowIndex = minute / 15;
                while (rows.size() <= rowIndex) {
                    rows.add(new ArrayList<>());
                }
                rows.get(rowIndex).add(button);
            }

            inlineKeyboard.setKeyboard(rows);
            message.setReplyMarkup(inlineKeyboard);
            messageService.sendMessage(message);
        }

        public void handleMinuteSelection(Update update, int hour, int minute) throws TelegramApiException {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            LocalDate selectedDate = selectedDates.get(chatId);
            LocalTime selectedTime = LocalTime.of(hour, minute);
            LocalDateTime selectedDateTime = LocalDateTime.of(selectedDate, selectedTime);

            updateTaskDateTime(chatId, selectedDateTime);
        }

        private void updateTaskDateTime(long chatId, LocalDateTime newDateTime) throws TelegramApiException {
            Long taskId = taskIds.get(chatId);
            Task task = taskService.findTaskById(taskId);

            if (task != null) {
                task.setDueDate(newDateTime);
                taskService.saveTask(task);
                messageService.sendMessage(chatId, "Дата і час задачі оновлені.");
            } else {
                messageService.sendMessage(chatId, "Задача не знайдена.");
            }
        }

        public SendMessage generateCalendarMessage(long chatId, YearMonth currentMonth) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));

            String text = currentMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("uk", "UA")) + " " + currentMonth.getYear();
            message.setText(text);

            LocalDate today = LocalDate.now();
            List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
            keyboardRows.add(CalendarUtils.createWeekDaysHeader());
            keyboardRows.addAll(createDaysButtons(currentMonth, today));
            keyboardRows.add(CalendarUtils.createNavigationRow(currentMonth));

            message.setReplyMarkup(CalendarUtils.createInlineKeyboardMarkup(keyboardRows));

            return message;
        }

        private List<List<InlineKeyboardButton>> createDaysButtons(YearMonth currentMonth, LocalDate today) {
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            LocalDate date = currentMonth.atDay(1);
            int lengthOfMonth = currentMonth.lengthOfMonth();
            int dayOfWeek = date.getDayOfWeek().getValue();

            List<InlineKeyboardButton> weekRow = new ArrayList<>();

            for (int i = 1; i < dayOfWeek; i++) {
                weekRow.add(CalendarUtils.createButton(" ", "empty"));
            }

            for (int dayOfMonth = 1; dayOfMonth <= lengthOfMonth; dayOfMonth++) {
                date = currentMonth.atDay(dayOfMonth);
                dayOfWeek = date.getDayOfWeek().getValue();
                String callbackData;
                String dayButtonText = String.format("%02d", dayOfMonth);

                if (date.isBefore(today)) {
                    dayButtonText = "✘" + dayButtonText;
                    callbackData = "INVALID_DATE";
                } else {
                    callbackData = "CAL_DAYS_" + dayOfMonth;
                    if (date.equals(today)) {
                        dayButtonText = "⬤ " + dayButtonText;
                    }
                }
                weekRow.add(CalendarUtils.createButton(dayButtonText, callbackData));

                if (dayOfWeek == 7 || dayOfMonth == lengthOfMonth) {
                    rows.add(weekRow);
                    weekRow = new ArrayList<>();
                }
            }
            return rows;
        }
    }
