package com.example.bot_diary.pages_handler.comands;

import com.example.bot_diary.models.RegistrationRequest;
import com.example.bot_diary.models.User;
import com.example.bot_diary.pages_handler.comands.command_handler.*;
import com.example.bot_diary.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private  UserService userService;


    public void handleCallbackQuery(Update update) throws TelegramApiException {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();
        Optional<User> userOptional = userService.findUserByChatId(chatId);

        NewTaskCommandHandler.UserState currentState = newTaskCommandHandler.getUserStates().getOrDefault(chatId, NewTaskCommandHandler.UserState.NONE);
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        // Якщо користувач не адміністратор і його статус UNCONFIRMED

        if (!(chatId == 712909082L) && userOptional.isPresent() && userOptional.get().getStatus() == AdminHandler.UserStatus.UNCONFIRMED) {
            messageService.sendMessage(chatId, "Вашу заявку ще не підтверджено. Будь ласка, зачекайте, поки адміністратор перевірить вашу заявку.");
            return; // Вихід, щоб не обробляти інші команди
        }

        switch (callbackData) {
            case "APPLY_YES":
                RegistrationRequest request = new RegistrationRequest(chatId);
                adminHandler.addPendingRequest(request);
                messageService.sendMessage(chatId, "Вашу заявку надіслано на розгляд адміністратору.");
                break;
            case "APPLY_NO":
                messageService.sendMessage(chatId, "Ви вирішили не надсилати заявку.");
                break;
            // Інші випадки обробки callbackData
        }

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
        } else if (callbackData.startsWith("PREVIOUS_MONTH_") || callbackData.startsWith("NEXT_MONTH_")) {
            calendarHandler.handleMonthChange(callbackData, chatId, messageId);
        } else if ("continue_creation".equals(callbackData)) {
            newTaskCommandHandler.promptForNotificationDate(update.getCallbackQuery());
        } else if (callbackData.startsWith("DAY")) {
            dateSelectionHandler.handleDaySelection(update, currentState);
        } else if (callbackData.startsWith("HOUR_")) {
            timePickerHandler.handleHourSelection(update);
        } else if (callbackData.startsWith("MINUTE_")) {
            timePickerHandler.handleMinuteSelection(update);
        } else if (callbackData.startsWith("PAST")) {
            messageService.sendMessage(chatId, "Вибачте, але цей день уже пройшов. Будь ласка, виберіть поточний день або дату в майбутньому.");
        } else if (callbackData.startsWith("PAST_HOUR") || callbackData.startsWith("PAST_MINUTE")) {
            messageService.sendMessage(chatId, "Цей час уже пройшов і ви його не можете вибрати. Будь ласка, виберіть час, який ще не настав.");
        } else  if (callbackData.startsWith("ACCEPT_")) {
            Long applicantChatId = Long.parseLong(callbackData.split("_")[1]);
            adminHandler.processApplication(applicantChatId, true, update);
        } else if (callbackData.startsWith("REJECT_")) {
            Long applicantChatId = Long.parseLong(callbackData.split("_")[1]);
            adminHandler.processApplication(applicantChatId, false, update);
        } else if (callbackData.startsWith("CONFIRM_")) {
            Long userId = Long.valueOf(callbackData.split("_")[1]);
            adminHandler.confirmUser(userId, chatId);
        } else   if (callbackData.startsWith("BLOCK_")) {
            Long userId = Long.valueOf(callbackData.split("_")[1]);
            blockUser(userId);
        } else if (callbackData.startsWith("UNBLOCK_")) {
            Long userId = Long.valueOf(callbackData.split("_")[1]);
            unblockUser(userId);
        } else if (callbackData.startsWith("DELETE_USER_")) {
            Long userId = Long.valueOf(callbackData.split("_")[1]);
            deleteUser(userId, chatId);
        }


    }
    private void blockUser(Long userId) throws TelegramApiException {
        userService.findUserByChatId(userId).ifPresent(user -> {
            user.setStatus(AdminHandler.UserStatus.BLOCKED);
            userService.saveUser(user);

            // Відправте повідомлення адміністратору про блокування користувача
            String adminMessage = String.format("Користувача %s %s (ID: %d) заблоковано.",
                    user.getFirstName(), user.getLastName(), user.getChatId());
            messageService.sendMessage(712909082L, adminMessage);

            // Відправте повідомлення користувачу про його блокування
            String userMessage = "Ви були заблоковані та більше не маєте доступу до боту.";
            messageService.sendMessage(user.getChatId(), userMessage);
        });
    }
    private void unblockUser(Long userId) throws TelegramApiException {
        userService.findUserByChatId(userId).ifPresent(user -> {
            user.setStatus(AdminHandler.UserStatus.CONFIRMED);
            userService.saveUser(user);

            // Відправте повідомлення адміністратору про розблокування користувача
            String adminMessage = String.format("Користувача %s %s (ID: %d) розблоковано.",
                    user.getFirstName(), user.getLastName(), user.getChatId());
            messageService.sendMessage(712909082L, adminMessage);

            // Відправте повідомлення користувачу про відновлення доступу
            String userMessage = "Ваш доступ до боту було відновлено.";
            messageService.sendMessage(user.getChatId(), userMessage);
        });
    }

    private void deleteUser(Long userId, Long adminChatId) throws TelegramApiException {
        userService.findUserByChatId(userId).ifPresent(user -> {
            userService.deleteUser(user); // Вам потрібно буде реалізувати цей метод в UserService
            messageService.sendMessage(adminChatId, "Користувача видалено: " + user.getFirstName() + " " + user.getLastName());
        });
    }

}
