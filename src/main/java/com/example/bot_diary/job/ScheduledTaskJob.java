package com.example.bot_diary.job;
import com.example.bot_diary.models.Task;
import com.example.bot_diary.models.TaskStatus;
import com.example.bot_diary.service.TaskService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ScheduledTaskJob implements Job{
    @Autowired
    private TaskService taskService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime now = LocalDateTime.now();
        List<Task> scheduledTasks = taskService.findTasksScheduledForNow(now);

        for (Task task : scheduledTasks) {
            task.setStatus(TaskStatus.POSTPONED);
            taskService.saveTask(task);
        }
    }
}
