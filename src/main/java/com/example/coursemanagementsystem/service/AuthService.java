package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.dto.ApiResponseDto;
import com.example.coursemanagementsystem.dto.CreateLoginDto;
import com.example.coursemanagementsystem.dto.CreateRegisterDto;
import com.example.coursemanagementsystem.dto.LoginResponseDto;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.repository.UserRepository;
import com.example.coursemanagementsystem.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    //register
    public ApiResponseDto<Void> register(CreateRegisterDto createRegisterDto) {
        String email = createRegisterDto.getEmail();
        String name = createRegisterDto.getName();
        if (userRepository.findByEmailIgnoreCase(email).isPresent())
            throw new RuntimeException("Email Already exists");
        if (userRepository.findByNameIgnoreCase(name).isPresent())
            throw new RuntimeException("Username already exists");
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
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(createLoginDto.getEmail(), createLoginDto.getPassword())
            );
            if (authentication.isAuthenticated()) {
                String jwt = jwtUtil.generateToken(user.getEmail().toLowerCase());
                LoginResponseDto dto = new LoginResponseDto();
                dto.setId(user.getId());
                dto.setName(user.getName());
                dto.setEmail(user.getEmail());
                dto.setRole(user.getRole());
                dto.setToken(jwt);
                dto.setCreatedAt(user.getCreatedAt());
                dto.setUpdatedAt(user.getUpdatedAt());
                return new ApiResponseDto<>(true, "User LoggedIn successfully", dto);
            } else {
                throw new BadCredentialsException("Invalid password");
            }
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid password");
        }
    }
}