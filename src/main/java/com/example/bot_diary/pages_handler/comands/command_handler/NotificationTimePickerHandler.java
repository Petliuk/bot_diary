package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.models.Notification;
import com.example.bot_diary.service.NotificationService;
import com.example.bot_diary.service.TaskService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.bot_diary.utilities.UserButtons.createButton;

@Component
public class NotificationTimePickerHandler {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TaskService taskService;

    public enum NotificationSetupState {
        SELECT_DATE,
        SELECT_HOUR,
        SELECT_MINUTE
    }
    @Autowired
    private MessageService messageService;

    private final Map<Long, LocalDate> selectedDates = new HashMap<>();
    private final Map<Long, Integer> notificationHours = new HashMap<>();
    private final Map<Long, NotificationSetupState> userStates = new HashMap<>();
    private final Map<Long, Long> taskIds = new HashMap<>();
    public final Map<Long, YearMonth> selectedYearMonths = new HashMap<>();
    public void handleCalendarDaySelection(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        try {
            int dayOfMonth = safelyParseInteger(callbackData.substring("CAL_DAYS_".length()));
            handleDateSelection(update, dayOfMonth);
        } catch (NumberFormatException e) {
            System.err.println("Invalid day format: " + callbackData);
        }
    }

    public void handleTaskNotificationInitiation(String callbackData, Update update) throws TelegramApiException {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long taskId = Long.parseLong(callbackData.substring("ADD_NOTIFICATION_TASK_".length()));
        initiateNotificationSetup(update.getCallbackQuery(), taskId);
    }

    public void handleHourSelection(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        try {
            int hour = Integer.parseInt(callbackData.substring("SELECT_HOURS_".length()));
            handleHourSelection(update, hour);
        } catch (NumberFormatException e) {
            System.err.println("Некоректне значення години: " + callbackData);
        }
    }

    public void handleMinuteSelection(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        String[] parts = callbackData.split("_");
        try {
            int hour = Integer.parseInt(parts[2]);
            int minute = Integer.parseInt(parts[3]);
            handleMinuteSelection(update, hour, minute);
        } catch (NumberFormatException e) {
            System.err.println("Некоректне значення для годин або хвилин: " + callbackData);
        }
    }
    private Integer safelyParseInteger(String data) {
        Matcher matcher = Pattern.compile("\\d+").matcher(data);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        } else {
            throw new NumberFormatException("No digits found in the string: " + data);
        }
    }

    public void handleAddNotification(Update update) throws TelegramApiException {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long taskId = getTaskIdForChat(chatId);
        if (taskId != null) {
            initiateNotificationSetup(update.getCallbackQuery(), taskId);
        } else {
            messageService.sendMessage(chatId, "Не вдалося знайти завдання для створення сповіщення.");
        }
    }
    public void initiateNotificationSetup(CallbackQuery callbackQuery, long taskId) throws TelegramApiException {
        Long chatId = callbackQuery.getMessage().getChatId();
        taskIds.put(chatId, taskId);
        userStates.put(chatId, NotificationSetupState.SELECT_DATE);
        promptForNotificationDate(callbackQuery);
    }

    public Long getTaskIdForChat(Long chatId) {
        return taskIds.get(chatId);
    }
    private void promptForNotificationDate(CallbackQuery callbackQuery) throws TelegramApiException {
        Long chatId = callbackQuery.getMessage().getChatId();
        messageService.sendMessage(generateCalendarMessage(chatId, YearMonth.now()));
    }

    public void handleDateSelection(Update update, int dayOfMonth) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        NotificationSetupState state = userStates.getOrDefault(chatId, null);

        if (state != NotificationSetupState.SELECT_DATE) {
            messageService.sendMessage(chatId, "Кнопки доступні лише під час створення сповіщення.");
            return;
        }

        String callbackData = update.getCallbackQuery().getData();
        if (callbackData.startsWith("INVALID_DATE")) {
            messageService.sendMessage(chatId, "Цей день не можна вибрати для сповіщення. Оберіть дату, яка є до кінцевого терміну задачі.");
            return;
        }

        YearMonth currentMonth = selectedYearMonths.getOrDefault(chatId, YearMonth.now());
        LocalDate selectedDate = currentMonth.atDay(dayOfMonth);
        selectedDates.put(chatId, selectedDate);
        userStates.put(chatId, NotificationSetupState.SELECT_HOUR);
        showHourSelection(chatId);
    }



    public SendMessage generateCalendarMessage(long chatId, YearMonth currentMonth) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        String text = currentMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("uk", "UA")) + " " + currentMonth.getYear();
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

    private List<List<InlineKeyboardButton>> createDaysButtons(YearMonth currentMonth, long chatId) {
        LocalDate today = LocalDate.now();
        LocalDate taskDeadline = taskService.findTaskById(taskIds.get(chatId)).getDueDate().toLocalDate();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        LocalDate date = currentMonth.atDay(1);
        int lengthOfMonth = currentMonth.lengthOfMonth();
        int dayOfWeek = date.getDayOfWeek().getValue();

        List<InlineKeyboardButton> weekRow = new ArrayList<>();

        for (int i = 1; i < dayOfWeek; i++) {
            weekRow.add(createButton(" ", "empty"));
        }

        for (int dayOfMonth = 1; dayOfMonth <= lengthOfMonth; dayOfMonth++) {
            date = currentMonth.atDay(dayOfMonth);
            dayOfWeek = date.getDayOfWeek().getValue();
            String callbackData;
            String dayButtonText = String.format("%02d", dayOfMonth);

            if (date.isBefore(today) || date.isAfter(taskDeadline)) {
                dayButtonText = "✘" + dayButtonText;
                callbackData = "INVALID_DATE";
            } else {
                callbackData = "CAL_DAYS_" + dayOfMonth;
                if (date.equals(today)) {
                    dayButtonText = "⬤ " + dayButtonText;
                }
            }
            weekRow.add(createButton(dayButtonText, callbackData));

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
        previousMonthButton.setCallbackData("PREVIOUS_MONTHS_" + currentMonth.minusMonths(1).toString());

        InlineKeyboardButton currentMonthButton = new InlineKeyboardButton();
        currentMonthButton.setText("Current Month");
        currentMonthButton.setCallbackData("CURRENT_MONTH");

        InlineKeyboardButton nextMonthButton = new InlineKeyboardButton();
        nextMonthButton.setText(">>>");
        nextMonthButton.setCallbackData("NEXT_MONTHS_" + currentMonth.plusMonths(1).toString());

        List<InlineKeyboardButton> navigationRow = new ArrayList<>();
        navigationRow.add(previousMonthButton);
        navigationRow.add(currentMonthButton);
        navigationRow.add(nextMonthButton);

        return navigationRow;
    }

    public void showHourSelection(long chatId) throws TelegramApiException {
        LocalDateTime dueDateTime = taskService.findTaskById(taskIds.get(chatId)).getDueDate();
        LocalDate selectedDate = selectedDates.get(chatId);
        LocalTime now = LocalTime.now();

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Виберіть годину:");

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            if ((selectedDate.equals(LocalDate.now()) && hour < now.getHour()) ||
                    (selectedDate.equals(dueDateTime.toLocalDate()) && hour > dueDateTime.toLocalTime().getHour())) {
                continue;
            }
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
        messageService.sendMessage(message);;
    }

    public void handleHourSelection(Update update, int hour) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (userStates.getOrDefault(chatId, null) != NotificationSetupState.SELECT_HOUR) {
            messageService.sendMessage(chatId, "Кнопки доступні лише під час створення сповіщення.");
            return;
        }

        notificationHours.put(chatId, hour);
        userStates.put(chatId, NotificationSetupState.SELECT_MINUTE);
        showMinuteSelection(chatId, hour);
    }

    public void showMinuteSelection(long chatId, int hour) throws TelegramApiException {
        LocalDateTime dueDateTime = taskService.findTaskById(taskIds.get(chatId)).getDueDate();
        LocalDate dueDate = dueDateTime.toLocalDate();
        LocalTime dueTime = dueDateTime.toLocalTime();

        LocalDate selectedDate = selectedDates.get(chatId);
        LocalTime now = LocalTime.now();

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(String.format("Вибрано %02d годин. Виберіть хвилини:", hour));

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int minute = 0; minute < 60; minute += 5) {
            if ((selectedDate.equals(LocalDate.now()) && hour == now.getHour() && minute < now.getMinute()) ||
                    (selectedDate.equals(dueDate) && hour == dueTime.getHour() && minute > dueTime.getMinute())) {
                continue;
            }

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.format("%02d", minute));
            button.setCallbackData("SELECT_MINUTES_" + hour + "_" + minute);
            int rowIndex = minute / 30;
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
        if (userStates.getOrDefault(chatId, null) != NotificationSetupState.SELECT_MINUTE) {
            messageService.sendMessage(chatId, "Кнопки доступні лише під час створення сповіщення.");
            return;
        }

        LocalDate selectedDate = selectedDates.get(chatId);
        LocalDateTime dueDateTime = taskService.findTaskById(taskIds.get(chatId)).getDueDate();

        if (selectedDate.equals(dueDateTime.toLocalDate()) && LocalTime.of(hour, minute).isAfter(dueDateTime.toLocalTime()) ||
                selectedDate.equals(LocalDate.now()) && LocalTime.of(hour, minute).isBefore(LocalTime.now())) {
            messageService.sendMessage(chatId, "Недопустимий час для сповіщення.");
            return;
        }

        LocalDateTime notificationDateTime = LocalDateTime.of(selectedDate, LocalTime.of(hour, minute));
        Notification notification = new Notification();
        notification.setTask(taskService.findTaskById(taskIds.get(chatId)));
        notification.setNotificationTime(notificationDateTime);
        notificationService.saveAndScheduleNotification(notification);

        messageService.sendMessage(chatId, String.format("Сповіщення заплановано на %s о %02d:%02d", selectedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), hour, minute));
        /*userStates.remove(chatId);*/
        sendRepeatNotificationQuery(chatId);
    }
    private void sendRepeatNotificationQuery(Long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Бажаєте додати нове сповіщення?");
        message.setReplyMarkup(createInlineKeyboardForNewNotification());
        messageService.sendMessage(message);
    }

    private InlineKeyboardMarkup createInlineKeyboardForNewNotification() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton createButton = new InlineKeyboardButton();
        createButton.setText("Створити сповіщення");
        createButton.setCallbackData("ADD_NOTIFICATION");

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("Ні");
        noButton.setCallbackData("CANCEL_NOTIFICATION");

        rowInline.add(createButton);
        rowInline.add(noButton);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(rowInline);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
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