package com.example.bot_diary.service;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {


    @Autowired
    private TaskRepository taskRepository;

    @Transactional
    public long countUserTasks(Long userId) {
        return taskRepository.countByUserChatId(userId);
    }

    @Transactional
    public List<LocalDate> findDatesWithTasks(YearMonth month, Long userChatId) {
        List<Task> tasks = taskRepository.findTasksInMonthForUser(month.atDay(1).atStartOfDay(), month.atEndOfMonth().atStartOfDay(), userChatId);
        return tasks.stream()
                .map(Task::getDueDate)
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Task> findTasksForDay(LocalDate date, Long userChatId) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return taskRepository.findTasksForDay(startOfDay, endOfDay, userChatId);
    }

    @Transactional
    public List<Task> findTasksByStatusAndUserChatId(TaskStatus status, Long userChatId) {
        return taskRepository.findByUserChatIdAndStatus(userChatId, status);
    }

    @Transactional
    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    @Transactional
    public Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }

    @Transactional
    public void revertTask(Long taskId) {
        Task task = findTaskById(taskId);
        if (task != null) {
            task.setStatus(TaskStatus.NOT_COMPLETED);
            saveTask(task);
        }
    }

    @Transactional
    public void postponeTask(Long taskId) {
        Task task = findTaskById(taskId);
        if (task != null) {
            task.setStatus(TaskStatus.POSTPONED);
            saveTask(task);
        }
    }

    @Transactional
    public Task findTaskByIdWithNotifications(Long taskId) {
        return taskRepository.findByIdWithNotifications(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Transactional
    public List<Task> findOverdueTasks(LocalDateTime now) {
        return taskRepository.findTasksDue(now);
    }

    @Transactional
    public List<Task> findTasksScheduledForNow(LocalDateTime now) {
        return taskRepository.findTasksScheduledForNow(now);
    }
}