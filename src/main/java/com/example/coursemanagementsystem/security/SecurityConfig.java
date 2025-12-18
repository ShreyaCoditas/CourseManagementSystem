package com.example.coursemanagementsystem.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh-token",
                                "/api/auth/logout",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()
                        .requestMatchers("/api/instructor/add/course").hasRole("INSTRUCTOR")
                        .requestMatchers("/api/instructor/update/course/{courseId}").hasRole("INSTRUCTOR")
                        .requestMatchers("/api/instructor/delete/course/{courseId}").hasRole("INSTRUCTOR")
                        .requestMatchers("/api/instructor/enrolled/students").hasRole("INSTRUCTOR")
                        .requestMatchers("/api/instructor/all/courses").hasRole("INSTRUCTOR")
                        .requestMatchers("/api/instructor/upload/picture").hasRole("INSTRUCTOR")
                        .requestMatchers("/api/student/enroll/course/{courseId}").hasRole("STUDENT")
                        .requestMatchers("/api/student/cancel/enrollment/course/{courseId}").hasRole("STUDENT")
                        .requestMatchers("/api/student/view/courses").hasRole("STUDENT")
                        .requestMatchers("/api/student/view/enrolled/courses").hasRole("STUDENT")
                        .requestMatchers("/api/student/purchase/course/{courseId}").hasRole("STUDENT")
                        .requestMatchers("/api/admin/delete/users/{userId}").hasRole("ADMIN")
                        .requestMatchers("/api/admin/delete/courses/{courseId}").hasRole("ADMIN")
                        .requestMatchers("/api/admin/dashboard").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}



