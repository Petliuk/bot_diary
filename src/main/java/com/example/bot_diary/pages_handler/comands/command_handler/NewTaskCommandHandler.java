package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.models.User;
import com.example.bot_diary.pages_handler.comands.MessageService;
import com.example.bot_diary.service.TaskService;
import com.example.bot_diary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    TimePickerHandler timePickerHandler;

 /*   @Autowired
    private BotService botService;*/

    @Autowired
    private MessageService messageService;

    @Autowired
    private TaskService taskService;


    @Autowired
    private CalendarHandler calendarHandler;

    private final Map<Long, UserState> userStates = new HashMap<>();

    public Map<Long, UserState> getUserStates() {
        return userStates;
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

        User user = userService.findOrCreateUser(chatId);
        Task task = new Task();
        task.setDescription(update.getMessage().getText());
        task.setUser(user);
        task.setStatus(TaskStatus.NOT_COMPLETED);
        userStates.put(chatId, UserState.TASK_CREATED);
        sendOptionsAfterTaskCreation(chatId);
    }

    private void sendOptionsAfterTaskCreation(long chatId) throws TelegramApiException {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton buttonContinue = new InlineKeyboardButton();
        buttonContinue.setText("Так");
        buttonContinue.setCallbackData("continue_creation");

        InlineKeyboardButton buttonSetNotification = new InlineKeyboardButton();
        buttonSetNotification.setText("Ні");
        buttonSetNotification.setCallbackData("save_task");

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(buttonContinue);
        keyboardButtonsRow1.add(buttonSetNotification);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);

        inlineKeyboardMarkup.setKeyboard(rowList);


        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setChatId(String.valueOf(chatId));
        message.setText("Бажаєте налаштувати сповіщення?");
        message.setReplyMarkup(inlineKeyboardMarkup);
        messageService.sendMessage(message);

    }

    public void saveTaskAndNotifyUser(CallbackQuery callbackQuery) throws TelegramApiException {
        Long chatId = callbackQuery.getMessage().getChatId();

        String taskDescription = taskDescriptions.getOrDefault(chatId, "Опис не знайдено");

        User user = userService.findOrCreateUser(chatId);
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

        // Використання вибраного року та місяця замість поточного
        YearMonth selectedMonth = calendarHandler.selectedYearMonths.getOrDefault(chatId, YearMonth.now());
        LocalDate notificationDate = selectedMonth.atDay(dayOfMonth);
        selectedDates.put(chatId, notificationDate); // Зберігаємо обрану дату

        userStates.put(chatId, UserState.AWAITING_NOTIFICATION_TIME);
        SendMessage timePickerMessage = timePickerHandler.createHourPickerMessage(chatId);
        messageService.sendMessage(timePickerMessage);
    }

    public void saveTaskWithNotificationTime(Long chatId, int hour, int minute) throws TelegramApiException {
        LocalDate notificationDate = selectedDates.get(chatId);
        if (notificationDate == null) {
            messageService.sendMessage(chatId, "Помилка: Дата не була вибрана.");
            return;
        }

        // Створення LocalDateTime без конвертації часової зони
        LocalDateTime dueDateTime = LocalDateTime.of(notificationDate, LocalTime.of(hour, minute));

        Task task = new Task();
        task.setDescription(taskDescriptions.get(chatId));
        task.setUser(userService.findOrCreateUser(chatId));
        task.setStatus(TaskStatus.NOT_COMPLETED);
        task.setDueDate(dueDateTime); // Не конвертувати час
        taskService.saveTask(task);


        // Прибирання
        selectedDates.remove(chatId);
        taskDescriptions.remove(chatId);
        userStates.put(chatId, UserState.NONE);

        messageService.sendMessage(chatId, "Ваша задача зі сповіщенням за часом збережена.");
        calendarHandler.selectedYearMonths.remove(chatId);
    }
}