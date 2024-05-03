package com.example.bot_diary.job;

import com.example.bot_diary.repository.NotificationRepository;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.bot_diary.models.Notification;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class NotificationScheduler {

    private Scheduler scheduler;

    @Autowired
    private NotificationRepository notificationRepository;

    public NotificationScheduler() {
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void rescheduleAllPendingNotifications() {
        List<Notification> unsentNotifications = notificationRepository.findDueNotifications(LocalDateTime.now());
        for (Notification notification : unsentNotifications) {
            if (!notification.isSent()) {
                try {
                    scheduleNotification(notification);
                } catch (SchedulerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void scheduleNotification(Notification notification) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(NotificationJob.class)
                .withIdentity("notification_" + notification.getId(), "notifications")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger_" + notification.getId(), "notifications")
                .startAt(Date.from(notification.getNotificationTime().atZone(ZoneId.systemDefault()).toInstant()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

}