package com.example.bot_diary.job;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {
    @Bean
    public JobDetail overdueTaskJobDetail() {
        return JobBuilder.newJob(OverdueTaskJob.class)
                .withIdentity("overdueTaskJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger overdueTaskTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(overdueTaskJobDetail())
                .withIdentity("overdueTaskTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(1)
                        .repeatForever())
                .build();
    }
}
