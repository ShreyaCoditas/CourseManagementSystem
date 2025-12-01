package com.example.coursemanagementsystem.repository;

import com.example.coursemanagementsystem.constants.EnrollmentStatus;
import com.example.coursemanagementsystem.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment,Long> {
    boolean existsByCourseIdAndStudentIdAndEnrollmentStatus(Long courseId, Long id, EnrollmentStatus enrollmentStatus);

    Optional<Enrollment> findByCourseIdAndStudentId(Long courseId, Long id);

    Optional<Enrollment> findByStudentId(Long studentId);
}
