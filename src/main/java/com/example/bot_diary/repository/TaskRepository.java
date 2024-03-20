package com.example.bot_diary.repository;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserChatIdAndStatus(Long userChatId, TaskStatus status);

    List<Task> findByUserChatId(Long userChatId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByStatusNotIn(List<TaskStatus> statuses);

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
}

