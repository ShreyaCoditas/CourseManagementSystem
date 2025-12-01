package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.dto.AdminDashboardDto;
import com.example.coursemanagementsystem.dto.ApiResponseDto;
import com.example.coursemanagementsystem.entity.Course;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.repository.CourseRepository;
import com.example.coursemanagementsystem.repository.EnrollmentRepository;
import com.example.coursemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public ApiResponseDto<Void> deleteUser(Long userId) {
        User user=userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));
        user.setStatus(Status.INACTIVE);
        userRepository.save(user);
        return new ApiResponseDto<>(true,"Disabled User Successfully",null);
    }

    public ApiResponseDto<Void> deleteCourse(Long courseId) {
        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->new RuntimeException("Course Not Found"));
        course.setStatus(Status.INACTIVE);
        courseRepository.save(course);
        return new ApiResponseDto<>(true,"Disabled Courses Successfully",null);
    }

    public ApiResponseDto<List<AdminDashboardDto>> adminDashboard() {

    }
}
