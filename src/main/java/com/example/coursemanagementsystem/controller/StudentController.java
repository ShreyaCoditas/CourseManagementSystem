package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.dto.*;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.repository.UserRepository;
import com.example.coursemanagementsystem.security.UserPrincipal;
import com.example.coursemanagementsystem.service.StudentService;
import jakarta.validation.Valid;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/")
public class StudentController {

    @Autowired
    private StudentService studentService;

  @PostMapping("/enroll/course/{courseId}")
    public ResponseEntity<ApiResponseDto<EnrollmentDTO>> enrollIntoCourse(
          @PathVariable Long courseId,
          @AuthenticationPrincipal UserPrincipal userPrincipal){
      User student=userPrincipal.getUser();
      ApiResponseDto<EnrollmentDTO> response=studentService.enrollIntoCourse(courseId,student);
      return ResponseEntity.ok(response);
  }

  @PostMapping("/cancel/enrollment/course/{courseId}")
    public ResponseEntity<ApiResponseDto<CancelEnrollmentResponseDTO>> cancelEnrollment(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal userPrincipal){
      User student=userPrincipal.getUser();
      ApiResponseDto< CancelEnrollmentResponseDTO> response=studentService.cancelEnrollmentIntoCourse(courseId,student);
      return ResponseEntity.ok(response);
  }

  @GetMapping("/view/courses")
    public ResponseEntity<ApiResponseDto<List<CourseResponseDto>>> getAllCourses(){
      ApiResponseDto<List<CourseResponseDto>> response=studentService.getAllCourses();
      return ResponseEntity.ok(response);
  }

  //To view the courses in which they are enrolled
  @GetMapping("/view/enrolled/courses")
  public ResponseEntity<ApiResponseDto<List<CourseResponseDto>>> getAllEnrolledCourses(){
    ApiResponseDto<List<CourseResponseDto>> response=studentService.getAllEnrolledCourses();
    return ResponseEntity.ok(response);
  }
}
