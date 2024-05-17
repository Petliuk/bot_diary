package com.example.bot_diary.pages_handler.comands;

import com.example.bot_diary.models.User;
import com.example.bot_diary.models.UserState;
import com.example.bot_diary.pages_handler.comands.command_handler.*;
import com.example.bot_diary.service.UserService;
import com.example.bot_diary.utilities.ChecksForAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Slf4j
@Component
public class CommandHandler {

    @Autowired
    private AdminHandler adminHandler;
    @Autowired
    private UserRegistrationHandler userRegistrationHandler;
    @Autowired
    private NewTaskCommandHandler newTaskCommandHandler;
    @Autowired
    private CompletedTasksCommandHandler completedTasksCommandHandler;
    @Autowired
    private PostponedTasksCommandHandler postponedTasksCommandHandler;
    @Autowired
    private CalendarHandler calendarHandler;
    @Autowired
    private AllTasksCommandHandler allTasksCommandHandler;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ChecksForAccess checksForAccess;
    @Autowired
    private DescriptionUpdateHandler descriptionUpdateHandler;
    @Autowired
    private DateTimeCallbackHandler dateTimeCallbackHandler;

    @Value("${admin.chat.id}")
    private Long adminChatId;

    public void handleTextMessage(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        Optional<User> userOptional = userService.findUserByChatId(chatId);
        String messageText = update.getMessage().getText();

        if (!checksForAccess.isUserEligibleToProceed(chatId, userOptional, messageText)) return;

        if (descriptionUpdateHandler.isAwaitingDescription(chatId)) {
            descriptionUpdateHandler.updateDescription(chatId, messageText);
            return;
        }
        UserState currentState = newTaskCommandHandler.getUserStates().getOrDefault(chatId, UserState.NONE);
        if (currentState == UserState.AWAITING_TASK_DESCRIPTION && messageText.startsWith("/")) {
            newTaskCommandHandler.getUserStates().put(chatId, UserState.NONE);
            messageService.sendMessage(chatId, "Створення нової задачі скасовано.");
        }

        switch (messageText) {
            case "/postponed": postponedTasksCommandHandler.handle(update); break;
            case "/newtask": newTaskCommandHandler.handle(update); break;
            case "/done": completedTasksCommandHandler.handle(update); break;
            case "/alltasks": allTasksCommandHandler.handle(update); break;
            case "/start": userRegistrationHandler.handleUserRegistration(update); break;
            case "/calendar": calendarHandler.handleCalendarCommand(update); break;
            case "/help": messageService.sendHelpMessage(chatId);break;
            case "/b":
            case "/users":
            case "/blockedusers":

                handleAdminCommands(chatId, messageText, update);
                break;
            default: handleDefaultCommand(chatId, currentState, update);
        }
    }

    private void handleAdminCommands(long chatId, String messageText, Update update) throws TelegramApiException {
        if (chatId == adminChatId) {
            switch (messageText) {
                case "/b": adminHandler.handleAdminCommands(update); break;
                case "/users": adminHandler.handleListUsersCommand(update); break;
                case "/blockedusers": adminHandler.showBlockedUsers(chatId); break;
            }
        } else {
            messageService.sendMessage(chatId, "Використовуйте команди та кнопки які вам надає бот!");
        }
    }

    private void handleDefaultCommand(long chatId, UserState currentState, Update update) throws TelegramApiException {
        if (currentState == UserState.AWAITING_TASK_DESCRIPTION) {
            newTaskCommandHandler.handle(update);
        } else {
            messageService.sendMessage(chatId, "Використовуйте команди та кнопки які вам надає бот!");
        }
    }

}