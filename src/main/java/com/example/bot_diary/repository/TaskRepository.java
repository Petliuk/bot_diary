package com.example.bot_diary.repository;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserChatIdAndStatus(Long userChatId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.user.chatId = :userId AND t.dueDate BETWEEN :startDateTime AND :endDateTime")
    List<Task> findTasksInMonthForUser(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE t.user.chatId = :userId AND t.dueDate BETWEEN :startOfDay AND :endOfDay")
    List<Task> findTasksForDay(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startTime AND :endTime AND t.status = 'NOT_COMPLETED'")
    List<Task> findTasksDue(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.chatId = :userId")
    long countByUserChatId(@Param("userId") Long userId);

    Optional<Task> findFirstByUserChatIdOrderByDueDateDesc(Long chatId);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.notifications WHERE t.id = :id")
    Optional<Task> findByIdWithNotifications(@Param("id") Long id);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :now AND t.status = 'NOT_COMPLETED'")
    List<Task> findTasksDue(@Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.scheduledTime <= :now AND t.status = 'NOT_COMPLETED'")
    List<Task> findTasksScheduledForNow(@Param("now") LocalDateTime now);
}