package com.example.bot_diary.models;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequest {

    private Long chatId;

    public RegistrationRequest(Long chatId) {
        this.chatId = chatId;
    }
}
