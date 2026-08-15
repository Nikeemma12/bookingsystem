package com.nzube.bookingsystem.exception;

public class RefreshTokenReuseException extends RuntimeException {

    public RefreshTokenReuseException(String message){
        super(message);
    }
}
