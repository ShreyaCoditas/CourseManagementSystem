package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.dto.*;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.repository.UserRepository;
import com.example.coursemanagementsystem.security.UserPrincipal;
import com.example.coursemanagementsystem.service.StudentService;
import jakarta.validation.Valid;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
  public ResponseEntity<ApiResponseDto<PaginatedResponse<CourseResponseDto>>> getAllCourses(
          @RequestParam(defaultValue = "10") int pageSize,
          @RequestParam(defaultValue = "0") int pageNumber,
          @RequestParam(defaultValue = "id") String sortBy,
          @RequestParam(defaultValue = "asc") String direction,

          //Filters
          @RequestParam(required = false)String title,
          @RequestParam(required = false) String instructorName,
          @RequestParam(required = false) Status status,

          //Data Range
          @RequestParam(required = false)LocalDateTime fromDate,
          @RequestParam(required = false)LocalDateTime toDate,

          //Global Search
          @RequestParam(required = false) String search,

          //student count range
          @RequestParam(required = false) Integer minStudents,
          @RequestParam(required = false) Integer maxStudents
          ) {
    ApiResponseDto<PaginatedResponse<CourseResponseDto>> response = studentService.getAllCourses(pageNumber,pageSize,sortBy,direction,title,instructorName,status,fromDate,toDate,search,minStudents,maxStudents);
    return ResponseEntity.ok(response);
  }



  //To view the courses in which they are enrolled
  @GetMapping("/view/enrolled/courses")
  public ResponseEntity<ApiResponseDto<PaginatedResponse<CourseResponseDto>>> getEnrolledCourses(
          @AuthenticationPrincipal UserPrincipal principal,
          @RequestParam(value="pageSize",defaultValue ="10")int pageSize,
          @RequestParam(value="pageNumber",defaultValue = "0")int pageNumber){
    Long studentId=principal.getUser().getId();
    ApiResponseDto<PaginatedResponse<CourseResponseDto>> response=studentService.getEnrolledCourses(studentId,pageSize,pageNumber);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/purchase/course/{courseId}")
  public ResponseEntity<ApiResponseDto<String>> purchaseCourse(
          @PathVariable Long courseId,
          @AuthenticationPrincipal UserPrincipal principal
  ) {
    User student = principal.getUser();
    ApiResponseDto<String> response = studentService.purchaseCourse(courseId, student);
    return ResponseEntity.ok(response);
  }



}
