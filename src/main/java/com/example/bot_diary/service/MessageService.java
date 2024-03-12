package com.example.bot_diary.service;

import com.example.bot_diary.pages_handler.comands.BotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MessageService {


    static final String NO_BUTTON = "NO_BUTTON";
    static final String YES_BUTTON = "YES_BUTTON";

    static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";



    private final BotService command; // Змінено на інтерфейс BotService

    @Autowired
    public MessageService(@Lazy BotService command) {
        this.command = command;
    }

    public void sendStartMessage(long chatId, String name) {
        String messageText = "Welcome " + name;
        command.sendMessage(chatId, messageText); // Використання BotService замість прямого доступу до TelegramLongPollingBot
    }


  public void sendHelpMessage(long chatId) {
        sendMessage(chatId, HELP_TEXT);
    }

    public void sendRegisterMessage(long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want to register?");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData(YES_BUTTON);

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);

        rowInline.add(yesButton);
        rowInline.add(noButton);
        rowsInline.add(rowInline);

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);

        command.sendMessage(message); // Використання методу sendMessage з об'єктом SendMessage
    }

   /* SendMessage message = new SendMessage();
      message.setChatId(String.valueOf(chatId));
      message.setText("Do you relly want to register?");

    InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();  // Кнопки які будуть відображатись після команти /register
    List<List< InlineKeyboardButton>> rowsInLine = new ArrayList<>(); // створюэмо рядки кнопок
    List<InlineKeyboardButton> rowInLine = new ArrayList<>();  //Створюємо кнопки для першого ряду.

    var yesButton = new InlineKeyboardButton();  // Створення першої кнопки
        yesButton.setText("Yes");          // Текст який буде в перший енопці
        yesButton.setCallbackData(YES_BUTTON); // Ідентифікатор який дає зрозуміти яка кнопка була нажата.

    var noButton = new InlineKeyboardButton(); // Створення другої кнопки
        noButton.setText("No");      // Текст який буде в другій кнопці
        noButton.setCallbackData(NO_BUTTON); // Ідентифікатор який дає зрозуміти яка кнопка була нажата.

        rowInLine.add(yesButton);                   // В порядку якому ми додаэмо кнопки в тому порядку будуть выдображатись кнопки.
        rowInLine.add(noButton);                    // Додаэмо кнопки в перший рядок

        rowsInLine.add(rowInLine);                  // Додаємо кнопки в список

        markupInLine.setKeyboard(rowsInLine);

        message.setReplyMarkup(markupInLine);

    executeMessage(message);

}*/


    public void sendUnknownCommandMessage(long chatId) {
        sendMessage(chatId, "Sorry, but that command does not exist");
    }

    public void handleCallbackQuery(String callbackData, long chatId, long messageId) throws TelegramApiException {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId((int) messageId);

        if (callbackData.equals(YES_BUTTON)) {
            editMessageText.setText("You pressed YES");
        } else if (callbackData.equals(NO_BUTTON)) {
            editMessageText.setText("You pressed NO");
        }

        command.editMessage(editMessageText);
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        command.sendMessage(chatId, text);
    }
}

