package com.example.bot_diary.utilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class SchedulerUtil {

    @Autowired
    private TelegramLongPollingBot bot;

    @PostConstruct
    public void init() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "На початок."));
        listOfCommands.add(new BotCommand("/newtask", "Створити нову задачу."));
        listOfCommands.add(new BotCommand("/alltasks", "Усі задачі."));
        listOfCommands.add(new BotCommand("/done", "Виконані задачі."));
        listOfCommands.add(new BotCommand("/postponed", "Усі Відкладені задачі."));
        listOfCommands.add(new BotCommand("/calendar", "Мій календар"));

        try {
            bot.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }
}
