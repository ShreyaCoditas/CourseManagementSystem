package com.example.coursemanagementsystem.service;
import com.example.coursemanagementsystem.constants.PaymentStatus;
import com.example.coursemanagementsystem.dto.*;
import com.example.coursemanagementsystem.constants.EnrollmentStatus;
import com.example.coursemanagementsystem.constants.Roles;
import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.entity.Course;
import com.example.coursemanagementsystem.entity.Enrollment;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.exception.AlreadyEnrolledException;
import com.example.coursemanagementsystem.exception.InactiveCourseException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.repository.CourseRepository;
import com.example.coursemanagementsystem.repository.EnrollmentRepository;
import com.example.coursemanagementsystem.specifications.CourseSpecifications;
import com.example.coursemanagementsystem.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StudentService {

    @Autowired
    private  EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EmailUtil emailUtil;



    public ApiResponseDto<PaginatedResponse<CourseResponseDto>> getAllCourses(
            int pageNumber, int pageSize,
            String sortBy,String direction,
            String title,String instructorName,Status status,
            LocalDateTime fromDate,LocalDateTime toDate,
            String searchKeyword,
            Integer minStudents,Integer maxStudents) {

        Sort sort=direction.equalsIgnoreCase("desc")
                ?Sort.by(sortBy).descending()
                :Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize,sort);
        Status finalStatus = (status == null ? Status.ACTIVE : status);

        Specification<Course> spec=Specification.allOf(
                CourseSpecifications.hasTitle(title),
                CourseSpecifications.hasInstructorName(instructorName),
                CourseSpecifications.hasStatus(finalStatus),
                CourseSpecifications.createdBetween(fromDate,toDate),
                CourseSpecifications.globalSearch(searchKeyword)
        );

        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        Page<CourseResponseDto> dtoPage = coursePage.map(this::mapToCourseDto);
        PaginatedResponse response=new PaginatedResponse(dtoPage.getContent(),dtoPage.getNumber(), dtoPage.getSize(), dtoPage.getTotalElements(), dtoPage.getTotalPages());
        return new ApiResponseDto<>(true, "Fetched All Courses successfully", response);
    }

    
    public ApiResponseDto<PaginatedResponse<CourseResponseDto>> getEnrolledCourses(Long studentId,int pageSize,int pageNumber){
        Pageable pageable=PageRequest.of(pageNumber,pageSize);
        Page<Enrollment> enrollments=enrollmentRepository.findByStudentIdAndEnrollmentStatus(studentId,EnrollmentStatus.ENROLLED,pageable);
        Page<CourseResponseDto> courseResponseDtos=enrollments.map(e->mapToCourseDto(e.getCourse()));
        PaginatedResponse response=new PaginatedResponse(courseResponseDtos.getContent(),courseResponseDtos.getSize(),courseResponseDtos.getNumber(),courseResponseDtos.getTotalElements(),courseResponseDtos.getTotalPages());
        return new ApiResponseDto<>(true,"Fetched enrolled courses successfully",response);
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
        if (course.getPrice() != null && course.getPrice() > 0) {
            enrollment.setPaymentStatus(PaymentStatus.PENDING); // paid course → pending payment
        } else {
            enrollment.setPaymentStatus(PaymentStatus.PAID); // free course
        }
        Enrollment saved=enrollmentRepository.save(enrollment);

        emailUtil.sendEnrollmentEmail(
                student.getEmail(),
                "Enrollment Successful: " + course.getTitle(),
                student.getName(),
                course.getTitle(),
                course.getInstructor().getName(),
                enrollment.getEnrollmentStatus().name(),
                enrollment.getPaymentStatus().name()
        );


        return new ApiResponseDto<>(true,"Student enrolled into course successfully",mapToEnrollmentDTO(saved));

    }


    public ApiResponseDto<CancelEnrollmentResponseDTO> cancelEnrollmentIntoCourse(Long courseId, User student) {
        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->new ResourceNotFoundException("Course Not Found"));

        if(course.getStatus()==Status.INACTIVE)
            throw new InactiveCourseException("Cannot cancel inactive status");

        if(!student.getRole().equals(Roles.STUDENT))
            throw new AccessDeniedException("Only Student can cancel the enrollment");

        Enrollment enrollment=enrollmentRepository.findByCourseIdAndStudentId(courseId,student.getId())
                        .orElseThrow(()->new ResourceNotFoundException("Enrollment Not found"));

        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setEnrollmentStatus(EnrollmentStatus.CANCELLED);
        enrollment.setCancelledAt(LocalDateTime.now());
        Enrollment saved=enrollmentRepository.save(enrollment);
        return new ApiResponseDto<>(true,"cancelled enrollment successfully",mapToDto(saved));
    }


    public ApiResponseDto<String> purchaseCourse(Long courseId, User student) {
        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->new ResourceNotFoundException("Course Not Found"));

        if(course.getStatus()==Status.INACTIVE)
            throw new InactiveCourseException("Cannot cancel inactive status");

        if(!Roles.STUDENT.equals(student.getRole()))
            throw new AccessDeniedException("Only students are allowed to purchase the Courses");

        Enrollment enrollment = enrollmentRepository
                .findByCourseIdAndStudentIdAndEnrollmentStatus(courseId, student.getId(),EnrollmentStatus.ENROLLED)
                .orElseThrow(() -> new ResourceNotFoundException("Enroll before purchasing"));

        if (enrollment.getPaymentStatus() == PaymentStatus.PAID)
            return new ApiResponseDto<>(true, "Course already purchased", "PAID");

        enrollment.setPaymentStatus(PaymentStatus.PAID);
        enrollmentRepository.save(enrollment);

        emailUtil.sendPaymentConfirmationEmail(
                student.getEmail(),
                student.getName(),
                course.getTitle(),
                course.getInstructor().getName(),
                course.getPrice(),
                LocalDateTime.now().toString()
        );

        return new ApiResponseDto<>(true, "Payment successful. Course unlocked!", "PAID");
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
                .courseType(course.getPrice() != null && course.getPrice() > 0 ? "PAID" : "FREE")
                .price(course.getPrice())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }



}
