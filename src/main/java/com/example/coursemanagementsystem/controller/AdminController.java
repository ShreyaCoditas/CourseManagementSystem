package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.dto.AdminDashboardDto;
import com.example.coursemanagementsystem.dto.ApiResponseDto;
import com.example.coursemanagementsystem.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @DeleteMapping("/delete/users/{userId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteUser(@PathVariable Long userId){
        ApiResponseDto<Void> response=adminService.deleteUser(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/courses/{courseId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteCourse(@PathVariable Long courseId){
        ApiResponseDto<Void> response=adminService.deleteCourse(courseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponseDto<AdminDashboardDto>> adminDashboard(){
        ApiResponseDto<AdminDashboardDto> response=adminService.adminDashboard();
        return ResponseEntity.ok(response);
    }
}
