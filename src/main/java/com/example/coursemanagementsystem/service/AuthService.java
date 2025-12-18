package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.dto.*;
import com.example.coursemanagementsystem.entity.RefreshToken;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.exception.ResourceAlreadyExistsException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.repository.RefreshTokenRepository;
import com.example.coursemanagementsystem.repository.UserRepository;
import com.example.coursemanagementsystem.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    //register
    public ApiResponseDto<Void> register(CreateRegisterDto createRegisterDto) {
        String email = createRegisterDto.getEmail();
        String name = createRegisterDto.getName();
        if (userRepository.findByEmailIgnoreCase(email).isPresent())
            throw new ResourceAlreadyExistsException("Email Already exists");
        if (userRepository.findByNameIgnoreCase(name).isPresent())
            throw new ResourceAlreadyExistsException("Username already exists");
        User user = new User();
        user.setName(createRegisterDto.getName());
        user.setEmail(createRegisterDto.getEmail());
        user.setPassword(passwordEncoder.encode(createRegisterDto.getPassword()));
        user.setRole(createRegisterDto.getRole());
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
        return new ApiResponseDto<>(true, "User Registered successfully", null);
    }

    //login
    public ApiResponseDto<LoginResponseDto> login(@Valid CreateLoginDto createLoginDto) {
        User user = userRepository.findByEmailIgnoreCase(createLoginDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email Not Found"));

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(createLoginDto.getEmail(), createLoginDto.getPassword())
            );
            if (authentication.isAuthenticated()) {
                String accessToken = jwtUtil.generateAccessToken(user.getEmail().toLowerCase());
                RefreshToken refreshToken=new RefreshToken();
                refreshToken.setToken(UUID.randomUUID().toString());
                refreshToken.setUser(user);
                refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
                refreshTokenRepository.save(refreshToken);

                LoginResponseDto dto = new LoginResponseDto();
                dto.setId(user.getId());
                dto.setName(user.getName());
                dto.setEmail(user.getEmail());
                dto.setRole(user.getRole());
                dto.setAccesstoken(accessToken);
                dto.setRefreshtoken(refreshToken.getToken());
                dto.setCreatedAt(user.getCreatedAt());
                dto.setUpdatedAt(user.getUpdatedAt());
                return new ApiResponseDto<>(true, "User LoggedIn successfully", dto);
            } else {
                throw new BadCredentialsException("Invalid password");
            }
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid password");
        }
    }


    public ApiResponseDto<LoginResponseDto> refreshToken(String refreshToken) {

        RefreshToken oldToken=refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(()-> new RuntimeException("Invalid refresh token"));

        if(oldToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Refresh token expired");
        }

        User user=oldToken.getUser();

        RefreshToken newToken=new RefreshToken();
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setUser(user);
        newToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(newToken);

        LoginResponseDto dto=new LoginResponseDto();
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setAccesstoken(jwtUtil.generateAccessToken(dto.getEmail()));
        dto.setRefreshtoken(newToken.getToken());
        return new ApiResponseDto<>(true,"Access Token Refreshed",dto);
    }

    public ApiResponseDto<Void> logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(()-> new RuntimeException("Invalid Refresh Token"));

        refreshTokenRepository.deleteByToken(refreshToken);

        return new ApiResponseDto<>(true,"Logged out succesfully",null);
    }
}