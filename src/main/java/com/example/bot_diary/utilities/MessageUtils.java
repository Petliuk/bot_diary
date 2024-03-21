package com.example.bot_diary.utilities;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.pages_handler.comands.BotService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MessageUtils {

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
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public static void sendTaskMessages(List<Task> tasks, long chatId, BotService botService) throws TelegramApiException {
        for (Task task : tasks) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            buttons.add(createButton("Видалити", "DELETE_TASK_" + task.getId()));
            buttons.add(createButton("Виконано", "DONE_TASK_" + task.getId()));
            buttons.add(createButton("Відкласти", "POSTPONE_TASK_" + task.getId()));

            SendMessage message = createTaskMessage(task, chatId, buttons);
            botService.sendMessage(message);
        }
    }

    private static String formatTaskText(Task task) {
        String dateText = task.getDueDate() != null ?
                "🗓 Дата: " + task.getDueDate().toLocalDate() + "\n" +
                        "⏰ Час: " + task.getDueDate().toLocalTime() + "\n" : "Дата і час не вказані.\n";
        return dateText + "🔖 Статус: " + task.getStatus().getDisplayName() + "\n" +
                "📝 Опис: " + task.getDescription() + "\n";
    }

    public static void sendTasksWithCustomButtons(List<Task> tasks, long chatId, BotService botService, Function<Task, List<InlineKeyboardButton>> buttonsProvider) throws TelegramApiException {
        for (Task task : tasks) {
            List<InlineKeyboardButton> buttons = buttonsProvider.apply(task);
            SendMessage message = createTaskMessage(task, chatId, buttons);
            botService.sendMessage(message);
        }
    }
}