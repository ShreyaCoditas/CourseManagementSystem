package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.constants.EnrollmentStatus;
import com.example.coursemanagementsystem.constants.PaymentStatus;
import com.example.coursemanagementsystem.constants.Roles;
import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.dto.ApiResponseDto;
import com.example.coursemanagementsystem.dto.CancelEnrollmentResponseDTO;
import com.example.coursemanagementsystem.dto.EnrollmentDTO;
import com.example.coursemanagementsystem.entity.Course;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.entity.Enrollment;
import com.example.coursemanagementsystem.exception.AlreadyEnrolledException;
import com.example.coursemanagementsystem.exception.InactiveCourseException;
import com.example.coursemanagementsystem.repository.CourseRepository;
import com.example.coursemanagementsystem.repository.EnrollmentRepository;
import com.example.coursemanagementsystem.util.EmailUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EmailUtil emailUtil;

    @InjectMocks
    private StudentService studentService;


    // Helper methods
    private User createStudent() {
        User u = new User();
        u.setId(1L);
        u.setName("Shreya");
        u.setEmail("shreya@gmail.com");
        u.setRole(Roles.STUDENT);
        return u;
    }

    private Course createActiveCourse(Double price) {
        Course c = new Course();
        c.setId(10L);
        c.setTitle("Spring Boot");
        c.setStatus(Status.ACTIVE);
        c.setPrice(price);
        User instructor = new User();
        instructor.setName("Instructor");
        c.setInstructor(instructor);
        return c;
    }


    // enrollIntoCourse()

    @Test
    void enrollIntoCourse_success_freeCourse() {
        User student = createStudent();
        Course course = createActiveCourse(0.0);

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseIdAndStudentIdAndEnrollmentStatus(
                10L, 1L, EnrollmentStatus.ENROLLED)).thenReturn(false);

        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ApiResponseDto<EnrollmentDTO> response =
                studentService.enrollIntoCourse(10L, student);

        assertTrue(response.isSuccess());
        assertEquals(EnrollmentStatus.ENROLLED, response.getData().getEnrollmentStatus());

        verify(emailUtil, times(1))
                .sendEnrollmentEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void enrollIntoCourse_alreadyEnrolled_shouldThrowException() {
        User student = createStudent();
        Course course = createActiveCourse(0.0);

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseIdAndStudentIdAndEnrollmentStatus(
                10L, 1L, EnrollmentStatus.ENROLLED)).thenReturn(true);

        assertThrows(AlreadyEnrolledException.class,
                () -> studentService.enrollIntoCourse(10L, student));
    }

    @Test
    void enrollIntoCourse_inactiveCourse_shouldThrowException() {
        User student = createStudent();
        Course course = createActiveCourse(0.0);
        course.setStatus(Status.INACTIVE);

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        assertThrows(InactiveCourseException.class,
                () -> studentService.enrollIntoCourse(10L, student));
    }


    // cancelEnrollmentIntoCourse()
    @Test
    void cancelEnrollment_success() {
        User student = createStudent();
        Course course = createActiveCourse(0.0);

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setEnrollmentStatus(EnrollmentStatus.ENROLLED);

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseIdAndStudentId(10L, 1L))
                .thenReturn(Optional.of(enrollment));

        when(enrollmentRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        ApiResponseDto<CancelEnrollmentResponseDTO> response =
                studentService.cancelEnrollmentIntoCourse(10L, student);

        assertEquals(EnrollmentStatus.CANCELLED, response.getData().getEnrollmentStatus());
    }

    // purchaseCourse()
    @Test
    void purchaseCourse_success() {
        User student = createStudent();
        Course course = createActiveCourse(500.0);

        Enrollment enrollment = new Enrollment();
        enrollment.setPaymentStatus(PaymentStatus.PENDING);
        enrollment.setStudent(student);
        enrollment.setCourse(course);

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseIdAndStudentIdAndEnrollmentStatus(
                10L, 1L, EnrollmentStatus.ENROLLED))
                .thenReturn(Optional.of(enrollment));

        ApiResponseDto<String> response =
                studentService.purchaseCourse(10L, student);

        assertEquals("PAID", response.getData());

        verify(emailUtil, times(1))
                .sendPaymentConfirmationEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    void purchaseCourse_alreadyPaid() {
        User student = createStudent();
        Course course = createActiveCourse(500.0);

        Enrollment enrollment = new Enrollment();
        enrollment.setPaymentStatus(PaymentStatus.PAID);

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseIdAndStudentIdAndEnrollmentStatus(
                10L, 1L, EnrollmentStatus.ENROLLED))
                .thenReturn(Optional.of(enrollment));

        ApiResponseDto<String> response =
                studentService.purchaseCourse(10L, student);

        assertEquals("PAID", response.getData());
    }
}
