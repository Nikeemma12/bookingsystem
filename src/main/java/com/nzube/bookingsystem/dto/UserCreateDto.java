package com.nzube.bookingsystem.dto;

import jakarta.validation.constraints.*;

public record UserCreateDto (

        @NotBlank(message="Name can't be null")
        String name
){}
