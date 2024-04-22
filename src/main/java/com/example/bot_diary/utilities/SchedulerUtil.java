package com.example.bot_diary.utilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class SchedulerUtil {

    @Autowired
    private TelegramLongPollingBot bot;

    @PostConstruct
    public void init() {
        setCommandsForAllUsers();
        setCommandsForAdmin();
    }

    @Value("${admin.chat.id}")
    private Long adminChatId;

    private void setCommandsForAllUsers() {
        List<BotCommand> commandsForAllUsers = new ArrayList<>();
        commandsForAllUsers.add(new BotCommand("/start", "На початок."));
        commandsForAllUsers.add(new BotCommand("/newtask", "Створити нову задачу."));
        commandsForAllUsers.add(new BotCommand("/alltasks", "Усі задачі."));
        commandsForAllUsers.add(new BotCommand("/done", "Виконані задачі."));
        commandsForAllUsers.add(new BotCommand("/postponed", "Відкладені задачі."));
        commandsForAllUsers.add(new BotCommand("/calendar", "Календар"));
        commandsForAllUsers.add(new BotCommand("/help", "Отримати допомогу."));
        try {
            bot.execute(new SetMyCommands(commandsForAllUsers, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list for all users: " + e.getMessage());
        }
    }

    private void setCommandsForAdmin() {
        List<BotCommand> commandsForAdmin = new ArrayList<>();
        commandsForAdmin.add(new BotCommand("/start", "На початок."));
        commandsForAdmin.add(new BotCommand("/newtask", "Створити нову задачу."));
        commandsForAdmin.add(new BotCommand("/alltasks", "Усі задачі."));
        commandsForAdmin.add(new BotCommand("/done", "Виконані задачі."));
        commandsForAdmin.add(new BotCommand("/postponed", "Відкладені задачі."));
        commandsForAdmin.add(new BotCommand("/calendar", "Календар"));
        commandsForAdmin.add(new BotCommand("/b", "Заяви."));
        commandsForAdmin.add(new BotCommand("/users", "Користувачі"));
        commandsForAdmin.add(new BotCommand("/blockedusers", "Заблоковані Користувачі"));
        commandsForAdmin.add(new BotCommand("/help", "Отримати допомогу."));
        BotCommandScope scopeForAdmin = new BotCommandScopeChat(String.valueOf(adminChatId));



        try {
            bot.execute(new SetMyCommands(commandsForAdmin, scopeForAdmin, null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list for admin: " + e.getMessage());
        }
    }

}