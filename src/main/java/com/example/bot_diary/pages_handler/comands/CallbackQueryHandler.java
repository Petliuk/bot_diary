package com.example.bot_diary.pages_handler.comands;

import com.example.bot_diary.pages_handler.comands.command_handler.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class CallbackQueryHandler {

    @Autowired
    CompletedTasksCommandHandler completedTasksCommandHandler;

    @Autowired
    TimePickerHandler timePickerHandler;

    @Autowired
    PostponedTasksCommandHandler postponedTasksCommandHandler;

    @Autowired
    private NewTaskCommandHandler newTaskCommandHandler;

    @Autowired
    private AllTasksCommandHandler allTasksCommandHandler;

    @Autowired
    private CalendarHandler calendarHandler;

    @Autowired
    private DeleteTasksCommandHandler deleteTasksCommandHandler;

    @Autowired
    private DateSelectionHandler dateSelectionHandler;

    public void handleCallbackQuery(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        NewTaskCommandHandler.UserState currentState = newTaskCommandHandler.getUserStates().getOrDefault(chatId, NewTaskCommandHandler.UserState.NONE);
        int messageId = update.getCallbackQuery().getMessage().getMessageId();


        if (StartCommandHandler.CREATE_TASK.equals(callbackData)) {
            newTaskCommandHandler.initiateNewTaskCreation(update.getCallbackQuery().getMessage().getChatId());
        } else if (callbackData.startsWith("MY_TASKS")) {
            allTasksCommandHandler.handle(update);
        } else if (callbackData.startsWith("DELETE_TASK_")) {
            deleteTasksCommandHandler.handle(update);
        } else if (callbackData.startsWith("REVERT_TASK_") || callbackData.startsWith("DONE_TASK_")) {
            completedTasksCommandHandler.handleCallbackQuery(update);
        } else if (callbackData.startsWith("POSTPONE_TASK_")) {
            postponedTasksCommandHandler.handleCallbackQuery(update);
        } else if ("save_task".equals(callbackData)) {
            newTaskCommandHandler.saveTaskAndNotifyUser(update.getCallbackQuery());
        } else if (callbackData.startsWith("PREVIOUS_MONTH_") || callbackData.startsWith("NEXT_MONTH_")) {
            calendarHandler.handleMonthChange(callbackData, chatId, messageId);
        } else if ("continue_creation".equals(callbackData)) {
            newTaskCommandHandler.promptForNotificationDate(update.getCallbackQuery());
        } else if (callbackData.startsWith("DAY")) {
            dateSelectionHandler.handleDaySelection(update, currentState);
        } else if (callbackData.startsWith("HOUR_")) {
            timePickerHandler.handleHourSelection(update);
        } else if (callbackData.startsWith("MINUTE_")) {
            timePickerHandler.handleMinuteSelection(update);
        }
    }

}
