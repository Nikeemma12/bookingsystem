package com.nzube.bookingsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data

@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","seat_id","idempotencyKey"}))
public class Bookings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Id;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="event_id")
    private Event event;

    private String idempotencyKey;
    private String status; // "pending" | "confirmed" | "cancelled"
    private LocalDateTime bookedAt;
}
