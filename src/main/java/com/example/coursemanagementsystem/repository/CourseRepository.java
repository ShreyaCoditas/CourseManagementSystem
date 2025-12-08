package com.example.coursemanagementsystem.repository;

import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.entity.Course;
import com.example.coursemanagementsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course,Long> {

    List<Course> findByInstructorId(Long id);

    Page<Course> findByStatus(Status status, Pageable pageable);
}
