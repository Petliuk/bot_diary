package com.example.bot_diary.service;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> findAllTasksByUserChatId(Long userChatId) {
        return taskRepository.findByUserChatId(userChatId);
    }

    public List<Task> findTasksDue() {
        LocalDateTime now = LocalDateTime.now();
        return taskRepository.findTasksDue(now.minusMinutes(1), now.plusMinutes(1));
    }

    public List<LocalDate> findDatesWithTasks(YearMonth month, Long userChatId) {
        List<Task> tasks = taskRepository.findTasksInMonthForUser(month.atDay(1).atStartOfDay(), month.atEndOfMonth().atStartOfDay(), userChatId);
        return tasks.stream()
                .map(Task::getDueDate)
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .collect(Collectors.toList());
    }
    public List<Task> findTasksForDay(LocalDate date, Long userChatId) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return taskRepository.findTasksForDay(startOfDay, endOfDay, userChatId);
    }

    public List<Task> findTasksByStatusAndUserChatId(TaskStatus status, Long userChatId) {
        return taskRepository.findByUserChatIdAndStatus(userChatId, status);
    }

    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    public List<Task> findAllTasks() {
        return taskRepository.findByStatusNotIn(Arrays.asList(TaskStatus.COMPLETED, TaskStatus.POSTPONED));
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    public Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }


    public List<Task> findAllTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    public void revertTask(Long taskId) {
        Task task = findTaskById(taskId);
        if (task != null) {
            task.setStatus(TaskStatus.NOT_COMPLETED);
            saveTask(task);
        }
    }

    public void postponeTask(Long taskId) {
        Task task = findTaskById(taskId);
        if (task != null) {
            task.setStatus(TaskStatus.POSTPONED);
            saveTask(task);
        }
    }

}
