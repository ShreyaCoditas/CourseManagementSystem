package com.example.coursemanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AdminDashboardDto {
    private Long totalStudents;
    private Long totalInstructors;
    private Long totalCourses;
    private Long totalEnrollments;
    private Long activeEnrollments;
    private Long cancelledEnrollments;
}
