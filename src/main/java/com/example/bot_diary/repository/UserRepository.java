package com.example.bot_diary.repository;

import com.example.bot_diary.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{
}
