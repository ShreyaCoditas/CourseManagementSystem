package com.example.coursemanagementsystem.dto;

import com.example.coursemanagementsystem.constants.Roles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRegisterDto {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "email is required")
    @Email
    private String email;

    @NotBlank(message = "password is required")
    private String password;

   @NotNull(message = "role is required")
    private Roles role;
}
