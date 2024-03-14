package com.example.bot_diary.service;

import com.example.bot_diary.models.User;
import com.example.bot_diary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findOrCreateUser(Long chatId) {
        return userRepository.findById(chatId).orElseGet(() -> {
            User newUser = new User();
            newUser.setChatId(chatId);
            newUser.setEmail("temporary@example.com"); // Припустимо, що ви вже встановили email
            newUser.setPassword("temporaryPassword"); // Тимчасовий пароль, рекомендується отримати реальний пароль від користувача
            newUser.setRegisterAt(LocalDateTime.now());
            // Додаткова ініціалізація нового користувача
            return userRepository.save(newUser);
        });
    }

}