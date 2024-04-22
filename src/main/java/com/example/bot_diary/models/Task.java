package com.example.bot_diary.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;

    @Column(name = "due_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime dueDate;

    @Column(name = "timezone")
    private String timezone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_chat_id", nullable = false)
    private User user;

    @Column(name = "notification_time", columnDefinition = "TIMESTAMP")
    private LocalDateTime notificationTime;

}