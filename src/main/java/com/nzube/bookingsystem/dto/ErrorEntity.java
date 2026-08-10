package com.nzube.bookingsystem.dto;

import java.time.LocalDateTime;

public record ErrorEntity(
        LocalDateTime timestamp,
        int statusCode,
        String message,
        String details
) {
}
