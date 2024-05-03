package com.example.bot_diary.service;

import com.example.bot_diary.job.NotificationScheduler;
import com.example.bot_diary.models.Notification;
import com.example.bot_diary.repository.NotificationRepository;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationScheduler notificationScheduler;

    @Transactional
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Transactional
    public List<Notification> findNotificationsByTaskId(Long taskId) {
        return notificationRepository.findByTaskId(taskId);
    }

    @Transactional
    public void saveAndFlushNotification(Notification notification) {
        notificationRepository.saveAndFlush(notification);
    }

    @Transactional
    public List<Notification> findDueNotifications(LocalDateTime now) {
        return notificationRepository.findByNotificationTimeBefore(now);
    }

    @Transactional
    public void saveAndScheduleNotification(Notification notification) {
        Notification savedNotification = notificationRepository.save(notification);
        try {
            notificationScheduler.scheduleNotification(savedNotification);
        } catch (SchedulerException e) {
            System.err.println("Не вдалося запланувати сповіщення: " + e.getMessage());
            // Обробіть помилку адекватно
        }
    }
}
