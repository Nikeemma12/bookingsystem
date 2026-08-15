package com.nzube.bookingsystem.exception;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException(String message){
        super(message);
    }
}
