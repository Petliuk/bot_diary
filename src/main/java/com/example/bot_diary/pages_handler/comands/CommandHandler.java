package com.example.bot_diary.pages_handler.comands;

import com.example.bot_diary.pages_handler.comands.command_handler.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class CommandHandler {

    @Autowired
    private StartCommandHandler startCommandHandler;

    @Autowired
    private NewTaskCommandHandler newTaskCommandHandler;

    @Autowired
    private CompletedTasksCommandHandler completedTasksCommandHandler;

    @Autowired
    private PostponedTasksCommandHandler postponedTasksCommandHandler;

    @Autowired
    private CalendarHandler calendarHandler;

    @Autowired
    private AllTasksCommandHandler allTasksCommandHandler;

  public void handleTextMessage(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        NewTaskCommandHandler.UserState currentState = newTaskCommandHandler.getUserStates().getOrDefault(chatId, NewTaskCommandHandler.UserState.NONE);

        switch (messageText) {
            case "/postponed":
                postponedTasksCommandHandler.handle(update);
                break;
            case "/newtask":
                newTaskCommandHandler.handle(update);
                break;
            case "/done":
                completedTasksCommandHandler.handle(update);
                break;
            case "/alltasks":
                allTasksCommandHandler.handle(update);
                break;
            case "/start":
                startCommandHandler.handle(update);
                break;
         /*   case "/help":
                messageService.sendHelpMessage(chatId);
                break;*/
            case "/calendar":
                calendarHandler.handleCalendarCommand(update);
                break;
            default:
                if (currentState == NewTaskCommandHandler.UserState.AWAITING_TASK_DESCRIPTION && !messageText.startsWith("/")) {
                    newTaskCommandHandler.handle(update);
                }
                break;
        }
    }

}
