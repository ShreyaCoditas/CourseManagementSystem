package com.example.coursemanagementsystem.dto;

import com.example.coursemanagementsystem.constants.Roles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private Long id;
    private String name;
    private String email;
    private String accesstoken;
    private String refreshtoken;
    private Roles role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
