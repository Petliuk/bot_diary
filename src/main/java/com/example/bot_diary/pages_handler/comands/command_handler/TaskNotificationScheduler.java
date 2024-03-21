package com.example.bot_diary.pages_handler.comands.command_handler;


import com.example.bot_diary.bot.TelegramBot;
import com.example.bot_diary.models.Task;
import com.example.bot_diary.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class TaskNotificationScheduler {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TelegramBot telegramBot;

    @Scheduled(fixedRate = 60000) // перевірка кожну хвилину
    public void sendTaskReminders() {
        List<Task> dueTasks = taskService.findTasksDue();
        for (Task task : dueTasks) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(task.getDueDate().minusMinutes(1)) && now.isBefore(task.getDueDate().plusMinutes(1))) {
                telegramBot.sendMessage(task.getUser().getChatId(), buildReminderMessage(task));
            }
        }
    }

    private String buildReminderMessage(Task task) {
        return "Нагадування: Вам потрібно виконати задачу \"" + task.getDescription() + "\" до " + task.getDueDate();
    }
}