package com.example.bot_diary.bot;

import com.example.bot_diary.configuration.BotConfig;
import com.example.bot_diary.pages_handler.comands.BotService;
import com.example.bot_diary.pages_handler.comands.CommandHandler;
import com.example.bot_diary.pages_handler.comands.command_handler.*;
import com.example.bot_diary.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot implements BotService {

    @Autowired
    private BotConfig config;

    @Autowired
    CompletedTasksCommandHandler completedTasksCommandHandler;
    @Autowired
    PostponedTasksCommandHandler postponedTasksCommandHandler;

    @Autowired
    private StartCommandHandler startCommandHandler;

    @Autowired
    private NewTaskCommandHandler newTaskCommandHandler;

    @Autowired
    private AllTasksCommandHandler allTasksCommandHandler;

    ///////*    @Autowired
    /////////   private AllTasksCallbackHandler callbackQueryHandler;*/

    @Autowired
    CommandHandler commandHandler;

    @Autowired
    private DeleteTasksCommandHandler deleteTasksCommandHandler;

    @Autowired
    private MessageService messageService;

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
            // Можливо, ви захочете відправити повідомлення про помилку користувачеві тут
        }
    }

    private void handleTextMessage(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        NewTaskCommandHandler.UserState currentState = newTaskCommandHandler.getUserStates().getOrDefault(chatId, NewTaskCommandHandler.UserState.NONE);

        switch (messageText) {
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
            // Додайте інші команди тут, якщо потрібно
        }
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        if (text == null || text.isEmpty()) {
            log.error("Attempted to send empty message to chatId: " + chatId);
            return; // Просто повернутися, не намагаючись відправити повідомлення
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



/*    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            commandHandler.handleCommand(update);
        }
    }
}*/





/*    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;


    private enum BotState {
        AWAITING_TASK_DESCRIPTION
    }

 *//*   private final Map<Long, BotState>erStates = us new ConcurrentHashMap<>();*/



  /*  @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            switch (messageText) {
                case "/newtask":
                    userStates.put(chatId, BotState.AWAITING_TASK_DESCRIPTION);
                    sendMessage(chatId, "Введіть опис завдання:");
                    break;
                case "/alltasks":
                    List<Task> tasks = taskService.findAllTasks();
                    String message = tasks.stream()
                            .map(task -> task.getId() + ": " + task.getDescription() + " [" + task.getStatus().getDisplayName() + "]")
                            .collect(Collectors.joining("\n"));
                    sendMessage(chatId, message.isEmpty() ? "Задачі відсутні." : message);
                    break;
                default:
                    if (userStates.getOrDefault(chatId, null) == BotState.AWAITING_TASK_DESCRIPTION) {
                        User user = userService.findOrCreateUser(chatId);
                        Task task = new Task();
                        task.setDescription(messageText);
                        task.setUser(user);
                        task.setStatus(TaskStatus.NOT_COMPLETED);
                        taskService.saveTask(task);

                        userStates.remove(chatId);
                        sendMessage(chatId, "Завдання створено!");
                    }
                    break;
            }
        }
    }*/

