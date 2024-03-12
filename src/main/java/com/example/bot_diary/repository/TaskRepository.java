package com.example.bot_diary.repository;

import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByStatusNotIn(List<TaskStatus> statuses);
}
