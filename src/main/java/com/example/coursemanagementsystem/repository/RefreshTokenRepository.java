package com.example.coursemanagementsystem.repository;

import com.example.coursemanagementsystem.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Ref;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String refreshToken);
}
