package com.nzube.bookingsystem.dto;

import jakarta.validation.constraints.*;

public record UserCreateDto (

        @NotBlank(message="Name can't be null")
        String name,

        @NotBlank(message = "Password can't be blank")
        String password,

        @Email(message="Email must be in correct format")
        String email,

        @NotNull(message = "Role can't be null")
        String role
){}
