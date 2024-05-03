package com.example.bot_diary.job;

import com.example.bot_diary.models.Notification;
import com.example.bot_diary.pages_handler.comands.command_handler.MessageService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.bot_diary.service.NotificationService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class NotificationJob implements Job {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private MessageService messageService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime now = LocalDateTime.now();
        List<Notification> notifications = notificationService.findDueNotifications(now);
        System.out.println("Notifications to send: " + notifications.size());
        for (Notification notification : notifications) {
            System.out.println("Processing notification ID: " + notification.getId() + ", Sent: " + notification.isSent());
            if (!notification.isSent()) {
                String messageText = String.format(
                        "Ви маєте виконати задачу \"%s\" в %s",
                        notification.getTask().getDescription(),
                        notification.getTask().getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                );
                messageService.sendMessage(notification.getTask().getUser().getChatId(), messageText);
                notification.setSent(true);
                notificationService.saveAndFlushNotification(notification);
                System.out.println("Notification sent and marked as sent: " + notification.getId());
            }
        }
    }
}