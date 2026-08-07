package com.nzube.bookingsystem.exception;

import java.time.LocalDateTime;

public record ErrorEntity(
        LocalDateTime timestamp,
        int statusCode,
        String message,
        String details
) {
}
