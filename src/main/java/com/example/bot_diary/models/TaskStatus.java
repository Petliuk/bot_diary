package com.example.bot_diary.models;

public enum TaskStatus {

    COMPLETED("Виконано"),
    POSTPONED("Відкладено"),
    NOT_COMPLETED("Не виконано");

    private final String displayName;
    TaskStatus(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }

}