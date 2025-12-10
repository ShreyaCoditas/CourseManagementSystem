package com.example.coursemanagementsystem.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseDto {
    private String title;
    private String description;
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;
}
