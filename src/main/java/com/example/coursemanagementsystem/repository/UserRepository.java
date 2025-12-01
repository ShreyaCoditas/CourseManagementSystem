package com.example.coursemanagementsystem.repository;

import com.example.coursemanagementsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByNameIgnoreCase(String name);

}
