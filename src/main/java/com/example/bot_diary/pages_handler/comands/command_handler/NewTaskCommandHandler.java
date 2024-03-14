package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.models.User;
import com.example.bot_diary.pages_handler.comands.BotService;
import com.example.bot_diary.service.MessageService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NewTaskCommandHandler {
    public enum UserState {
        NONE,
        AWAITING_TASK_DESCRIPTION,
        TASK_CREATED // новий стан для обробки після створення задачі
    }
    private final Map<Long, String> taskDescriptions = new HashMap<>();

    @Autowired
    private UserService userService;

    @Autowired
    private BotService botService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private MessageService messageService;

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
            // Якщо ні, ініціюємо створення нової задачі
            initiateNewTaskCreation(chatId);
        } else {
            // Якщо так, продовжуємо зі створенням задачі
            createTask(update);
        }
      /*  if (currentState == UserState.AWAITING_TASK_DESCRIPTION) {
            createTask(update);
        }*/
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
        // Замість збереження задачі, змініть стан і запитайте про наступні кроки
        userStates.put(chatId, UserState.TASK_CREATED);
        sendOptionsAfterTaskCreation(chatId); // відправте кнопки користувачеві
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
        botService.sendMessage(message);

    }
    public void saveTaskAndNotifyUser(CallbackQuery callbackQuery) throws TelegramApiException {
        Long chatId = callbackQuery.getMessage().getChatId();

        // Використання збереженого опису задачі
        String taskDescription = taskDescriptions.getOrDefault(chatId, "Опис не знайдено");

        // Створення та збереження задачі з актуальним описом
        User user = userService.findOrCreateUser(chatId);
        Task task = new Task();
        task.setDescription(taskDescription);
        task.setUser(user);
        task.setStatus(TaskStatus.NOT_COMPLETED);
        taskService.saveTask(task);

        // Очистка стану користувача та збереженого опису
        userStates.put(chatId, UserState.NONE);
        taskDescriptions.remove(chatId);

        // Відправлення повідомлення користувачу
        botService.sendMessage(chatId, "Ваша задача збережена.");
    }
}