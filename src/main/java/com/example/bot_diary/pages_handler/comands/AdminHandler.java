package com.example.bot_diary.pages_handler.comands;

import com.example.bot_diary.models.RegistrationRequest;
import com.example.bot_diary.models.User;
import com.example.bot_diary.models.UserStatus;
import com.example.bot_diary.pages_handler.comands.command_handler.MessageService;
import com.example.bot_diary.service.UserService;
import com.example.bot_diary.utilities.AdminButtons;
import com.example.bot_diary.utilities.ChecksForAccess;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

@Component
@Builder
@Slf4j
public class AdminHandler {

    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userRepository;
    @Autowired
    private ChecksForAccess checksForAccess;

    private List<RegistrationRequest> pendingRequests;

    public void handleAdminCommands(Update update) throws TelegramApiException {
        if ("/b".equals(update.getMessage().getText()) && isAdmin(update.getMessage().getChatId())) {
            showUnconfirmedUsers(update.getMessage().getChatId());
        }
    }

    private boolean isAdmin(Long chatId) {
        return chatId.equals(712909082L);
    }
    public void processApplyRequest(Long chatId) {
        RegistrationRequest request = new RegistrationRequest(chatId);
        addPendingRequest(request); // Додаємо запит у список очікуючих
        messageService.sendMessage(chatId, "Вашу заявку надіслано на розгляд адміністратору."); // Відправляємо повідомлення користувачу
    }
    public void addPendingRequest(RegistrationRequest request) {
        pendingRequests.add(request);
    }

    private void showUnconfirmedUsers(Long adminChatId) throws TelegramApiException {
        List<User> unconfirmedUsers = userService.findUsersByStatus(UserStatus.UNCONFIRMED);
        if (unconfirmedUsers.isEmpty()) {
            messageService.sendMessage(adminChatId, "Наразі немає непідтверджених користувачів.");
            return;
        }

        for (User user : unconfirmedUsers) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(adminChatId));
            message.setText("Користувач: " + user.getFirstName() + " " + user.getLastName());
            message.setReplyMarkup(AdminButtons.getConfirmUserMarkup(user.getChatId()));
            messageService.sendMessage(message);
        }
    }

    void confirmUser(Long userId, Long adminChatId) {
        Optional<User> userOptional = userService.findUserByChatId(userId);
        userOptional.ifPresent(user -> {
            user.setStatus(UserStatus.CONFIRMED);
            userService.saveUser(user);

            messageService.sendMessage(adminChatId, "Користувача підтверджено: " + user.getFirstName() + " " + user.getLastName());

            String userMessage = "🎉 Вашу заявку підтверджено!\n\n" +
                    "🆓 Ви отримуєте можливість користуватися ботом абсолютно безкоштовно протягом наступних 15 днів! Це чудова можливість ознайомитись із усіма його функціями та перевагами.\n\n" +
                    "💰 Після закінчення пробного періоду вартість користування ботом складатиме 100 грн на місяць. Ми завчасно нагадаємо вам про необхідність оплати, щоб ви могли без перерв продовжувати користуватись нашими послугами.\n\n" +
                    "Дякуємо, що вибрали наш сервіс! Якщо у вас виникнуть питання чи потрібна додаткова інформація, будь ласка, звертайтесь до підтримки.";
            messageService.sendMessage(userId, userMessage);
        });
    }

    public void processApplication(Long chatId, boolean accept, Update update) {
        Optional<User> userOptional = userService.findUserByChatId(chatId);
        userOptional.ifPresent(user -> {
            if (accept) {
                user.setStatus(UserStatus.CONFIRMED);
                userService.saveUser(user);
                messageService.sendMessage(chatId, "Вашу заявку на реєстрацію підтверджено.");
            } else {
                messageService.sendMessage(chatId, "Вашу заявку на реєстрацію відхилено.");
            }
        });
    }

    public void handleListUsersCommand(Update update) throws TelegramApiException {
        List<User> confirmedUsers = userService.findUsersByStatus(UserStatus.CONFIRMED);
        Long adminChatId = update.getMessage().getChatId();

        for (User user : confirmedUsers) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(adminChatId));
            message.setText("Користувач: " + user.getFirstName() + " " + user.getLastName());
            message.setReplyMarkup(AdminButtons.getUserListButtons(user.getChatId()));
            messageService.sendMessage(message);
        }
    }

    public void showBlockedUsers(Long adminChatId) throws TelegramApiException {
        List<User> blockedUsers = userService.findUsersByStatus(UserStatus.BLOCKED);
        if (blockedUsers.isEmpty()) {
            messageService.sendMessage(adminChatId, "Наразі немає заблокованих користувачів.");
            return;
        }

        for (User user : blockedUsers) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(adminChatId));
            message.setText("Заблокований користувач: " + user.getFirstName() + " " + user.getLastName());
            message.setReplyMarkup(AdminButtons.getBlockedUserButtons(user.getChatId()));
            messageService.sendMessage(message);
        }
    }

    public void handleDeleteUser(String callbackData, long chatId) {
        try {
            String userIdStr = callbackData.substring("DELETE_USER_".length());
            Long userId = Long.parseLong(userIdStr);
            checksForAccess.deleteUser(userId, chatId);
        } catch (NumberFormatException e) {
            messageService.sendMessage(chatId, "Помилка: неправильний формат ID користувача.");
            log.error("Error parsing user ID: ", e);
        }
    }
/*    public void handleDeleteUser(String callbackData, long chatId) {
        try {
            String userIdStr = callbackData.substring("DELETE_USER_".length());
            Long userId = Long.parseLong(userIdStr);
            Optional<User> userOptional = userService.findUserByChatId(userId);

            if (userOptional.isPresent()) {
                userService.deleteUser(userOptional.get());
                messageService.sendMessage(chatId, "Користувача видалено: " + userOptional.get().getFirstName() + " " + userOptional.get().getLastName());
            } else {
                messageService.sendMessage(chatId, "Помилка: користувач не знайдений.");
            }
        } catch (NumberFormatException e) {
            messageService.sendMessage(chatId, "Помилка: неправильний формат ID користувача.");
            log.error("Error parsing user ID: ", e);
        }
    }*/

}