package com.example.bot_diary.service;

import com.example.bot_diary.models.User;
import com.example.bot_diary.models.UserStatus;
import com.example.bot_diary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> findUserByChatId(Long chatId) {
        return userRepository.findById(chatId);
    }

    public List<User> findUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }
    public User createUser(Long chatId, String firstName, String lastName, String userName) {
        User newUser = new User();
        newUser.setChatId(chatId);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setUserName(userName);
        newUser.setRegisterAt(LocalDateTime.now());
        return userRepository.save(newUser);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

}