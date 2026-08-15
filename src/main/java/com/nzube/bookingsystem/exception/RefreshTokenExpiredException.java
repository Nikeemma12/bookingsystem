package com.nzube.bookingsystem.exception;

public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException(String message){
        super(message);
    }
}
