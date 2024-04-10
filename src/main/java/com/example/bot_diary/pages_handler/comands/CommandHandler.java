package com.example.bot_diary.pages_handler.comands;

import com.example.bot_diary.models.User;
import com.example.bot_diary.pages_handler.comands.command_handler.*;
import com.example.bot_diary.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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

    public void handleTextMessage(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        Optional<User> userOptional = userService.findUserByChatId(chatId);
        String messageText = update.getMessage().getText();

        if (!(chatId == 712909082L) && userOptional.isPresent() && userOptional.get().getStatus() == AdminHandler.UserStatus.UNCONFIRMED) {
            messageService.sendMessage(chatId, "Вашу заявку ще не підтверджено...");
            return;
        }


        if ("/b".equals(messageText)) {
            if (chatId == 712909082L) { // Перевірка чи chatId належить адміністратору
                adminHandler.handleAdminCommands(update);
            } else {
                messageService.sendMessage(chatId, "Ви не маєте права використовувати цю команду.");
            }
            return; // Завершуємо обробку команди, щоб не виконувати інші перевірки
        }

        if ("/users".equals(messageText)) {
            if (chatId == 712909082L) {
                adminHandler.handleListUsersCommand(update);
            } else {
                messageService.sendMessage(chatId, "Ви не маєте права використовувати цю команду.");
            }
            return;
        }


        // Якщо користувач незареєстрований і команда не є командою старту, надішліть повідомлення про необхідність реєстрації
        if (userOptional.isEmpty() && !"/start".equals(update.getMessage().getText())) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Будь ласка, спочатку зареєструйтесь, використовуючи команду /start.");
            messageService.sendMessage(message);
            return; // Припиняємо обробку команд
        }
        if ("/blockedusers".equals(messageText) && chatId == 712909082L) {
            adminHandler.showBlockedUsers(chatId);
        } else {
            messageService.sendMessage(chatId, "Ви не маєте права використовувати цю команду.");
        }
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
                allTasksCommandHandler.handle(update);
                break;
            case "/start":
                userRegistrationHandler.handleUserRegistration(update);
                break;


         /*   case "/help":
                messageService.sendHelpMessage(chatId);
                break;*/
            case "/calendar":
                calendarHandler.handleCalendarCommand(update);
                break;
            default:
                if (currentState == NewTaskCommandHandler.UserState.AWAITING_TASK_DESCRIPTION && !messageText.startsWith("/")) {
                    newTaskCommandHandler.handle(update);
                }
                break;
        }
    }

}
