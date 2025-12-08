package com.example.coursemanagementsystem.service;
import com.example.coursemanagementsystem.security.UserPrincipal;

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
import com.example.coursemanagementsystem.exception.AlreadyEnrolledException;
import com.example.coursemanagementsystem.exception.InactiveCourseException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.repository.CourseRepository;
import com.example.coursemanagementsystem.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private  EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

//    public ApiResponseDto<List<CourseResponseDto>> getAllCourses() {
//        List<Course> courses=courseRepository.findByStatus(Status.ACTIVE);
//        List<CourseResponseDto> courseResponseDtos=courses.stream()
//                .map(this::mapToCourseDto)
//                .collect(Collectors.toList());
//        return new ApiResponseDto<>(true,"Fetched All Courses successfully",courseResponseDtos);
//    }

    public ApiResponseDto<Page<CourseResponseDto>> getAllCourses(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Course> coursePage = courseRepository.findByStatus(Status.ACTIVE, pageable);
        Page<CourseResponseDto> dtoPage = coursePage.map(this::mapToCourseDto);
        return new ApiResponseDto<>(true, "Fetched All Courses successfully", dtoPage);
    }

//    public ApiResponseDto<List<CourseResponseDto>> getEnrolledCourses(Long studentId) {
//        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndEnrollmentStatus(studentId, EnrollmentStatus.ENROLLED);
//        List<CourseResponseDto> courseResponseDtos = enrollments.stream()
//                .map(e -> mapToCourseDto(e.getCourse()))
//                .toList();
//        return new ApiResponseDto<>(true, "Fetched enrolled courses successfully", courseResponseDtos);
//    }

    public ApiResponseDto<Page<CourseResponseDto>> getEnrolledCourses(Long studentId,int pageSize,int pageNumber){
        Pageable pageable=PageRequest.of(pageNumber,pageSize);
        Page<Enrollment> enrollments=enrollmentRepository.findByStudentIdAndEnrollmentStatus(studentId,EnrollmentStatus.ENROLLED,pageable);
        Page<CourseResponseDto> courseResponseDtos=enrollments.map(e->mapToCourseDto(e.getCourse()));
        return new ApiResponseDto<>(true,"Fetched enrolled courses successfully",courseResponseDtos);
    }

    public ApiResponseDto<EnrollmentDTO> enrollIntoCourse(Long courseId, User student) {
        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->new ResourceNotFoundException("Course Not Found"));
        if(!Roles.STUDENT.equals(student.getRole()))
            throw new AccessDeniedException("Only students can enroll into courses");
        if(course.getStatus()== Status.INACTIVE)
            throw new InactiveCourseException("Cannot enroll into inactive courses");

        boolean alreadyenrolled=enrollmentRepository.existsByCourseIdAndStudentIdAndEnrollmentStatus(courseId,student.getId(),EnrollmentStatus.ENROLLED);
        if(alreadyenrolled)
            throw new AlreadyEnrolledException("Already enrolled in this course");
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
                .orElseThrow(()->new ResourceNotFoundException("Course Not Found"));

        if(course.getStatus()==Status.INACTIVE)
            throw new InactiveCourseException("Cannot cancel inactive status");

        if(!student.getRole().equals(Roles.STUDENT))
            throw new AccessDeniedException("Only Student can cancel the enrollment");

        Enrollment enrollment=enrollmentRepository.
                findByCourseIdAndStudentId(courseId,student.getId())
                        .orElseThrow(()->new ResourceNotFoundException("Enrollment Not found"));

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



}
