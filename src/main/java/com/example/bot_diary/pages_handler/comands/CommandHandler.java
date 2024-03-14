package com.example.bot_diary.pages_handler.comands;

import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.models.User;
import com.example.bot_diary.pages_handler.comands.command_handler.StartCommandHandler;
import com.example.bot_diary.service.MessageService;
import com.example.bot_diary.service.TaskService;
import com.example.bot_diary.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.example.bot_diary.models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CommandHandler {

    private final UserService userService;
    private final TaskService taskService;
    private final MessageService messageService;

    private final BotService botService;

    @Autowired
    public CommandHandler(UserService userService, TaskService taskService, MessageService messageService, BotService botService) {
        this.userService = userService;
        this.taskService = taskService;
        this.messageService = messageService;
        this.botService = botService;
    }
    public enum UserState {
        NONE,
        AWAITING_TASK_DESCRIPTION
    }

    private final Map<Long, UserState> userStates = new HashMap<>();

    public void handleCommand(Update update) throws TelegramApiException {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    sendStartCommand(chatId);
                    break;
                case "/newtask":
                    userStates.put(chatId, UserState.AWAITING_TASK_DESCRIPTION);
                    messageService.sendMessage(chatId, "Напишіть нову задачу:");
                    break;
                case "/alltasks":
                    // Виклик методу для отримання та відправлення всіх задач
                    handleAllTasks(chatId);
                    break;

                /*case "/start":
                    userService.registerUser(update.getMessage());
                    messageService.sendStartMessage(chatId, update.getMessage().getChat().getLastName());
                    break;*/

              case "/help":
                    messageService.sendHelpMessage(chatId);
                    break;

                case "/register":
                    messageService.sendRegisterMessage(chatId);
                    break;
                default:
                    handleDefaultMessage(chatId, messageText);
                    break;

               /* default:
                    messageService.sendUnknownCommandMessage(chatId);*/
            }
        }
    }
    static final String CREATE_TASK = "CREATE_TASK";
    static final String MY_TASKS = "MY_TASKS";
    public void handleCallbackQuery(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (callbackData.startsWith("UPDATE_TASK_")) {
            Long taskId = Long.parseLong(callbackData.split("_")[2]);
            Task task = taskService.findTaskById(taskId); // Вам потрібно додати метод findTaskById у TaskService
            if (task != null) {
                // Тут можна змінити статус задачі або оновити інші поля
                taskService.updateTask(task);

                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText("Задача оновлена.");
                botService.sendMessage(message);
            }else {
                System.out.println("-------------------------------------------------------------------------------------");
                System.out.println("Задачі з індексом " + taskId + " не існує");
            }
        }
        if (callbackData.startsWith("DELETE_TASK_")) {
            Long taskId = Long.parseLong(callbackData.split("_")[2]);
            taskService.deleteTask(taskId);
            // Відправте повідомлення про успішне видалення
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Задачу видалено.");
            botService.sendMessage(message);
        }

        switch (callbackData) {
            case CREATE_TASK:
                userStates.put(chatId, UserState.AWAITING_TASK_DESCRIPTION); // Встановлюємо стан користувача
                SendMessage createTaskMessage = new SendMessage();
                createTaskMessage.setChatId(String.valueOf(chatId));
                createTaskMessage.setText("Напишіть нову задачу:");
                botService.sendMessage(createTaskMessage);
                break;
            case MY_TASKS:
                sendAllTasks(chatId);
                break;

            // ...
        }
    }
    public void sendStartCommand(long chatId) throws TelegramApiException {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton createTaskButton = new InlineKeyboardButton();
        createTaskButton.setText("Створити задачу");
        createTaskButton.setCallbackData(CREATE_TASK);

        InlineKeyboardButton myTasksButton = new InlineKeyboardButton();
        myTasksButton.setText("Мої завдання");
        myTasksButton.setCallbackData(MY_TASKS);

        rowInline.add(createTaskButton);
        rowInline.add(myTasksButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Виберіть пункт:");
        message.setReplyMarkup(markupInline);

        message.getText();
        if (!message.getText().isEmpty()) {
            botService.sendMessage(message);
        } else {
            log.error("Cannot send message with empty text to chatId: " + chatId);
        }

        // Тепер використовується command замість messageService
    }

    private void sendAllTasks(Long chatId) throws TelegramApiException {
        List<Task> tasks = taskService.findAllTasks();
        StringBuilder response = new StringBuilder();
        if (tasks.isEmpty()) {
            response.append("Задачі відсутні.");
        } else {
            for (Task task : tasks) {
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();

                InlineKeyboardButton updateButton = new InlineKeyboardButton();
                updateButton.setText("Оновити");
                updateButton.setCallbackData("UPDATE_TASK_" + task.getId());
                rowInline.add(updateButton); // Додайте цю кнопку до рядка інлайн-клавіатури

                InlineKeyboardButton deleteButton = new InlineKeyboardButton();
                deleteButton.setText("Видалити");
                deleteButton.setCallbackData("DELETE_TASK_" + task.getId());
                rowInline.add(deleteButton);
                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);

                response.append("ID: ").append(task.getId())
                        .append("\nОпис: ").append(task.getDescription())
                        .append("\nСтатус: ").append(task.getStatus().getDisplayName())
                        .append("\n\n");

                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText(response.toString());
                message.setReplyMarkup(markupInline);
                botService.sendMessage(message);

                // Очистіть StringBuilder для наступної задачі
                response.setLength(0);
            }
        }
    }
/*
    public void handleCallbackQuery(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        messageService.handleCallbackQuery(callbackData, chatId, messageId);
    }
*/

    private void handleDefaultMessage(Long chatId, String messageText) throws TelegramApiException {
        UserState currentState = userStates.getOrDefault(chatId, UserState.NONE);
        if (currentState == UserState.AWAITING_TASK_DESCRIPTION) {
            // Логіка створення нової задачі
            User user = userService.findOrCreateUser(chatId);
            Task task = new Task();
            task.setDescription(messageText);
            task.setUser(user);
            task.setStatus(TaskStatus.NOT_COMPLETED);
            taskService.saveTask(task);

            userStates.put(chatId, UserState.NONE); // Оновлюємо стан користувача, після того як задача створена
            messageService.sendMessage(chatId, "Ваша задача збережена.");
        } else {
            messageService.sendMessage(chatId, "Невідома команда або текст. Спробуйте /start для відображення меню.");
        }
    }


    private void handleAllTasks(Long chatId) throws TelegramApiException {
        List<Task> tasks = taskService.findAllTasks();
        StringBuilder response = new StringBuilder();
        if (tasks.isEmpty()) {
            response.append("Задачі відсутні.");
        } else {
            for (Task task : tasks) {
                response.append("ID: ").append(task.getId())
                        .append("\nОпис: ").append(task.getDescription())
                        .append("\nСтатус: ").append(task.getStatus())
                        .append("\n\n");
            }
        }
        messageService.sendMessage(chatId, response.toString());
    }

}

