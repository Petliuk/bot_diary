package com.example.bot_diary.bot;

import com.example.bot_diary.configuration.BotConfig;
import com.example.bot_diary.pages_handler.comands.CallbackQueryHandler;
import com.example.bot_diary.pages_handler.comands.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot{

    @Autowired
    private BotConfig config;

    @Autowired
    private CommandHandler commandHandler;

    @Autowired
    private CallbackQueryHandler callbackQueryHandler;


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

/*    @Override
    public void onRegister() {
        super.onRegister();
    }*/

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                commandHandler.handleTextMessage(update);
            } else if (update.hasCallbackQuery()) {
                callbackQueryHandler.handleCallbackQuery(update);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

/*    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }*/

}



