package com.example.coursemanagementsystem.controller;

import com.cloudinary.Api;
import com.example.coursemanagementsystem.dto.*;
import com.example.coursemanagementsystem.repository.UserRepository;
import com.example.coursemanagementsystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> refresh(@RequestBody RefreshTokenRequestDto dto){
        ApiResponseDto<LoginResponseDto> response=authService.refreshToken(dto.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout(@RequestBody RefreshTokenRequestDto dto){
        ApiResponseDto<Void> response=authService.logout(dto.getRefreshToken());
        return ResponseEntity.ok(response);
    }


}
