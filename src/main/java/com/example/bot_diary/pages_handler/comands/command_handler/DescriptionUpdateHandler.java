package com.example.bot_diary.pages_handler.comands.command_handler;

import com.example.bot_diary.models.Notification;
import com.example.bot_diary.models.Task;
import com.example.bot_diary.service.NotificationService;
import com.example.bot_diary.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class DescriptionUpdateHandler {

    @Autowired
    private TaskService taskService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    private enum DescriptionUpdateState {
        AWAITING_NEW_DESCRIPTION,
        IDLE // Default state
    }

    private Map<Long, Long> editSessions = new HashMap<>();
    private Map<Long, DescriptionUpdateState> userStates = new HashMap<>();

    public void initiateDescriptionChange(long chatId, long taskId) {
        editSessions.put(chatId, taskId);
        userStates.put(chatId, DescriptionUpdateState.AWAITING_NEW_DESCRIPTION);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Напишіть новий опис задачі.");
        try {
            messageService.sendMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void updateDescription(long chatId, String newDescription) {
        if (userStates.getOrDefault(chatId, DescriptionUpdateState.IDLE) == DescriptionUpdateState.AWAITING_NEW_DESCRIPTION) {
            Long taskId = editSessions.get(chatId);
            if (taskId != null) {
                Task task = taskService.findTaskById(taskId);
                if (task != null) {
                    task.setDescription(newDescription);
                    taskService.saveTask(task);
                    messageService.sendMessage(chatId, "Ваша задача оновлена.");
                } else {
                    messageService.sendMessage(chatId, "Задача не знайдена.");
                }
                clearUserState(chatId);
            }
        }
    }

    private void clearUserState(long chatId) {
        editSessions.remove(chatId);
        userStates.remove(chatId);
    }

    public boolean isAwaitingDescription(long chatId) {
        return userStates.getOrDefault(chatId, DescriptionUpdateState.IDLE) == DescriptionUpdateState.AWAITING_NEW_DESCRIPTION;
    }

    public void deleteNotification(Update update, String callbackData) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long notificationId = Long.parseLong(callbackData.split("_")[2]);
        Optional<Notification> notification = notificationService.findNotificationById(notificationId);
        System.out.println("DescriptionUpdateHandler notificationId  =  " + notification);
        if (notification.isPresent()) {
            notificationService.deleteNotification(notificationId);
            messageService.sendMessage(chatId, "Сповіщення видалено.");
        } else {
            messageService.sendMessage(chatId, "Сповіщення не знайдено.");
        }
    }

    public void handleDescriptionUpdate(String callbackData, long chatId) throws TelegramApiException {
        long taskId = Long.parseLong(callbackData.split("_")[2]);
        initiateDescriptionChange(chatId, taskId);
    }
}