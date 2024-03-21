package com.example.bot_diary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BotDiaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotDiaryApplication.class, args);
    }

}
