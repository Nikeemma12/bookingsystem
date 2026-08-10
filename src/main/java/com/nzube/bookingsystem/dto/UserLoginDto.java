package com.nzube.bookingsystem.dto;

import jakarta.validation.constraints.*;

public record UserLoginDto(
        @NotBlank(message="Email can't be blank")
        @Email(message="Email must be in correct format")
        String email,
        @NotBlank(message= "Password can't be blank")
        String password
        ) {
}
