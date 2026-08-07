package com.nzube.bookingsystem.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "event_id")
    private Event event;
    private char row;
    private int seatNumber;
    private String status; //"available" | "held" | "booked"

    private LocalDateTime heldUntil;   // when the hold expires
    private int heldByUserId;
    @Version
    private int version;
}
