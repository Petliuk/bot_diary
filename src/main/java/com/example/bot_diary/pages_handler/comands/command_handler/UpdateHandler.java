package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.models.Notification;
import com.example.bot_diary.models.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UpdateHandler {

    @Autowired
    private MessageService messageService;

    public SendMessage sendTaskDetails(Task task, long chatId) {
        try {
            messageService.sendMessage(createMessageWithButtons(chatId, getDateInfo(task), createDateButtons(task)));
            messageService.sendMessage(createMessageWithButtons(chatId, getTimeInfo(task), createTimeButtons(task)));
            messageService.sendMessage(createMessageWithButtons(chatId, getDescription(task), createDescriptionButtons(task)));
            messageService.sendMessage(createMessageWithButtons(chatId, getNotificationsInfo(task), createNotificationButtons(task)));
        } catch (TelegramApiException e) {
            log.error("Failed to send task details: ", e);
        }
        return null;
    }

    private SendMessage createMessageWithButtons(long chatId, String info, List<List<InlineKeyboardButton>> buttons) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(buttons);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(info);
        message.setReplyMarkup(markupInline);

        return message;
    }

    public static String getDateInfo(Task task) {
        return "🗓Дата: " + (task.getDueDate() != null ? task.getDueDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "Дата не встановлена");
    }

    public static String getTimeInfo(Task task) {
        return "⏰Час: " + (task.getDueDate() != null ? task.getDueDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "Час не встановлений");
    }

    public static String getDescription(Task task) {
        return "📝Опис: " + (task.getDescription() != null && !task.getDescription().isEmpty() ? task.getDescription() : "Опис відсутній");
    }

    public static String getNotificationsInfo(Task task) {
        if (task.getNotifications() != null && !task.getNotifications().isEmpty()) {
            return "🔔Сповіщення: " + task.getNotifications().stream()
                    .map(notification -> notification.getNotificationTime() != null ?
                            notification.getNotificationTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) :
                            "Час сповіщення не встановлений")
                    .collect(Collectors.joining("\n"));
        }
        return "Сповіщення відсутні";
    }


    private static List<List<InlineKeyboardButton>> createDateButtons(Task task) {
        return Arrays.asList(Arrays.asList(
                createButton("🗓 Змінити дату", "CHANGE_DATE_" + task.getId()),
                createButton("🗑 Видалити дату", "DELETE_DATE_" + task.getId())
        ));
    }

    private static List<List<InlineKeyboardButton>> createTimeButtons(Task task) {
        return Arrays.asList(Arrays.asList(
                createButton("⏰ Змінити час", "CHANGE_TIME_" + task.getId()),
                createButton("🗑 Видалити час", "DELETE_TIME_" + task.getId())
        ));
    }

    private static List<List<InlineKeyboardButton>> createDescriptionButtons(Task task) {
        return Arrays.asList(Arrays.asList(
                createButton("✏️ Змінити опис", "CHANGE_DESC_" + task.getId()),
                createButton("🗑 Видалити опис", "DELETE_DESC_" + task.getId())
        ));
    }

    private static List<List<InlineKeyboardButton>> createNotificationButtons(Task task) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        if (task.getNotifications() != null && !task.getNotifications().isEmpty()) {
            for (Notification notification : task.getNotifications()) {
                buttons.add(Collections.singletonList(
                        createButton("🔔 Видалити сповіщення " + notification.getId(), "DELETE_NOTIF_" + notification.getId())
                ));
            }
        } else {
            buttons.add(Collections.singletonList(
                    createButton("🔔 Add Notification", "ADD_NOTIF_" + task.getId())
            ));
        }
        return buttons;
    }

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

}