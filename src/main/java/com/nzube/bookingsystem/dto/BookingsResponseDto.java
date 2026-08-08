package com.nzube.bookingsystem.dto;

import com.nzube.bookingsystem.model.Bookings;

import java.time.LocalDateTime;

public record BookingsResponseDto(
        int id,
        int userId,
        String userName,
        int seatId,
        char seatRow,
        int seatNumber,
        int eventId,
        String eventName,
        String idempotencyKey,
        String status,
        LocalDateTime bookedAt
){
    public static BookingsResponseDto from(Bookings bookings){
        return new BookingsResponseDto(
                bookings.getId(),
                bookings.getUser().getId(),
                bookings.getUser().getName(),
                bookings.getSeat().getId(),
                bookings.getSeat().getRow(),
                bookings.getSeat().getSeatNumber(),
                bookings.getEvent().getId(),
                bookings.getEvent().getName(),
                bookings.getIdempotencyKey(),
                bookings.getStatus(),
                bookings.getBookedAt()
        );
    }
}
