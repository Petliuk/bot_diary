package com.example.bot_diary.repository;

import com.example.bot_diary.models.User;
import com.example.bot_diary.models.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long>{
    List<User> findByStatus(UserStatus status);
}
