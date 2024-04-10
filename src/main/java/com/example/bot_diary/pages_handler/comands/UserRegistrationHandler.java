package com.example.bot_diary.pages_handler.comands;

import com.example.bot_diary.models.User;
import com.example.bot_diary.pages_handler.comands.command_handler.MessageService;
import com.example.bot_diary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserRegistrationHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    public void handleUserRegistration(Update update) throws TelegramApiException {

        String firstName = null;
        String lastName = null;
        String userName = null;
        firstName = update.getMessage().getFrom().getFirstName();
        lastName = update.getMessage().getFrom().getLastName();
        userName = update.getMessage().getFrom().getUserName();


        Long chatId = update.getMessage().getChatId();
        Optional<User> userOptional = userService.findUserByChatId(chatId);
        if (userOptional.isPresent()) {
            sendMainMenu(chatId);
        } else {
            User newUser = User.builder()
                    .chatId(chatId)
                    .status(AdminHandler.UserStatus.UNCONFIRMED) // Явне встановлення статусу
                    .registerAt(LocalDateTime.now())
                    .userName(userName)
                    .lastName(lastName)
                    .firstName(firstName)
                    .build();
            userService.saveUser(newUser);

            sendRegistrationRequest(chatId);
        }
    }

    private void sendRegistrationRequest(Long chatId) throws TelegramApiException {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton createUserYesButton = new InlineKeyboardButton();
        createUserYesButton.setText("Так");
        createUserYesButton.setCallbackData("APPLY_YES");

        InlineKeyboardButton createUserNoButton = new InlineKeyboardButton();
        createUserNoButton.setText("Ні");
        createUserNoButton.setCallbackData("APPLY_NO");


        rowInline.add(createUserYesButton);
        rowInline.add(createUserNoButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Бажаєте надіслати заявку на користування цим ботом?");
        message.setReplyMarkup(markupInline);

        messageService.sendMessage(message);
    }


    private void sendMainMenu(Long chatId) throws TelegramApiException {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        InlineKeyboardButton createTaskButton = new InlineKeyboardButton();
        createTaskButton.setText("Створити задачу");
        createTaskButton.setCallbackData("CREATE_TASK");
        rowInline1.add(createTaskButton);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton viewTasksButton = new InlineKeyboardButton();
        viewTasksButton.setText("Мої завдання");
        viewTasksButton.setCallbackData("MY_TASKS");
        rowInline2.add(viewTasksButton);



        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Виберіть опцію:");
        message.setReplyMarkup(markupInline);

        messageService.sendMessage(message);
    }
}