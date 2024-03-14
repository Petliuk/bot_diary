package com.example.bot_diary.service;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    public List<Task> findAllTasks() {
        return taskRepository.findByStatusNotIn(Arrays.asList(TaskStatus.COMPLETED, TaskStatus.POSTPONED));
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }
    public void updateTask(Task task) {
        if(task != null && task.getId() != null) {
            taskRepository.save(task);
        }
    }

    public Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }

    public void markTaskAsCompleted(Long taskId) {
        Task task = findTaskById(taskId);
        if (task != null && task.getStatus() != TaskStatus.COMPLETED) {
            task.setStatus(TaskStatus.COMPLETED);
            saveTask(task);
        }
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
