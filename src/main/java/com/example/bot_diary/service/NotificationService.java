package com.example.bot_diary.service;

import com.example.bot_diary.job.NotificationScheduler;
import com.example.bot_diary.models.Notification;
import com.example.bot_diary.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
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

    @Transactional
    public Optional<Notification> findNotificationById(Long notificationId) {
        Optional<Notification> notificatId = notificationRepository.findById(notificationId);
        System.out.println("NotificationService notificatId =  " + notificatId);
        return notificatId;
    }

    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            try {
                notificationRepository.delete(notification);
                if (!notificationRepository.existsById(notificationId)) {
                    log.info("Notification deleted successfully.");
                } else {
                    log.error("Failed to delete notification.");
                }
            } catch (Exception e) {
                log.error("Exception when trying to delete notification: ", e);
            }
        } else {
            log.warn("Notification not found with ID: {}", notificationId);
        }
    }

}
