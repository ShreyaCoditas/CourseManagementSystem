package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.constants.Roles;
import com.example.coursemanagementsystem.dto.ApiResponseDto;
import com.example.coursemanagementsystem.dto.CreateLoginDto;
import com.example.coursemanagementsystem.dto.CreateRegisterDto;
import com.example.coursemanagementsystem.dto.LoginResponseDto;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.exception.ResourceAlreadyExistsException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.repository.UserRepository;
import com.example.coursemanagementsystem.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    AuthenticationManager authManager;

    @Mock
    JwtUtil jwtUtil;

    @InjectMocks
    AuthService authService;



    // TEST 1: Register - Success
    @Test
    void testRegisterSuccess() {

        CreateRegisterDto dto = new CreateRegisterDto();
        dto.setEmail("test@gmail.com");
        dto.setName("Shreya");
        dto.setPassword("123");
        dto.setRole(Roles.STUDENT);

        // mock: no existing email or name
        when(userRepository.findByEmailIgnoreCase("test@gmail.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByNameIgnoreCase("Shreya"))
                .thenReturn(Optional.empty());

        // mock password encoding
        when(passwordEncoder.encode("123")).thenReturn("encodedPass");

        ApiResponseDto<Void> response = authService.register(dto);

        assertTrue(response.isSuccess());
        assertEquals("User Registered successfully", response.getMessage());

        // verify that save() was called
        verify(userRepository, times(1)).save(any(User.class));
    }



    // TEST 2: Register - Email Already Exists
    @Test
    void testRegisterEmailExists() {

        CreateRegisterDto dto = new CreateRegisterDto();
        dto.setEmail("test@gmail.com");
        dto.setName("Shreya");

        when(userRepository.findByEmailIgnoreCase("test@gmail.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(ResourceAlreadyExistsException.class,
                () -> authService.register(dto));
    }



    // TEST 3: Register - Username Exists
    @Test
    void testRegisterUsernameExists() {

        CreateRegisterDto dto = new CreateRegisterDto();
        dto.setEmail("abc@gmail.com");
        dto.setName("Shreya");

        when(userRepository.findByEmailIgnoreCase("abc@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.findByNameIgnoreCase("Shreya"))
                .thenReturn(Optional.of(new User()));

        assertThrows(ResourceAlreadyExistsException.class,
                () -> authService.register(dto));
    }



    // TEST 4: Login - Success
    @Test
    void testLoginSuccess() {

        CreateLoginDto dto = new CreateLoginDto();
        dto.setEmail("shreya@gmail.com");
        dto.setPassword("123");

        User user = new User();
        user.setEmail("shreya@gmail.com");
        user.setName("Shreya");

        when(userRepository.findByEmailIgnoreCase("shreya@gmail.com"))
                .thenReturn(Optional.of(user));

        // mock authentication success
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // mock jwt token
        when(jwtUtil.generateAccessToken("shreya@gmail.com"))
                .thenReturn("fake-jwt");

        ApiResponseDto<LoginResponseDto> response = authService.login(dto);

        assertTrue(response.isSuccess());
        assertEquals("User LoggedIn successfully", response.getMessage());
        assertEquals("fake-jwt", response.getData().getToken());
    }



    // TEST 5: Login - Email Not Found
    @Test
    void testLoginEmailNotFound() {

        CreateLoginDto dto = new CreateLoginDto();
        dto.setEmail("unknown@gmail.com");

        when(userRepository.findByEmailIgnoreCase("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.login(dto));
    }



    // TEST 6: Login - Wrong Password
    @Test
    void testLoginWrongPassword() {

        CreateLoginDto dto = new CreateLoginDto();
        dto.setEmail("shreya@gmail.com");
        dto.setPassword("wrong");

        User user = new User();
        user.setEmail("shreya@gmail.com");

        when(userRepository.findByEmailIgnoreCase("shreya@gmail.com"))
                .thenReturn(Optional.of(user));

        // mock bad password attempt
        when(authManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid password"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(dto));
    }
}
