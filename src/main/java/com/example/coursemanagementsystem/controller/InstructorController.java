package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.dto.*;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.security.UserPrincipal;
import com.example.coursemanagementsystem.service.InstructorService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/instructor/")
public class InstructorController {

    @Autowired
    private InstructorService instructorService;

    @PostMapping("/add/course")
    public ResponseEntity<ApiResponseDto<Void>> addCourse(
            @Valid @RequestBody AddCourseDto addCourseDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        User instructor=userPrincipal.getUser();
        log.info("Add Course API called | instructorId={}",instructor.getId());
        ApiResponseDto<Void> response=instructorService.addCourse(addCourseDto,instructor);
        log.info("Add Course API completed | instructorId={}",instructor.getId());
        return ResponseEntity.ok(response);
    }



    @PutMapping("/update/course/{courseId}")
    public ResponseEntity<ApiResponseDto<CourseDto>> updateCourse(
           @Valid @RequestBody UpdateCourseDto updateCourseDto,
           @PathVariable Long courseId,
           @AuthenticationPrincipal UserPrincipal userPrincipal){
        User instructor=userPrincipal.getUser();
        log.info("Update Course API called | instructorId={} | courseId={} ",instructor.getId(),courseId);
        ApiResponseDto<CourseDto> response=instructorService.updateCourse(updateCourseDto,courseId,instructor);
        log.info("Update Course API completed | instructorId={} | courseId={} ",instructor.getId(),courseId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/course/{courseId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        User instructor=userPrincipal.getUser();
        log.info("Delete Course API called | instructorId={} | courseId={}",instructor.getId(),courseId);
        ApiResponseDto<Void> response=instructorService.deleteCourse(courseId,instructor);
        log.info("Delete Course API completed | instructorId={} | courseId={}",instructor.getId(),courseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/enrolled/students")
    public ResponseEntity<ApiResponseDto<PaginatedResponse<StudentInfoDto>>> getEnrolledStudents(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageNumber
    ){
        User instructor=principal.getUser();
        log.info("To Fetch Enrolled Students API called");
        ApiResponseDto<PaginatedResponse<StudentInfoDto>> response=instructorService.getEnrolledStudents(instructor,pageSize,pageNumber);
        log.info("To Fetch Enrolled Students API completed");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/picture")
    public ResponseEntity<ApiResponseDto<Void>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ){
        User instructor=principal.getUser();
        log.info("To Uplaod image API called | instructorId={}",instructor.getId());
        ApiResponseDto<Void> response=instructorService.uploadImage(file,instructor);
        log.info("To Uplaod image API completed | instructorId={}",instructor.getId());
        return ResponseEntity.ok(response);
    }
}
