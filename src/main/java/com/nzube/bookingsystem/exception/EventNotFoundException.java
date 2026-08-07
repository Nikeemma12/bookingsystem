package com.nzube.bookingsystem.exception;

public class EventNotFoundException extends RuntimeException{

    public EventNotFoundException(String message){
        super(message);
    }
}
