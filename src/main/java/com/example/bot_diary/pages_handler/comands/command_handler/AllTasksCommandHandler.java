package com.example.bot_diary.pages_handler.comands.command_handler;
import com.example.bot_diary.service.MessageService;
import com.example.bot_diary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class RegistrationCommandHandler {

    private final UserService userService;
    private final MessageService messageService;

    @Autowired
    public RegistrationCommandHandler(UserService userService, MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    public void handle(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        messageService.sendRegisterMessage(chatId);
    }
}