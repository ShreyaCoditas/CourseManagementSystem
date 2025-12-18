package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.constants.EnrollmentStatus;
import com.example.coursemanagementsystem.constants.Roles;
import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.dto.AdminDashboardDto;
import com.example.coursemanagementsystem.dto.ApiResponseDto;
import com.example.coursemanagementsystem.entity.Course;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.repository.CourseRepository;
import com.example.coursemanagementsystem.repository.EnrollmentRepository;
import com.example.coursemanagementsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public ApiResponseDto<Void> deleteUser(Long userId) {
        log.info("to delete user attempt | userId={} ",userId);

        User user=userRepository.findById(userId)
                .orElseThrow(()->{
                            log.warn("User not found | userId={}",userId);
                            return new ResourceNotFoundException("User not found");
                });

        user.setStatus(Status.INACTIVE);
        userRepository.save(user);
        log.info("Disabled user successfully | userId={}",userId);
        return new ApiResponseDto<>(true,"Disabled User Successfully",null);
    }

    public ApiResponseDto<Void> deleteCourse(Long courseId) {
        log.info("Delete course request | courseId={}",courseId);
        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->{
                            log.warn("Course Not Found | courseId={}",courseId);
                            return new ResourceNotFoundException("Course Not Found");
                });

        course.setStatus(Status.INACTIVE);
        courseRepository.save(course);
        log.info("Disabled courses successfully");
        return new ApiResponseDto<>(true,"Disabled Courses Successfully",null);
    }

    public ApiResponseDto<AdminDashboardDto> adminDashboard() {

        log.info("Admin Dashboard request");

        long totalStudents=userRepository.countByRole(Roles.STUDENT);
        long totalInstructors=userRepository.countByRole(Roles.INSTRUCTOR);
        long totalCourses=courseRepository.count();
        long totalEnrollments=enrollmentRepository.count();
        long activeEnrollments=enrollmentRepository.countByEnrollmentStatus(EnrollmentStatus.ENROLLED);
        long cancelledEnrollments=enrollmentRepository.countByEnrollmentStatus(EnrollmentStatus.CANCELLED);

        log.debug("stats| totalStudents={} | totalInstructors={} | totalCourses={} | totalEnrollments={} | activeEnrollments={} | cancelledEnrollements={}",
        totalStudents,totalInstructors,totalCourses,totalEnrollments,activeEnrollments,cancelledEnrollments);

        AdminDashboardDto dashboard=AdminDashboardDto.builder()
                .totalStudents(totalStudents)
                .totalInstructors(totalInstructors)
                .totalCourses(totalCourses)
                .totalEnrollments(totalEnrollments)
                .activeEnrollments(activeEnrollments)
                .cancelledEnrollments(cancelledEnrollments)
                .build();

        log.info("fetched admin dashboard successfully");

        return new ApiResponseDto<>(true,"Fetched Admin Dashboard successfully",dashboard);

    }


}
