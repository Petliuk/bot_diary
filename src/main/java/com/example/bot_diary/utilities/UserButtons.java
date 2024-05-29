package com.example.bot_diary.utilities;

import com.example.bot_diary.models.Notification;
import com.example.bot_diary.models.Task;
import com.example.bot_diary.pages_handler.comands.BotService;
import com.example.bot_diary.pages_handler.comands.command_handler.MessageService;
import com.example.bot_diary.service.NotificationService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UserButtons {

    public static SendMessage createTaskMessage(Task task, long chatId, List<InlineKeyboardButton> buttons) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(buttons);
        markupInline.setKeyboard(rowsInline);

        String messageText = formatTaskText(task);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        message.setReplyMarkup(markupInline);

        return message;
    }

    public static InlineKeyboardButton createButton(String text, String callbackData) {
        if (text == null || text.isEmpty()) throw new IllegalArgumentException("Button text cannot be null or empty");
        if (callbackData == null || callbackData.isEmpty()) throw new IllegalArgumentException("Callback data cannot be null or empty");

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public static void sendTaskMessagesWithNotifications(List<Task> tasks, long chatId, MessageService messageService, NotificationService notificationService) throws TelegramApiException {
        for (Task task : tasks) {
            List<Notification> notifications = notificationService.findNotificationsByTaskId(task.getId());

            List<InlineKeyboardButton> firstRowButtons = new ArrayList<>();
            firstRowButtons.add(createButton("Видалити", "DELETE_TASK_" + task.getId()));
            firstRowButtons.add(createButton("Виконано", "DONE_TASK_" + task.getId()));
            firstRowButtons.add(createButton("Відкласти", "POSTPONE_TASK_" + task.getId()));

            List<InlineKeyboardButton> secondRowButtons = new ArrayList<>();
            secondRowButtons.add(createButton("Змінити", "UPDATE_TASK_" + task.getId()));
            secondRowButtons.add(createButton("Додати сповіщення", "ADD_NOTIFICATION_TASK_" + task.getId()));

            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            rows.add(firstRowButtons);
            rows.add(secondRowButtons);

            String notificationDetails = formatNotificationDetails(notifications);
            SendMessage message = createTaskMessageWithNotifications(task, chatId, rows, notificationDetails);
            messageService.sendMessage(message);
        }
    }

    public static InlineKeyboardMarkup createTaskButtons(Long taskId) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        rowInline.add(createButton("Видалити", "DELETE_TASK_" + taskId));
        rowInline.add(createButton("Виконано", "DONE_TASK_" + taskId));
        rowInline.add(createButton("Відкласти", "POSTPONE_TASK_" + taskId));

        rowsInline.add(rowInline);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    private static String formatTaskText(Task task) {
        String dateText = task.getDueDate() != null ?
                "🗓 Дата: " + task.getDueDate().toLocalDate() + "\n" +
                        "⏰ Час: " + task.getDueDate().toLocalTime() + "\n" : "Дата і час не вказані.\n";
        return dateText + "🔖 Статус: " + task.getStatus().getDisplayName() + "\n" +
                "📝 Опис: " + task.getDescription() + "\n";
    }

    private static String formatNotificationDetails(List<Notification> notifications) {
        StringBuilder details = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Notification notification : notifications) {
            String date = notification.getNotificationTime().toLocalDate().format(dateFormatter);
            String time = notification.getNotificationTime().toLocalTime().format(timeFormatter);
            details.append(String.format("🔔 Сповіщення: %s о %s\n", date, time));
        }
        return details.toString();
    }

    public static SendMessage createTaskMessageWithNotifications(Task task, long chatId, List<List<InlineKeyboardButton>> rows, String notificationDetails) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rows);

        String messageText = formatTaskText(task) + (!notificationDetails.isEmpty() ? "\nСповіщення:\n" + notificationDetails : "");

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        message.setReplyMarkup(markupInline);

        return message;
    }

    public static void sendTasksWithCustomButtons(List<Task> tasks, long chatId, BotService botService, Function<Task, List<InlineKeyboardButton>> buttonsProvider) throws TelegramApiException {
        for (Task task : tasks) {
            List<InlineKeyboardButton> buttons = buttonsProvider.apply(task);
            SendMessage message = createTaskMessage(task, chatId, buttons);
            botService.sendMessage(message);
        }
    }

    public static List<List<InlineKeyboardButton>> createConfirmationButtons(String yesText, String yesCallbackData, String noText, String noCallbackData) {
        InlineKeyboardButton yesButton = createButton(yesText, yesCallbackData);
        InlineKeyboardButton noButton = createButton(noText, noCallbackData);

        List<InlineKeyboardButton> buttonsRow = new ArrayList<>();
        buttonsRow.add(yesButton);
        buttonsRow.add(noButton);

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        buttons.add(buttonsRow);

        return buttons;
    }
}