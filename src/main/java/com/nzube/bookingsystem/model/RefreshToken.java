package com.nzube.bookingsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String tokenHash;
    private Instant expiresAt;
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private boolean revoked;

}
