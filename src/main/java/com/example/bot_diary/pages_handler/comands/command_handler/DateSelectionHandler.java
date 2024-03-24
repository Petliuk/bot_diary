package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DateSelectionHandler {
    
    private final TaskService taskService;
    private final NewTaskCommandHandler newTaskCommandHandler;
    private final CalendarHandler calendarHandler;
    private final MessageService messageService;

    public void handleDaySelection(Update update, NewTaskCommandHandler.UserState currentState) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int dayOfMonth = Integer.parseInt(callbackData.substring(3));
        YearMonth selectedMonth = calendarHandler.selectedYearMonths.getOrDefault(chatId, YearMonth.now());

        LocalDate selectedDate = selectedMonth.atDay(dayOfMonth);
        List<Task> tasksForDay = taskService.findTasksForDay(selectedDate, chatId);

        if (!tasksForDay.isEmpty() || currentState == NewTaskCommandHandler.UserState.AWAITING_NOTIFICATION_DATE) {
            if (currentState == NewTaskCommandHandler.UserState.AWAITING_NOTIFICATION_DATE) {
                newTaskCommandHandler.saveTaskWithNotificationDate(update.getCallbackQuery());
            } else {

               showTasksForSelectedDay(chatId, selectedDate, tasksForDay);
            }

        } else {
            messageService.sendMessage(chatId, "Завдань не існує");
        }
    }

    public void showTasksForSelectedDay(long chatId, LocalDate selectedDate, List<Task> tasksForDay) throws TelegramApiException {
        if (tasksForDay.isEmpty()) {
            messageService.sendMessage(chatId, "На цей день задачі відсутні.");
        } else {
            for (Task task : tasksForDay) {
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();

                InlineKeyboardButton deleteButton = new InlineKeyboardButton();
                deleteButton.setText("Видалити");
                deleteButton.setCallbackData("DELETE_TASK_" + task.getId());
                rowInline.add(deleteButton);


                InlineKeyboardButton doneButton = new InlineKeyboardButton();
                doneButton.setText("Виконано");
                doneButton.setCallbackData("DONE_TASK_" + task.getId());
                rowInline.add(doneButton);


                InlineKeyboardButton postponeButton = new InlineKeyboardButton();
                postponeButton.setText("Відкласти");
                postponeButton.setCallbackData("POSTPONE_TASK_" + task.getId());
                rowInline.add(postponeButton);


                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);


                String messageText = "🗓 Дата: " + task.getDueDate().toLocalDate() + "\n" +
                        "⏰ Час: " + task.getDueDate().toLocalTime() + "\n" +
                        "🔖 Статус: " + getTaskStatusText(task.getStatus()) + "\n" +
                        "📝 Опис: " + task.getDescription() + "\n";

                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText(messageText);
                message.setReplyMarkup(markupInline);
                messageService.sendMessage(message);
            }
        }
    }

    private String getTaskStatusText(TaskStatus status) {
        switch (status) {
            case NOT_COMPLETED:
                return "Не виконано";
            case COMPLETED:
                return "Виконано";
            case POSTPONED:
                return "Відкладено";
            default:
                return "Невідомий статус";
        }
    }
}
