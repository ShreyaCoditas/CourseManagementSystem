package com.example.coursemanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.coursemanagementsystem.constants.Status;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseResponseDto {
    private Long id;
    private String title;
    private String description;
    private Status status;
    private String instructorName;
    private Double price;
    private String courseType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
