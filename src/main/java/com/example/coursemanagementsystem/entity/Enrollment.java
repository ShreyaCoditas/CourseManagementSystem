package com.example.coursemanagementsystem.entity;

import com.example.coursemanagementsystem.constants.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="enrollment")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="course_id")
    private Course course;

    @Column(name="enrolled_at")
    private LocalDateTime enrolledAt;

    @Column(name="cancelled_at")
    private LocalDateTime cancelledAt;

    @Enumerated(EnumType.STRING)
    @Column(name="enrollment_status")
    private EnrollmentStatus enrollmentStatus;

    @PrePersist
    private void onCreate(){
        enrolledAt=LocalDateTime.now();
        cancelledAt=LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate(){
        cancelledAt=LocalDateTime.now();
    }

}
