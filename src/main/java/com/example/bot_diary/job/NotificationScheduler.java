package com.example.bot_diary.job;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class NotificationScheduler {

    public void scheduleNotification(Long chatId, LocalDateTime dueDateTime, Duration reminderBeforeDue, String taskDescription) throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        LocalDateTime notificationDateTime = dueDateTime.minus(reminderBeforeDue);
        String uniqueIdentifier = "taskNotification_" + chatId + "_" + System.currentTimeMillis();

        JobDetail jobDetail = JobBuilder.newJob(NotificationJob.class)
                .withIdentity(uniqueIdentifier, "notifications")
                .usingJobData("chatId", chatId)
                .usingJobData("taskDescription", taskDescription)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger_" + uniqueIdentifier, "notifications")
                .startAt(Date.from(notificationDateTime.atZone(ZoneId.systemDefault()).toInstant()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        if (!scheduler.isStarted()) {
            scheduler.start();
        }
    }
}

