package com.example.bot_diary.repository;

import com.example.bot_diary.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByTaskId(Long taskId);

    List<Notification> findByNotificationTimeBefore(LocalDateTime time);

    @Query("SELECT n FROM Notification n WHERE n.notificationTime <= :currentTime AND n.sent = false")
    List<Notification> findDueNotifications(@Param("currentTime") LocalDateTime currentTime);
}
