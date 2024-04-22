package com.example.bot_diary.job;

import com.example.bot_diary.context.ApplicationContextProvider;
import com.example.bot_diary.pages_handler.comands.command_handler.MessageService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class NotificationJob implements Job {

    @Autowired
    private MessageService messageService;

    private void autowireDependencies() {
        AbstractApplicationContext applicationContext = ApplicationContextProvider.getAbstractApplicationContext();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        autowireDependencies();

        Long chatId = context.getJobDetail().getJobDataMap().getLong("chatId");
        String taskDescription = context.getJobDetail().getJobDataMap().getString("taskDescription");

        messageService.sendMessage(chatId, "Нагадування: " + taskDescription + ". Час виконати вашу задачу.");
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
}