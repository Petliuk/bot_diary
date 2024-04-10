package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.job.NotificationScheduler;
import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.models.User;
import com.example.bot_diary.service.TaskService;
import com.example.bot_diary.service.UserService;
import com.example.bot_diary.utilities.MessageUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class NewTaskCommandHandler {
    public enum UserState {
        NONE,
        AWAITING_TASK_DESCRIPTION,
        TASK_CREATED,
        AWAITING_NOTIFICATION_DATE,
        AWAITING_NOTIFICATION_TIME
    }

    private final Map<Long, String> taskDescriptions = new HashMap<>();
    private final Map<Long, LocalDate> selectedDates = new HashMap<>();

    @Autowired
    private UserService userService;

    @Autowired
    private TimePickerHandler timePickerHandler;

    @Autowired
    private MessageService messageService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CalendarHandler calendarHandler;

    @Autowired
    private NotificationScheduler notificationScheduler;

    private final Map<Long, UserState> userStates = new HashMap<>();
    public Map<Long, UserState> getUserStates() {
        return userStates;
    }

    public Map<Long, LocalDate> getSelectedDates() {
        return selectedDates;
    }

    public void initiateNewTaskCreation(long chatId) {
        userStates.put(chatId, UserState.AWAITING_TASK_DESCRIPTION);
        messageService.sendMessage(chatId, "Напишіть опис нової задачі:");
    }

    public void handle(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        UserState currentState = userStates.getOrDefault(chatId, UserState.NONE);

        if (currentState != UserState.AWAITING_TASK_DESCRIPTION) {
            initiateNewTaskCreation(chatId);
        } else {
            createTask(update);
        }
    }

    private void createTask(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        String taskDescription = update.getMessage().getText();
        taskDescriptions.put(chatId, taskDescription);

        String firstName = update.getMessage().getFrom().getFirstName(); // Ім'я завжди присутнє
        String lastName = update.getMessage().getFrom().getLastName(); // Прізвище може бути null
        String userName = update.getMessage().getFrom().getUserName(); // Юзернейм може бути null

        Optional<User> userOptional = userService.findUserByChatId(chatId);
        User user = userOptional.orElseGet(() -> userService.createUser(chatId, firstName, lastName, userName));

        Task task = new Task();
        task.setDescription(taskDescription);
        task.setUser(user);
        task.setStatus(TaskStatus.NOT_COMPLETED);
        taskService.saveTask(task);

        userStates.put(chatId, UserState.TASK_CREATED);
        sendOptionsAfterTaskCreation(chatId);
    }

    private void sendOptionsAfterTaskCreation(long chatId) throws TelegramApiException {
        List<List<InlineKeyboardButton>> buttons = MessageUtils.createConfirmationButtons("Так", "continue_creation", "Ні", "save_task");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(buttons);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Бажаєте обрати час?");
        message.setReplyMarkup(inlineKeyboardMarkup);
        messageService.sendMessage(message);
    }

    public void saveTaskAndNotifyUser(CallbackQuery callbackQuery) throws TelegramApiException {
        Long chatId = callbackQuery.getMessage().getChatId();
        String taskDescription = taskDescriptions.getOrDefault(chatId, "Опис не знайдено");

        Optional<User> userOptional = userService.findUserByChatId(chatId);
        User user = userOptional.orElseGet(() -> userService.createUser(chatId, null, null, null)); // Використання null для імені, прізвища та юзернейма, оскільки ми не маємо цих даних

        Task task = new Task();
        task.setDescription(taskDescription);
        task.setUser(user);
        task.setStatus(TaskStatus.NOT_COMPLETED);
        taskService.saveTask(task);

        userStates.put(chatId, UserState.NONE);
        taskDescriptions.remove(chatId);

        messageService.sendMessage(chatId, "Ваша задача збережена.");
    }

    public void promptForNotificationDate(CallbackQuery callbackQuery) throws TelegramApiException {
        Long chatId = callbackQuery.getMessage().getChatId();
        userStates.put(chatId, UserState.AWAITING_NOTIFICATION_DATE);

        SendMessage calendarMessage = calendarHandler.generateCalendarMessage(chatId, YearMonth.now());
        messageService.sendMessage(calendarMessage);
    }

    public void saveTaskWithNotificationDate(CallbackQuery callbackQuery) throws TelegramApiException {
        Long chatId = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();
        int dayOfMonth = Integer.parseInt(callbackData.substring(3));

        YearMonth selectedMonth = calendarHandler.selectedYearMonths.getOrDefault(chatId, YearMonth.now());
        LocalDate notificationDate = selectedMonth.atDay(dayOfMonth);
        selectedDates.put(chatId, notificationDate);

        userStates.put(chatId, UserState.AWAITING_NOTIFICATION_TIME);
        SendMessage timePickerMessage = timePickerHandler.createHourPickerMessage(chatId);
        messageService.sendMessage(timePickerMessage);
    }

    public void saveTaskWithNotificationTime(Long chatId, int hour, int minute) throws TelegramApiException {
        LocalDate notificationDate = selectedDates.get(chatId);
        if (notificationDate == null) {
            messageService.sendMessage(chatId, "Не вдалося налаштувати сповіщення: дата не визначена.");
            return;
        }

        LocalDateTime notificationDateTime = LocalDateTime.of(notificationDate, LocalTime.of(hour, minute));
        String taskDescription = taskDescriptions.get(chatId);

        Optional<User> userOptional = userService.findUserByChatId(chatId);
        User user = userOptional.orElseGet(() -> userService.createUser(chatId, null, null, null)); // Використання null для імені, прізвища та юзернейма, оскільки ми не маємо цих даних

        Task task = new Task();
        task.setDescription(taskDescription);
        task.setUser(user);
        task.setStatus(TaskStatus.NOT_COMPLETED);
        task.setDueDate(notificationDateTime);
        taskService.saveTask(task);

        try {
            notificationScheduler.scheduleNotification(chatId, notificationDateTime, Duration.ofMinutes(10));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        selectedDates.remove(chatId);
        taskDescriptions.remove(chatId);
        userStates.put(chatId, UserState.NONE);
        messageService.sendMessage(chatId, "Ваша задача збережена і ви отримаєте сповіщення за 10 хвилин до початку.");
    }
}