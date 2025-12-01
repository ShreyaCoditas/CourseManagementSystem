package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.constants.EnrollmentStatus;
import com.example.coursemanagementsystem.constants.Roles;
import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.dto.ApiResponseDto;
import com.example.coursemanagementsystem.dto.CancelEnrollmentResponseDTO;
import com.example.coursemanagementsystem.dto.CourseResponseDto;
import com.example.coursemanagementsystem.dto.EnrollmentDTO;
import com.example.coursemanagementsystem.entity.Course;
import com.example.coursemanagementsystem.entity.Enrollment;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.repository.CourseRepository;
import com.example.coursemanagementsystem.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private  EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    public ApiResponseDto<List<CourseResponseDto>> getAllCourses() {
        List<Course> courses=courseRepository.findAll();
        List<CourseResponseDto> courseResponseDtos=courses.stream()
                .filter(c->c.getStatus()==Status.ACTIVE)
                .map(this::mapToCourseDto)
                .collect(Collectors.toList());
        return new ApiResponseDto<>(true,"Fetched All Courses successfully",courseResponseDtos);
    }

    public ApiResponseDto<EnrollmentDTO> enrollIntoCourse(Long courseId, User student) {
        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->new RuntimeException("Course Not Found"));
        if(!Roles.STUDENT.equals(student.getRole()))
            throw new RuntimeException("Only students can enroll into courses");
        if(course.getStatus()== Status.INACTIVE)
            throw new RuntimeException("Cannot enroll into inactive courses");

        boolean alreadyenrolled=enrollmentRepository.existsByCourseIdAndStudentIdAndEnrollmentStatus(courseId,student.getId(),EnrollmentStatus.ENROLLED);
        if(alreadyenrolled)
            throw new RuntimeException("Already enrolled in this course");
        Enrollment enrollment=new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setEnrollmentStatus(EnrollmentStatus.ENROLLED);
        Enrollment saved=enrollmentRepository.save(enrollment);
        return new ApiResponseDto<>(true,"Student enrolled into course successfully",mapToEnrollmentDTO(saved));

    }


    public ApiResponseDto<CancelEnrollmentResponseDTO> cancelEnrollmentIntoCourse(Long courseId, User student) {
        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->new RuntimeException("Course Not Found"));

        if(course.getStatus()==Status.INACTIVE)
            throw new RuntimeException("Cannot cancel inactive status");

        if(!student.getRole().equals(Roles.STUDENT))
            throw new RuntimeException("Only Student can cancel the enrollment");

        Enrollment enrollment=enrollmentRepository.
                findByCourseIdAndStudentId(courseId,student.getId())
                        .orElseThrow(()->new RuntimeException("Enrollment Not found"));

        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setEnrollmentStatus(EnrollmentStatus.CANCELLED);
        enrollment.setCancelledAt(LocalDateTime.now());
        Enrollment saved=enrollmentRepository.save(enrollment);
        return new ApiResponseDto<>(true,"cancelled enrollment successfully",mapToDto(saved));
    }

    private EnrollmentDTO mapToEnrollmentDTO(Enrollment enrollment){
        Course course= enrollment.getCourse();
        User student=enrollment.getStudent();
        return EnrollmentDTO.builder()
                .enrollmentId(enrollment.getId())
                .studentId(student.getId())
                .studentName(student.getName())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .enrolledAt(enrollment.getEnrolledAt())
                .enrollmentStatus(enrollment.getEnrollmentStatus())
                .build();
    }

    private  CancelEnrollmentResponseDTO mapToDto(Enrollment enrollment){
        Course course=enrollment.getCourse();
        User student=enrollment.getStudent();
        return CancelEnrollmentResponseDTO.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .enrollmentStatus(enrollment.getEnrollmentStatus())
                .build();
    }


    private CourseResponseDto mapToCourseDto(Course course){
        return CourseResponseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .status(course.getStatus())
                .instructorName(course.getInstructor().getName())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    public ApiResponseDto<List<CourseResponseDto>> getAllEnrolledCourses() {
        List<Enrollment> enrollments=enrollmentRepository.findAll();
        List<CourseResponseDto> courseResponseDtos=enrollments.stream()
                .filter(e->e.getEnrollmentStatus()==EnrollmentStatus.ENROLLED)
                .map(e->mapToCourseDto(e.getCourse()))
                .collect(Collectors.toList());
        return new ApiResponseDto<>(true,"Fetched All Enrolled Courses successfully",courseResponseDtos);
    }
}
