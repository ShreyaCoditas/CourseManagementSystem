package com.example.coursemanagementsystem.dto;

import com.example.coursemanagementsystem.constants.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancelEnrollmentResponseDTO {
    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private Long studentId;
    private String studentName;
    private EnrollmentStatus enrollmentStatus;
}
