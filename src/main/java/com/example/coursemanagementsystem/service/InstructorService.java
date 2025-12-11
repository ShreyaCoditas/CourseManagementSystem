package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.constants.EnrollmentStatus;
import com.example.coursemanagementsystem.constants.Roles;
import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.dto.*;
import com.example.coursemanagementsystem.entity.Course;
import com.example.coursemanagementsystem.entity.Enrollment;
import com.example.coursemanagementsystem.entity.User;

import com.example.coursemanagementsystem.exception.InactiveCourseException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.repository.CourseRepository;
import com.example.coursemanagementsystem.repository.EnrollmentRepository;
import com.example.coursemanagementsystem.repository.UserRepository;
import com.example.coursemanagementsystem.security.UserPrincipal;
import jakarta.validation.Valid;
import org.apache.catalina.core.ApplicationPushBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstructorService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private UserRepository userRepository;



    public ApiResponseDto<Void> addCourse(@Valid AddCourseDto addCourseDto, User instructor) {
        if (!Roles.INSTRUCTOR.equals(instructor.getRole())) {
            throw new AccessDeniedException("Only instructors can add courses");
        }
        if (instructor.getStatus() == Status.INACTIVE) {
            throw new AccessDeniedException("Inactive instructors cannot add courses");
        }

        Course course=new Course();
        course.setTitle(addCourseDto.getTitle());
        course.setDescription(addCourseDto.getDescription());
        course.setInstructor(instructor);
        course.setStatus(Status.ACTIVE);
        course.setPrice(addCourseDto.getPrice());
        courseRepository.save(course);
        return new ApiResponseDto<>(true,"Course added successfully",null);



    }

    public ApiResponseDto<CourseDto> updateCourse(@Valid UpdateCourseDto updateCourseDto,Long courseId, User instructor) {
        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->new ResourceNotFoundException("Course Not Found Exception"));

        if (!Roles.INSTRUCTOR.equals(instructor.getRole())) {
            throw new AccessDeniedException("Only instructors can update courses");
        }

        if(course.getStatus()==Status.INACTIVE)
            throw new InactiveCourseException("cannot update inactive course");

        if(!course.getInstructor().getId().equals(instructor.getId()))
            throw new AccessDeniedException("you are not authorised to update other courses");

        course.setTitle(updateCourseDto.getTitle());
        course.setDescription(updateCourseDto.getDescription());
        if (updateCourseDto.getPrice() != null) {
            course.setPrice(updateCourseDto.getPrice());
        }

        courseRepository.save(course);
        return new ApiResponseDto<>(true,"Updated course successfully",convertToDto(course));
    }

    public ApiResponseDto<Void> deleteCourse(Long courseId,User instructor) {
        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->new ResourceNotFoundException("Course Not Found"));

        if(!course.getInstructor().getId().equals(instructor.getId()))
            throw new AccessDeniedException("You cannot delete others courses");

        course.setStatus(Status.INACTIVE);
        courseRepository.save(course);
        return new ApiResponseDto<>(true,"Course deleted successfully",null);
    }


    public ApiResponseDto<PaginatedResponse<StudentInfoDto>> getEnrolledStudents(User instructor,int pageSize,int pageNumber){
        if(!Roles.INSTRUCTOR.equals(instructor.getRole())){
            throw  new AccessDeniedException("Only instructors can view enrolled students");
        }
        Pageable pageable= PageRequest.of(pageNumber,pageSize);
        Page<Enrollment> enrollments=enrollmentRepository.findByCourseInstructorAndEnrollmentStatus(instructor,EnrollmentStatus.ENROLLED,pageable);
        Page<StudentInfoDto> dtoList=enrollments.map(this::mapToStudentInfoDto);
        PaginatedResponse<StudentInfoDto> response=new PaginatedResponse<>(dtoList.getContent(),dtoList.getNumber(),dtoList.getSize(),dtoList.getTotalElements(),dtoList.getTotalPages());
        return new ApiResponseDto<>(true,"Fetched enrolled students successfully",response);
    }

    public ApiResponseDto<Void> uploadImage(MultipartFile file, User instructor) {
        if(!Roles.INSTRUCTOR.equals(instructor.getRole())){
            throw new AccessDeniedException("Only instructors can upload profile pictures");
        }
        String imageUrl=cloudinaryService.uploadFile(file);
        instructor.setProfilePictureUrl(imageUrl);
        userRepository.save(instructor);
        return new ApiResponseDto<>(true,"Profile picture uploaded successfully",null);
    }



    private StudentInfoDto mapToStudentInfoDto(Enrollment enrollment) {
        User student = enrollment.getStudent();

        return StudentInfoDto.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .studentEmail(student.getEmail())
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }



    private CourseDto convertToDto(Course course){
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .courseType(course.getPrice() != null && course.getPrice() > 0 ? "PAID" : "FREE")
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }



}
