package com.example.bot_diary.models;

import com.example.bot_diary.pages_handler.comands.AdminHandler;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder  // Анотація тут
public class User {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "register_at", nullable = false)
    private LocalDateTime registerAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminHandler.UserStatus status; // Значення за замовчуванням

    public void setStatus(AdminHandler.UserStatus status) {
        this.status = status;
    }
}



