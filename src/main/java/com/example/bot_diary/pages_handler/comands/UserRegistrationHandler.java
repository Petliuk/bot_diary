package com.example.bot_diary.pages_handler.comands;

import com.example.bot_diary.models.User;
import com.example.bot_diary.models.UserStatus;
import com.example.bot_diary.pages_handler.comands.command_handler.MessageService;
import com.example.bot_diary.service.UserService;
import com.example.bot_diary.utilities.AdminButtons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class UserRegistrationHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Value("${admin.chat.id}")
    private Long adminChatId;

    public void handleUserRegistration(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        String firstName = update.getMessage().getFrom().getFirstName();
        String lastName = update.getMessage().getFrom().getLastName();
        String userName = update.getMessage().getFrom().getUserName();

        Optional<User> userOptional = userService.findUserByChatId(chatId);
        if (userOptional.isPresent()) {
            sendMainMenu(chatId);
        } else {
            User newUser = User.builder()
                    .chatId(chatId)
                    .status(UserStatus.UNCONFIRMED)
                    .registerAt(LocalDateTime.now())
                    .userName(userName)
                    .lastName(lastName)
                    .firstName(firstName)
                    .build();
            userService.saveUser(newUser);
            sendRegistrationRequest(chatId);
            notifyAdminAboutNewUser(newUser);
        }
    }

    private void sendRegistrationRequest(Long chatId) throws TelegramApiException {
        InlineKeyboardMarkup markup = AdminButtons.getRegistrationRequestMarkup();
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Бажаєте надіслати заявку на користування цим ботом?");
        message.setReplyMarkup(markup);
        messageService.sendMessage(message);
    }

    void sendMainMenu(Long chatId) throws TelegramApiException {
        InlineKeyboardMarkup markup = AdminButtons.getMainMenuMarkup();
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Виберіть опцію:");
        message.setReplyMarkup(markup);
        messageService.sendMessage(message);
    }

    private void notifyAdminAboutNewUser(User user) throws TelegramApiException {
        String adminMessage = String.format("Нова заявка на реєстрацію від %s %s (ID: %d).", user.getFirstName(), user.getLastName(), user.getChatId());
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(adminChatId));
        message.setText(adminMessage);
        messageService.sendMessage(message);
    }

}