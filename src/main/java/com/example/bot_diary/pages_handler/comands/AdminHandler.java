package com.example.bot_diary.pages_handler.comands;

import com.example.bot_diary.models.RegistrationRequest;
import com.example.bot_diary.models.User;
import com.example.bot_diary.pages_handler.comands.command_handler.MessageService;
import com.example.bot_diary.service.UserService;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Builder
public class AdminHandler {

    public enum UserStatus {
        UNCONFIRMED, // Заявка не підтверджена
        CONFIRMED,// Заявка підтверджена
        BLOCKED // Користувач заблокований
    }


    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userRepository;

    private List<RegistrationRequest> pendingRequests = new ArrayList<>();

    public void handleAdminCommands(Update update) throws TelegramApiException {
        if ("/b".equals(update.getMessage().getText()) && isAdmin(update.getMessage().getChatId())) {
            showUnconfirmedUsers(update.getMessage().getChatId());
        }
    }

    private boolean isAdmin(Long chatId) {
        return chatId.equals(712909082L);
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
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();

            InlineKeyboardButton confirmButton = new InlineKeyboardButton();
            confirmButton.setText("Підтвердити");
            confirmButton.setCallbackData("CONFIRM_" + user.getChatId());

            rowInline.add(confirmButton);
            rowsInline.add(rowInline);
            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(adminChatId));
            message.setText("Користувач: " + user.getFirstName() + " " + user.getLastName());
            message.setReplyMarkup(markupInline);

            messageService.sendMessage(message);
        }
    }


    void confirmUser(Long userId, Long adminChatId) {
        Optional<User> userOptional = userService.findUserByChatId(userId);
        userOptional.ifPresent(user -> {
            user.setStatus(AdminHandler.UserStatus.CONFIRMED);  // Оновлення статусу користувача
            userService.saveUser(user);  // Збереження оновленого користувача

            // Надсилання повідомлень про підтвердження
            messageService.sendMessage(adminChatId, "Користувача підтверджено: " + user.getFirstName() + " " + user.getLastName());
            messageService.sendMessage(userId, "Вашу заявку підтверджено. Тепер ви можете користуватися ботом.");
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
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();

            // Кнопка для блокування користувача
            InlineKeyboardButton blockButton = new InlineKeyboardButton();
            blockButton.setText("Блокувати");
            blockButton.setCallbackData("BLOCK_" + user.getChatId());
            rowInline.add(blockButton);

/*
            // Кнопка для розблокування користувача
            InlineKeyboardButton unblockButton = new InlineKeyboardButton();
            unblockButton.setText("Розблокувати");
            unblockButton.setCallbackData("UNBLOCK_" + user.getChatId());
            rowInline.add(unblockButton);
*/

            InlineKeyboardButton deleteButton = new InlineKeyboardButton();
            deleteButton.setText("Видалити");
            deleteButton.setCallbackData("DELETE_USER_" + user.getChatId());
            rowInline.add(deleteButton);



            rowsInline.add(rowInline);
            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(adminChatId));
            message.setText("Користувач: " + user.getFirstName() + " " + user.getLastName());
            message.setReplyMarkup(markupInline);

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
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();

            InlineKeyboardButton unblockButton = new InlineKeyboardButton();
            unblockButton.setText("Розблокувати");
            unblockButton.setCallbackData("UNBLOCK_" + user.getChatId());
            rowInline.add(unblockButton);

            InlineKeyboardButton deleteButton = new InlineKeyboardButton();
            deleteButton.setText("Видалити");
            deleteButton.setCallbackData("DELETE_USER_" + user.getChatId());
            rowInline.add(deleteButton);

            rowsInline.add(rowInline);
            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(adminChatId));
            message.setText("Заблокований користувач: " + user.getFirstName() + " " + user.getLastName());
            message.setReplyMarkup(markupInline);

            messageService.sendMessage(message);
        }
    }
}
