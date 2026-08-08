package com.nzube.bookingsystem.dto;

import jakarta.validation.constraints.*;

public record CreateEventDto(
        @NotBlank
        String name,
        @Min(value=1, message="Minimum rows is 1")
        @Max(value=20, message="Maximum rows is 20")
        int rows,
        @Min(value=1, message="Minimum seat per row is 1")
        @Max(value=20, message="Maximum seat per row is 20")
        int seatPerRows
) {}
