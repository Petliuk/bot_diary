package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.job.NotificationScheduler;
import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.models.User;
import com.example.bot_diary.models.UserState;
import com.example.bot_diary.service.TaskService;
import com.example.bot_diary.service.UserService;
import com.example.bot_diary.utilities.UserButtons;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class NewTaskCommandHandler {


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

    private final Map<Long, String> taskDescriptions = new HashMap<>();
    private final Map<Long, LocalDate> selectedDates = new HashMap<>();
    private final Map<Long, UserState> userStates = new HashMap<>();
    private final Map<Long, Integer> notificationHours = new HashMap<>();
    private final Map<Long, Integer> notificationMinutes = new HashMap<>();

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

        Optional<User> userOptional = userService.findUserByChatId(chatId);
        User user = userOptional.orElseGet(() -> userService.createUser(chatId,
                update.getMessage().getFrom().getFirstName(),
                update.getMessage().getFrom().getLastName(),
                update.getMessage().getFrom().getUserName()));

        if (taskService.countUserTasks(user.getChatId()) >= 200) {
            messageService.sendMessage(chatId, "Ви вже створили максимально дозволену кількість завдань (200). Видаліть існуючі завдання, щоб створити нові.");
            return;
        }

        taskDescriptions.put(chatId, taskDescription);
        userStates.put(chatId, UserState.TASK_CREATED);
        sendOptionsAfterTaskCreation(chatId);
    }

    private void sendOptionsAfterTaskCreation(long chatId) throws TelegramApiException {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(UserButtons.createConfirmationButtons("Так", "continue_creation", "Ні", "save_task_now"));

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Бажаєте обрати час?");
        message.setReplyMarkup(inlineKeyboardMarkup);
        messageService.sendMessage(message);
    }

    public void saveTaskAndNotifyUser(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();

        if (!userStates.getOrDefault(chatId, UserState.NONE).equals(UserState.TASK_CREATED)) {
            messageService.sendMessage(chatId, "Задача вже була збережена або не була створена.");
            return;
        }

        String taskDescription = taskDescriptions.get(chatId);
        Task task = new Task();
        task.setDescription(taskDescription);
        task.setUser(userService.findUserByChatId(chatId).orElse(null));
        task.setStatus(TaskStatus.NOT_COMPLETED);

        taskService.saveTask(task);

        userStates.put(chatId, UserState.NONE);
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

    public void saveTaskWithNotificationTime(Long chatId, int hour, int minute) {
        LocalDate dueDate = selectedDates.get(chatId);
        if (dueDate == null) {
            messageService.sendMessage(chatId, "Не вдалося налаштувати час завдання: дата не визначена.");
            return;
        }

        LocalDateTime dueDateTime = LocalDateTime.of(dueDate, LocalTime.of(hour, minute));
        String taskDescription = taskDescriptions.get(chatId);
        User user = userService.findUserByChatId(chatId).orElse(null);

        Task task = new Task();
        task.setDescription(taskDescription);
        task.setUser(user);
        task.setStatus(TaskStatus.NOT_COMPLETED);
        task.setDueDate(dueDateTime);

        taskService.saveTask(task);

      /*  updateNotificationTime(chatId, dueDate, hour, minute);*/

/*        selectedDates.remove(chatId);*/
        taskDescriptions.remove(chatId);
        userStates.put(chatId, UserState.NONE);
        messageService.sendMessage(chatId, "Вшв задача збережена з часом. ");
    }

    public void updateNotificationTime(Long chatId, LocalDate date, int hour, int minute) {

        LocalDateTime notificationTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
        Task task = taskService.findLastTaskByChatId(chatId);
        if (task != null) {
            task.setNotificationTime(notificationTime);
            taskService.saveTask(task);
            messageService.sendMessage(chatId, "Час сповіщення для вашої задачі було оновлено.");
            selectedDates.remove(chatId);
        } else {
            messageService.sendMessage(chatId, "Задача не знайдена.");
        }
    }
}