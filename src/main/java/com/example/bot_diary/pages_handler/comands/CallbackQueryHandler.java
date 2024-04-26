package com.example.bot_diary.pages_handler.comands;

import com.example.bot_diary.models.RegistrationRequest;
import com.example.bot_diary.models.User;
import com.example.bot_diary.models.UserState;
import com.example.bot_diary.pages_handler.comands.command_handler.*;
import com.example.bot_diary.service.UserService;
import com.example.bot_diary.utilities.ChecksForAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Slf4j
@Component
public class CallbackQueryHandler {

    @Autowired
    private CompletedTasksCommandHandler completedTasksCommandHandler;

    @Autowired
    private TimePickerHandler timePickerHandler;

    @Autowired
    private PostponedTasksCommandHandler postponedTasksCommandHandler;

    @Autowired
    private NewTaskCommandHandler newTaskCommandHandler;

    @Autowired
    private AllTasksCommandHandler allTasksCommandHandler;

    @Autowired
    private CalendarHandler calendarHandler;

    @Autowired
    private DeleteTasksCommandHandler deleteTasksCommandHandler;

    @Autowired
    private DateSelectionHandler dateSelectionHandler;

    @Autowired
    private MessageService messageService;

    @Autowired
    private AdminHandler adminHandler;

    @Autowired
    private UserService userService;

    @Value("${admin.chat.id}")
    private Long adminChatId;

    @Autowired
    private ChecksForAccess checksForAccess;

    @Autowired
    private  UserRegistrationHandler userRegistrationHandler;

    @Autowired
    private NotificationTimePickerHandler notificationTimePickerHandler;

    public void handleCallbackQuery(Update update) throws TelegramApiException {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();
        Optional<User> userOptional = userService.findUserByChatId(chatId);

        UserState currentState = newTaskCommandHandler.getUserStates().getOrDefault(chatId, UserState.NONE);
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        if (!checksForAccess.preChecks(update, currentState, userOptional, chatId)) {
            return; // Stop processing if pre-checks fail
        }

        if (callbackData.startsWith("HOURS_")) {
            notificationTimePickerHandler.handleHourSelection(update);
        }
        // Обробка вибору хвилин
        else if (callbackData.startsWith("MINUTES_")) {
            notificationTimePickerHandler.handleMinuteSelection(update);
        }

        if (callbackData.startsWith("HOUR_")) {
            if (currentState == UserState.AWAITING_NOTIFICATION_TIME) {
                timePickerHandler.handleHourSelection(update);
            } else {
                messageService.sendMessage(chatId, "Ви можете вибирати час лише під час встановлення сповіщення для задачі.");
            }
        } else if (callbackData.startsWith("MINUTE_")) {
            if (currentState == UserState.AWAITING_NOTIFICATION_TIME) {
                timePickerHandler.handleMinuteSelection(update);
            } else {
                messageService.sendMessage(chatId, "Ви можете вибирати хвилини лише під час встановлення сповіщення для задачі.");
            }
        }

        if (callbackData.startsWith("DELETE_USER_")) {
            try {
                String userIdStr = callbackData.substring("DELETE_USER_".length());
                Long userId = Long.parseLong(userIdStr);
                checksForAccess.deleteUser(userId, chatId);
            } catch (NumberFormatException e) {
                messageService.sendMessage(chatId, "Помилка: неправильний формат ID користувача.");
                log.error("Error parsing user ID: ", e);
            }
        }

        switch (callbackData) {
            case "continue_creation":
                newTaskCommandHandler.promptForNotificationDate(update.getCallbackQuery());
                break;
            case "save_task_now":
                newTaskCommandHandler.saveTaskAndNotifyUser(update.getCallbackQuery());
                break;
            case "APPLY_YES":
                RegistrationRequest request = new RegistrationRequest(chatId);
                adminHandler.addPendingRequest(request);
                messageService.sendMessage(chatId, "Вашу заявку надіслано на розгляд адміністратору.");
                break;
            case "APPLY_NO":
                messageService.sendMessage(chatId, "Ви вирішили не надсилати заявку.");
                break;
            case "yes_notify":
                // Отримання повідомлення для вибору години
                SendMessage hourPickerMessage = notificationTimePickerHandler.createHourPickerMessage(chatId);
                messageService.sendMessage(hourPickerMessage);
                break;
            case "no_notify":
                userRegistrationHandler.sendMainMenu(chatId);;
                break;
        }

        if (callbackData.startsWith("CREATE_TASK")) {
            newTaskCommandHandler.initiateNewTaskCreation(update.getCallbackQuery().getMessage().getChatId());
        } else if (callbackData.startsWith("MY_TASKS")) {
            allTasksCommandHandler.handle(update);
        } else if (callbackData.startsWith("DELETE_TASK_")) {
            deleteTasksCommandHandler.handle(update);
        } else if (callbackData.startsWith("REVERT_TASK_") || callbackData.startsWith("DONE_TASK_")) {
            completedTasksCommandHandler.handleCallbackQuery(update);
        } else if (callbackData.startsWith("POSTPONE_TASK_")) {
            postponedTasksCommandHandler.handleCallbackQuery(update);
        } else if (callbackData.startsWith("PREVIOUS_MONTH_") || callbackData.startsWith("NEXT_MONTH_")) {
            calendarHandler.handleMonthChange(callbackData, chatId, messageId);
        } else if (callbackData.startsWith("DAY")) {
            dateSelectionHandler.handleDaySelection(update, currentState);
        } else if (callbackData.startsWith("PAST")) {
            messageService.sendMessage(chatId, "Вибачте, але цей день уже пройшов. Будь ласка, виберіть поточний день або дату в майбутньому.");
        } else if (callbackData.startsWith("PAST_HOUR") || callbackData.startsWith("PAST_MINUTE")) {
            messageService.sendMessage(chatId, "Цей час уже пройшов і ви його не можете вибрати. Будь ласка, виберіть час, який ще не настав.");
        } else if (callbackData.startsWith("ACCEPT_")) {
            Long applicantChatId = Long.parseLong(callbackData.split("_")[1]);
            adminHandler.processApplication(applicantChatId, true, update);
        } else if (callbackData.startsWith("REJECT_")) {
            Long applicantChatId = Long.parseLong(callbackData.split("_")[1]);
            adminHandler.processApplication(applicantChatId, false, update);
        } else if (callbackData.startsWith("CONFIRM_")) {
            Long userId = Long.valueOf(callbackData.split("_")[1]);
            adminHandler.confirmUser(userId, chatId);
        } else if (callbackData.startsWith("BLOCK_")) {
            Long userId = Long.valueOf(callbackData.split("_")[1]);
            checksForAccess.blockUser(userId, adminChatId);
        } else if (callbackData.startsWith("UNBLOCK_")) {
            Long userId = Long.valueOf(callbackData.split("_")[1]);
            checksForAccess.unblockUser(userId);
        }
    }

}