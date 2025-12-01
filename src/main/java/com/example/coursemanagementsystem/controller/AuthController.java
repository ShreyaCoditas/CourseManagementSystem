package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.dto.ApiResponseDto;
import com.example.coursemanagementsystem.dto.CreateLoginDto;
import com.example.coursemanagementsystem.dto.CreateRegisterDto;
import com.example.coursemanagementsystem.dto.LoginResponseDto;
import com.example.coursemanagementsystem.repository.UserRepository;
import com.example.coursemanagementsystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    //Register
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<Void>> register(@Valid @RequestBody CreateRegisterDto createRegisterDto){
        ApiResponseDto<Void> response=authService.register(createRegisterDto);
        return ResponseEntity.ok(response);
    }

    //Login
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@Valid @RequestBody CreateLoginDto createLoginDto){
        ApiResponseDto<LoginResponseDto> response=authService.login(createLoginDto);
        return ResponseEntity.ok(response);
    }



}
