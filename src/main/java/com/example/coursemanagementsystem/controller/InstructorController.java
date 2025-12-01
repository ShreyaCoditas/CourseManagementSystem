package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.dto.AddCourseDto;
import com.example.coursemanagementsystem.dto.ApiResponseDto;
import com.example.coursemanagementsystem.dto.CourseDto;
import com.example.coursemanagementsystem.dto.UpdateCourseDto;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.security.UserPrincipal;
import com.example.coursemanagementsystem.service.InstructorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        ApiResponseDto<Void> response=instructorService.addCourse(addCourseDto,instructor);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/course/{courseId}")
    public ResponseEntity<ApiResponseDto<CourseDto>> updateCourse(
           @Valid @RequestBody UpdateCourseDto updateCourseDto,
           @PathVariable Long courseId,
           @AuthenticationPrincipal UserPrincipal userPrincipal){
        User instructor=userPrincipal.getUser();
        ApiResponseDto<CourseDto> response=instructorService.updateCourse(updateCourseDto,courseId,instructor);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/course/{courseId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        User instructor=userPrincipal.getUser();
        ApiResponseDto<Void> response=instructorService.deleteCourse(courseId,instructor);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all/courses")
    public ResponseEntity<ApiResponseDto<List<CourseDto>>>getAllCourses(
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        User instructor=userPrincipal.getUser();
        ApiResponseDto<List<CourseDto>> response=instructorService.getAllCourses(instructor);
        return ResponseEntity.ok(response);
    }

}
