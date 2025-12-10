package com.example.coursemanagementsystem.repository;

import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.entity.Course;
import com.example.coursemanagementsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course,Long>, JpaSpecificationExecutor<Course> {

    List<Course> findByInstructorId(Long id);

    Page<Course> findByStatus(Status status, Pageable pageable);
}
