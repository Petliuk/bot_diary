package com.example.bot_diary.pages_handler.comands;

/*import com.example.bot_diary.configuration.BotConfig;
import com.example.bot_diary.pages_handler.comands.command_handler.*;
import com.example.bot_diary.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component*/
public class BotCommandHandler {/* implements CommandHandler  {

    private final BotConfig config;
    private final CompletedTasksCommandHandler completedTasksCommandHandler;
    private final TimePickerHandler timePickerHandler;
    private final PostponedTasksCommandHandler postponedTasksCommandHandler;
    private final StartCommandHandler startCommandHandler;
    private final NewTaskCommandHandler newTaskCommandHandler;
    private final AllTasksCommandHandler allTasksCommandHandler;
    private final CalendarHandler calendarHandler;
    private final DeleteTasksCommandHandler deleteTasksCommandHandler;
    private final MessageService messageService;

    @Autowired
    public BotCommandHandler(BotConfig config, CompletedTasksCommandHandler completedTasksCommandHandler, TimePickerHandler timePickerHandler, PostponedTasksCommandHandler postponedTasksCommandHandler, StartCommandHandler startCommandHandler, NewTaskCommandHandler newTaskCommandHandler, AllTasksCommandHandler allTasksCommandHandler, CalendarHandler calendarHandler, DeleteTasksCommandHandler deleteTasksCommandHandler, MessageService messageService) {
        this.config = config;
        this.completedTasksCommandHandler = completedTasksCommandHandler;
        this.timePickerHandler = timePickerHandler;
        this.postponedTasksCommandHandler = postponedTasksCommandHandler;
        this.startCommandHandler = startCommandHandler;
        this.newTaskCommandHandler = newTaskCommandHandler;
        this.allTasksCommandHandler = allTasksCommandHandler;
        this.calendarHandler = calendarHandler;
        this.deleteTasksCommandHandler = deleteTasksCommandHandler;
        this.messageService = messageService;
    }
    @Override
    public void handle(Update update) throws TelegramApiException {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            switch (messageText) {
                case "/time":
                    // Викликаємо метод для створення і відправлення повідомлення з годинником
                    SendMessage timePickerMessage = timePickerHandler.createHourPickerMessage(chatId);
                    execute(timePickerMessage);
                    break;
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
                    handleCommand(update, messageText);
                    break;
                case "/start":
                    startCommandHandler.handle(update);
                    break;
                case "/help":
                    messageService.sendHelpMessage(chatId);
                    break;
                default:
                    if (currentState == NewTaskCommandHandler.UserState.AWAITING_TASK_DESCRIPTION && !messageText.startsWith("/")) {
                        newTaskCommandHandler.handle(update);
                    }
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            // Логіка обробки колбеків (кнопок)
        }
    }*/

}
