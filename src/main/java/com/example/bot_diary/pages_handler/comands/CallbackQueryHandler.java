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
        @Autowired
        private ChecksForAccess checksForAccess;
        @Autowired
        private UpdateHandler updateHandle;
        @Autowired
        private DescriptionUpdateHandler descriptionUpdateHandler;
        @Autowired
        private NotificationTimePickerHandler notificationTimePickerHandler;
        @Autowired
        private DateTimeCallbackHandler dateTimeCallbackHandler;
        @Autowired
        private DateTimeUpdateHandler dateTimeUpdateHandler;

        @Value("${admin.chat.id}")
        private Long adminChatId;

        public void handleCallbackQuery(Update update) throws TelegramApiException {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();
            Optional<User> userOptional = userService.findUserByChatId(chatId);

            UserState currentState = newTaskCommandHandler.getUserStates().getOrDefault(chatId, UserState.NONE);
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            System.out.println("Received callback data: " + callbackData);

            if (!checksForAccess.preChecks(update, currentState, userOptional, chatId)) {
                return;
            } else if (callbackData.startsWith("DELETE_TASK_")) {
                deleteTasksCommandHandler.handle(update);
            } else if (callbackData.startsWith("REVERT_TASK_") || callbackData.startsWith("DONE_TASK_")) {
                completedTasksCommandHandler.handleCallbackQuery(update);
            } else if (callbackData.startsWith("POSTPONE_TASK_")) {
                postponedTasksCommandHandler.handleCallbackQuery(update);
            } else if (callbackData.startsWith("PREVIOUS_MONTH_") || callbackData.startsWith("NEXT_MONTH_")) {
                calendarHandler.handleMonthChange(callbackData, chatId, messageId);
            } else if (callbackData.startsWith("DAY")) {
              /*  dateTimeCallbackHandler.handleDateSelection(update, currentState);*/
                dateSelectionHandler.handleDaySelection(update, currentState);
            } else if (callbackData.startsWith("PAST")) {
                messageService.sendMessage(chatId, "Вибачте, але цей день уже пройшов. Будь ласка, виберіть поточний день або дату в майбутньому.");
            } else if (callbackData.startsWith("PAST_HOUR") || callbackData.startsWith("PAST_MINUTE")) {
                messageService.sendMessage(chatId, "Цей час уже пройшов і ви його не можете вибрати. Будь ласка, виберіть час, який ще не настав.");
            } else if (callbackData.startsWith("ACCEPT_")) {
                adminHandler.processApplication(Long.parseLong(callbackData.split("_")[1]), true, update);
            } else if (callbackData.startsWith("REJECT_")) {
                adminHandler.processApplication(Long.parseLong(callbackData.split("_")[1]), false, update);
            } else if (callbackData.startsWith("CONFIRM_")) {
                adminHandler.confirmUser(Long.valueOf(callbackData.split("_")[1]), chatId);
            } else if (callbackData.startsWith("BLOCK_")) {
                checksForAccess.blockUser(Long.valueOf(callbackData.split("_")[1]), adminChatId);
            } else if (callbackData.startsWith("UNBLOCK_")) {
                checksForAccess.unblockUser(Long.valueOf(callbackData.split("_")[1]));
            } else if (callbackData.startsWith("DELETE_NOTIF_")) {
                descriptionUpdateHandler.deleteNotification(update, callbackData);
            } else if (callbackData.startsWith("UPDATE_TASK_")) {
                updateHandle.handleTaskUpdate(callbackData, update.getCallbackQuery().getMessage().getChatId());
            } else if (callbackData.startsWith("HOUR_")) {
                timePickerHandler.handleHourSelection(update, currentState);
            } else if (callbackData.startsWith("MINUTE_")) {
                timePickerHandler.handleMinuteSelection(update, currentState);
            } else if (callbackData.startsWith("ADD_NOTIFICATION_TASK_")) {
                notificationTimePickerHandler.handleTaskNotificationInitiation(callbackData, update);
            } else if (callbackData.startsWith("CAL_DAYS_")) {
                dateTimeCallbackHandler.handleCalendarDays(update, currentState);
            } else if (callbackData.startsWith("SELECT_HOURS_")) {
                dateTimeCallbackHandler.handleHourSelection(update, currentState);
            } else if (callbackData.startsWith("SELECT_MINUTES_")) {
                dateTimeCallbackHandler.handleMinuteSelection(update, currentState);
            } else if (callbackData.startsWith("PREVIOUS_MONTHS_") || callbackData.startsWith("NEXT_MONTHS_")) {
                notificationTimePickerHandler.handleMonthChange(callbackData, chatId, update.getCallbackQuery().getMessage().getMessageId());
            } else if (callbackData.equals("ADD_NOTIFICATION")) {
                notificationTimePickerHandler.handleAddNotification(update);
            } else if (callbackData.equals("CANCEL_NOTIFICATION")) {
                allTasksCommandHandler.handle(update);
            } else if (callbackData.equals("continue_creation")) {
                newTaskCommandHandler.promptForNotificationDate(update.getCallbackQuery());
            } else if (callbackData.equals("save_task_now")) {
                newTaskCommandHandler.saveTaskAndNotifyUser(update.getCallbackQuery());
            } else if (callbackData.equals("APPLY_NO")) {
                messageService.sendMessage(chatId, "Ви вирішили не надсилати заявку.");
            } else if (callbackData.equals("APPLY_YES")) {
                adminHandler.processApplyRequest(chatId);
            } else if (callbackData.equals("PASTS")) {
                messageService.sendMessage(chatId, "Вибачте, але цей день уже пройшов. Будь ласка, виберіть поточний день або дату в майбутньому.");
            } else if (callbackData.equals("CREATE_TASK")) {
                newTaskCommandHandler.initiateNewTaskCreation(update.getCallbackQuery().getMessage().getChatId());
            } else if (callbackData.equals("MY_TASKS")) {
                allTasksCommandHandler.handle(update);
            } else if (callbackData.startsWith("DELETE_USER_")) {
                adminHandler.handleDeleteUser(callbackData, chatId);
            } else if (callbackData.startsWith("CHANGE_DESC_")) {
                descriptionUpdateHandler.handleDescriptionUpdate(callbackData, chatId);
            } else if (callbackData.startsWith("CHANGE_DATETIME_")) {
                dateTimeUpdateHandler.initiateDateTimeUpdate(update.getCallbackQuery(), Long.parseLong(callbackData.split("_")[2]));
            }
        }
    }

