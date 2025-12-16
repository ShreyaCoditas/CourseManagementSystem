package com.example.coursemanagementsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCourseDto {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "description is required")
    private String description;
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;
    @NotNull(message = "Maximum capacity cannot be null")
    @Min(value=1, message  ="capacity should be one or more than one")
    private Integer maxCapacity;
}
