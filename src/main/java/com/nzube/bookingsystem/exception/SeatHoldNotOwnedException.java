package com.nzube.bookingsystem.exception;

public class SeatHoldNotOwnedException extends RuntimeException {
    public SeatHoldNotOwnedException(String message) {
        super(message);
    }
}
