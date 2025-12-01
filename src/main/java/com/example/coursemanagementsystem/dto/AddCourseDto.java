package com.example.coursemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCourseDto {
    @NotBlank(message = "Title ios required")
    private String title;
    @NotBlank(message = "description is required")
    private String description;
}
