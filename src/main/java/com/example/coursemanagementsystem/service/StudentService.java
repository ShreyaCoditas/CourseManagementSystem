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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
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
        log.info("fetch all courses");

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
        log.info("fetched all courses");
        return new ApiResponseDto<>(true, "Fetched All Courses successfully", response);
    }

    
    public ApiResponseDto<PaginatedResponse<CourseResponseDto>> getEnrolledCourses(Long studentId,int pageSize,int pageNumber){
        log.info("To fetch all enrolled courses");
        Pageable pageable=PageRequest.of(pageNumber,pageSize);
        Page<Enrollment> enrollments=enrollmentRepository.findByStudentIdAndEnrollmentStatus(studentId,EnrollmentStatus.ENROLLED,pageable);
        Page<CourseResponseDto> courseResponseDtos=enrollments.map(e->mapToCourseDto(e.getCourse()));
        PaginatedResponse response=new PaginatedResponse(courseResponseDtos.getContent(),courseResponseDtos.getSize(),courseResponseDtos.getNumber(),courseResponseDtos.getTotalElements(),courseResponseDtos.getTotalPages());
        log.info("fetched enrolled courses successfully");
        return new ApiResponseDto<>(true,"Fetched enrolled courses successfully",response);
    }

    public ApiResponseDto<EnrollmentDTO> enrollIntoCourse(Long courseId, User student) {

        log.info("enroll into course request| studentId={} | courseId={}",student.getId(),courseId);

        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->{
                    log.warn("Course Not Found | courseId={}",courseId);
                   return new ResourceNotFoundException("Course Not Found");
                });


        if(!Roles.STUDENT.equals(student.getRole())) {
            log.warn("Unauthorised access attempt|userId={}",student.getId());
            throw new AccessDeniedException("Only students can enroll into courses");
        }

        if(course.getStatus()== Status.INACTIVE) {
            log.warn("Enroll attempt for inactive course| courseId={}",courseId);
            throw new InactiveCourseException("Cannot enroll into inactive courses");
        }

        boolean alreadyenrolled=enrollmentRepository.existsByCourseIdAndStudentIdAndEnrollmentStatus(courseId,student.getId(),EnrollmentStatus.ENROLLED);
        if(alreadyenrolled) {
            log.warn("Student already enrolled or waitlisted| userId={}| courseId={}",student.getId(),courseId);
            throw new AlreadyEnrolledException("Already enrolled or waitlisted in this course");
        }
        int enrolled=enrollmentRepository.countByCourseIdAndEnrollmentStatus(courseId,EnrollmentStatus.ENROLLED);
        log.debug("total enrolled students| enrolled count={} | maxCapacity={}",enrolled,course.getMaxCapacity());
        Enrollment enrollment=new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        if(enrolled<course.getMaxCapacity())
            enrollment.setEnrollmentStatus(EnrollmentStatus.ENROLLED);
        else
            enrollment.setEnrollmentStatus(EnrollmentStatus.WAITLISTED);
        if (course.getPrice() != null && course.getPrice() > 0) {
            enrollment.setPaymentStatus(PaymentStatus.PENDING); // paid course → pending payment
        } else {
            enrollment.setPaymentStatus(PaymentStatus.PAID); // free course
        }
        Enrollment saved=enrollmentRepository.save(enrollment);
        log.info("Student enrolled into course successfully| studentId={} | courseId={} | enrollmentStatus={}",student.getId(),courseId,saved.getEnrollmentStatus());

        emailUtil.sendEnrollmentEmail(
                student.getEmail(),
                "Enrollment Successful: " + course.getTitle(),
                student.getName(),
                course.getTitle(),
                course.getInstructor().getName(),
                enrollment.getEnrollmentStatus().name(),
                enrollment.getPaymentStatus().name()
        );
        log.debug("Enrollment email sent| studentId={} | courseId={}",student.getId(),courseId);
        return new ApiResponseDto<>(true,"Student enrolled into course successfully",mapToEnrollmentDTO(saved));
    }


    public ApiResponseDto<CancelEnrollmentResponseDTO> cancelEnrollmentIntoCourse(Long courseId, User student) {
        log.info("Cancel Enrollment request| studentId={} | courseId={}",student.getId(),courseId);

        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->{
                    log.warn("Course Not Found | courseId={}",courseId);
                     return new ResourceNotFoundException("Course Not Found");
                });


        if(course.getStatus()==Status.INACTIVE) {
            log.warn("cancel attempt for inactive course| courseId={} | studentId={}",courseId,student.getId());
            throw new InactiveCourseException("Cannot cancel inactive status");
        }

        if(!student.getRole().equals(Roles.STUDENT)) {
            log.warn("Unauthorised attempt to cancel the enrollment | courseId={} | userId={}",courseId,student.getId());
            throw new AccessDeniedException("Only Student can cancel the enrollment");
        }

        Enrollment enrollment=enrollmentRepository.findByCourseIdAndStudentId(courseId,student.getId())
                        .orElseThrow(()->{
                                    log.warn("Enrollment of the student is not found | studentId={} | courseId={}",student.getId(),courseId);
                                    return new ResourceNotFoundException("Enrollment Not found");
                        });


        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setEnrollmentStatus(EnrollmentStatus.CANCELLED);
        enrollment.setCancelledAt(LocalDateTime.now());
        Enrollment saved=enrollmentRepository.save(enrollment);
        log.info("Cancelled enrollment successfully| courseId={} | studentId={}",courseId,student.getId());
        autoEnrollNextStudent(courseId);
        return new ApiResponseDto<>(true,"cancelled enrollment successfully",mapToDto(saved));
    }

    private void autoEnrollNextStudent(Long courseId) {
        log.debug("checking waitlist for auto-enroll | courseId={}",courseId);
       Optional<Enrollment> waitlisted=enrollmentRepository.findFirstByCourseIdAndEnrollmentStatusOrderByEnrolledAtAsc(courseId,EnrollmentStatus.WAITLISTED);
       waitlisted.ifPresent(enrollment->{
           enrollment.setEnrollmentStatus(EnrollmentStatus.ENROLLED);
           Enrollment saved=enrollmentRepository.save(enrollment);
           log.info("auto-enrolled waitlisted student | courseId={} | studentId={}",courseId,saved.getStudent().getId());

           User student=saved.getStudent();
           Course course=saved.getCourse();

           emailUtil.sendEnrollmentEmail(
                   student.getEmail(),
                   "Enrollment Successful: " + course.getTitle(),
                   student.getName(),
                   course.getTitle(),
                   course.getInstructor().getName(),
                   enrollment.getEnrollmentStatus().name(),
                   enrollment.getPaymentStatus().name()
           );
       });
    }


    public ApiResponseDto<String> purchaseCourse(Long courseId, User student) {
        log.info("Purchase course request attempt| courseId={} | studentId={}",courseId,student.getId());
        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->{
                    log.warn("Course Not Found | courseId={}",courseId);
                     return new ResourceNotFoundException("Course Not Found");
                });


        if(course.getStatus()==Status.INACTIVE) {
            log.warn("attempt to purchase inactive course| courseId={} | status={}",courseId,course.getStatus());
            throw new InactiveCourseException("Cannot purchase inactive status");
        }

        if(!Roles.STUDENT.equals(student.getRole())) {
            log.warn("Unauthorised access to purchase course| courseId={} | role={} | userId={}",courseId,student.getRole(),student.getId());
            throw new AccessDeniedException("Only students are allowed to purchase the Courses");
        }

        Enrollment enrollment = enrollmentRepository
                .findByCourseIdAndStudentIdAndEnrollmentStatus(courseId, student.getId(),EnrollmentStatus.ENROLLED)
                .orElseThrow(() -> new ResourceNotFoundException("Enroll before purchasing"));

        if (enrollment.getPaymentStatus() == PaymentStatus.PAID)
            return new ApiResponseDto<>(true, "Course already purchased", "PAID");

        enrollment.setPaymentStatus(PaymentStatus.PAID);
        Enrollment saved= enrollmentRepository.save(enrollment);

        log.info("Payment successful | courseId={} | payamentStatus={}",courseId,saved.getPaymentStatus());

        emailUtil.sendPaymentConfirmationEmail(student.getEmail(), student.getName(), course.getTitle(), course.getInstructor().getName(),course.getPrice(), LocalDateTime.now().toString());
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
                .enrollmentId(enrollment.getId())
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
