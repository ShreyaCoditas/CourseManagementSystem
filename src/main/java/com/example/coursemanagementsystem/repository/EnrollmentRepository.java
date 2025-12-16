package com.example.coursemanagementsystem.repository;

import com.example.coursemanagementsystem.constants.EnrollmentStatus;
import com.example.coursemanagementsystem.entity.Enrollment;
import com.example.coursemanagementsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment,Long> {
    boolean existsByCourseIdAndStudentIdAndEnrollmentStatus(Long courseId, Long id, EnrollmentStatus enrollmentStatus);

    Optional<Enrollment> findByCourseIdAndStudentId(Long courseId, Long id);

    Optional<Enrollment> findByCourseIdAndStudentIdAndEnrollmentStatus(
            Long courseId, Long studentId, EnrollmentStatus status);


    Optional<Enrollment> findByStudentId(Long studentId);

    long countByEnrollmentStatus(EnrollmentStatus enrollmentStatus);

//   List<Enrollment> findByStudentIdAndEnrollmentStatus(Long studentId, EnrollmentStatus enrollmentStatus);

    Page<Enrollment> findByStudentIdAndEnrollmentStatus(Long studentId, EnrollmentStatus enrollmentStatus, Pageable pageable);

    Page<Enrollment> findByCourseInstructorAndEnrollmentStatus(User instructor, EnrollmentStatus enrollmentStatus,Pageable pageable);

    int countByCourseIdAndEnrollmentStatus(Long courseId, EnrollmentStatus enrollmentStatus);


    Optional<Enrollment> findFirstByCourseIdAndEnrollmentStatusOrderByEnrolledAtAsc(Long courseId, EnrollmentStatus enrollmentStatus);
}
