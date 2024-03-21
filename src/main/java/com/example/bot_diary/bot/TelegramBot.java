package com.example.bot_diary.bot;

import com.example.bot_diary.configuration.BotConfig;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.pages_handler.comands.BotService;
import com.example.bot_diary.pages_handler.comands.command_handler.*;
import com.example.bot_diary.service.MessageService;
import com.example.bot_diary.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot implements BotService {

    @Autowired
    private BotConfig config;

    @Autowired
    CompletedTasksCommandHandler completedTasksCommandHandler;

    @Autowired
    TimePickerHandler timePickerHandler;

    @Autowired
    PostponedTasksCommandHandler postponedTasksCommandHandler;

    @Autowired
    private StartCommandHandler startCommandHandler;

    @Autowired
    private NewTaskCommandHandler newTaskCommandHandler;

    @Autowired
    private AllTasksCommandHandler allTasksCommandHandler;

    @Autowired
    private CalendarHandler calendarHandler;

    @Autowired
    private DeleteTasksCommandHandler deleteTasksCommandHandler;

    @Autowired
    private MessageService messageService;

    @Autowired
    private TaskService taskService;

    private final Map<Long, YearMonth> selectedYearMonths = new HashMap<>();

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }


    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update);
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void handleTextMessage(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        NewTaskCommandHandler.UserState currentState = newTaskCommandHandler.getUserStates().getOrDefault(chatId, NewTaskCommandHandler.UserState.NONE);

        if ("/calendar".equals(messageText)) {
            YearMonth currentMonth = YearMonth.now();
            SendMessage calendarMessage = calendarHandler.generateCalendarMessage(chatId, currentMonth);
            execute(calendarMessage);
        }

        switch (messageText) {
        /*    case "/time":
                SendMessage timePickerMessage = timePickerHandler.createHourPickerMessage(chatId);
                execute(timePickerMessage);
                break;*/
            case "/postponed":
                postponedTasksCommandHandler.handle(update);
                break;
            case "/newtask":
                newTaskCommandHandler.handle(update);
                break;
            case "/done":
                completedTasksCommandHandler.handle(update);
                break;
            case "/alltasks":
                handleCommand(update, messageText);
                break;
            case "/start":
                startCommandHandler.handle(update);
                break;
            case "/help":
                messageService.sendHelpMessage(chatId);
                break;
            default:
                if (currentState == NewTaskCommandHandler.UserState.AWAITING_TASK_DESCRIPTION && !messageText.startsWith("/")) {
                    newTaskCommandHandler.handle(update);
                }
                break;
        }
    }

    private void handleCallbackQuery(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        NewTaskCommandHandler.UserState currentState = newTaskCommandHandler.getUserStates().getOrDefault(chatId, NewTaskCommandHandler.UserState.NONE);
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        if (StartCommandHandler.CREATE_TASK.equals(callbackData)) {
            newTaskCommandHandler.initiateNewTaskCreation(update.getCallbackQuery().getMessage().getChatId());
        } else if (callbackData.startsWith("MY_TASKS")) {
            allTasksCommandHandler.handle(update);
        } else if (callbackData.startsWith("DELETE_TASK_")) {
            deleteTasksCommandHandler.handle(update);
        } else if (callbackData.startsWith("REVERT_TASK_") || callbackData.startsWith("DONE_TASK_")) {
            completedTasksCommandHandler.handleCallbackQuery(update);
        } else if (callbackData.startsWith("POSTPONE_TASK_")) {
            postponedTasksCommandHandler.handleCallbackQuery(update);
        } else if ("save_task".equals(callbackData)) {
            newTaskCommandHandler.saveTaskAndNotifyUser(update.getCallbackQuery());
        }else if (callbackData.startsWith("PREVIOUS_MONTH_") || callbackData.startsWith("NEXT_MONTH_")) {
            calendarHandler.handleMonthChange(callbackData, chatId, messageId);
        }



    else if ("continue_creation".equals(callbackData)) {
            newTaskCommandHandler.promptForNotificationDate(update.getCallbackQuery());
        } else if (callbackData.startsWith("DAY")) {
            int dayOfMonth = Integer.parseInt(callbackData.substring(3));
            YearMonth selectedMonth = calendarHandler.selectedYearMonths.getOrDefault(chatId, YearMonth.now());
            // Створюємо дату з використанням вибраного року та місяця
            LocalDate selectedDate = selectedMonth.atDay(dayOfMonth);
            List<Task> tasksForDay = taskService.findTasksForDay(selectedDate, chatId);

            if (!tasksForDay.isEmpty() || currentState == NewTaskCommandHandler.UserState.AWAITING_NOTIFICATION_DATE) {
                if (currentState == NewTaskCommandHandler.UserState.AWAITING_NOTIFICATION_DATE) {
                    // Користувач хоче створити нову задачу і вибрав дату
                    newTaskCommandHandler.saveTaskWithNotificationDate(update.getCallbackQuery());
                } else {
                    // Користувач хоче переглянути задачі за обраною датою
             showTasksForSelectedDay(chatId, selectedDate, tasksForDay);
                }
            } else {
                sendMessage(chatId, "Завдань не існує");
            }




        } else if (callbackData.startsWith("HOUR_")) {
            int chosenHour = Integer.parseInt(callbackData.substring(5));
            SendMessage minutePickerMessage = timePickerHandler.createMinutePickerMessage(chatId, chosenHour);
            execute(minutePickerMessage);
        } else if (callbackData.startsWith("MINUTE_")) {
            String[] parts = callbackData.split("_");
            int chosenHour = Integer.parseInt(parts[1]);
            int chosenMinute = Integer.parseInt(parts[2]);
            // Тут ваш код для збереження задачі з вибраним часом
            newTaskCommandHandler.saveTaskWithNotificationTime(chatId, chosenHour, chosenMinute);
        }
    }


   private void showTasksForSelectedDay(long chatId, LocalDate selectedDate, List<Task> tasksForDay) throws TelegramApiException {
        if (tasksForDay.isEmpty()) {
            sendMessage(chatId, "На цей день задачі відсутні.");
        } else {
            for (Task task : tasksForDay) {
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();

                InlineKeyboardButton deleteButton = new InlineKeyboardButton();
                deleteButton.setText("Видалити");
                deleteButton.setCallbackData("DELETE_TASK_" + task.getId());
                rowInline.add(deleteButton);


                InlineKeyboardButton doneButton = new InlineKeyboardButton();
                doneButton.setText("Виконано");
                doneButton.setCallbackData("DONE_TASK_" + task.getId());
                rowInline.add(doneButton); // Додайте цю кнопку до рядка інлайн-клавіатури


                InlineKeyboardButton postponeButton = new InlineKeyboardButton();
                postponeButton.setText("Відкласти");
                postponeButton.setCallbackData("POSTPONE_TASK_" + task.getId());
                rowInline.add(postponeButton); // Додайте цю кнопку до рядка інлайн-клавіатури


                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);


                String messageText = "🗓 Дата: " + task.getDueDate().toLocalDate() + "\n" +
                        "⏰ Час: " + task.getDueDate().toLocalTime() + "\n" +
                        "🔖 Статус: " + getTaskStatusText(task.getStatus()) + "\n" +
                        "📝 Опис: " + task.getDescription() + "\n";

                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText(messageText);
                message.setReplyMarkup(markupInline);
                execute(message);
            }
        }
    }

    private String getTaskStatusText(TaskStatus status) {
        switch (status) {
            case NOT_COMPLETED:
                return "Не виконано";
            case COMPLETED:
                return "Виконано";
            case POSTPONED:
                return "Відкладено";
            default:
                return "Невідомий статус";
        }
    }

    private void handleCommand(Update update, String command) throws TelegramApiException {
        switch (command) {
            case "/newtask":
                newTaskCommandHandler.handle(update);
                break;
            case "/alltasks":
                allTasksCommandHandler.handle(update);
                break;
        }
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        if (text == null || text.isEmpty()) {
            log.error("Attempted to send empty message to chatId: " + chatId);
            return;
        }
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    @Override
    public void editMessage(EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    @Override
    public void sendMessage(SendMessage message) throws TelegramApiException {
        execute(message);
    }

}



