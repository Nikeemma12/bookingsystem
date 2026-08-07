package com.nzube.bookingsystem.exception;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorEntity> handleUserError(UserNotFoundException e){

        ErrorEntity errorEntity =new ErrorEntity(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                "User not found"
        );
        return new ResponseEntity<>(errorEntity, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SeatNotFoundException.class)
    public ResponseEntity<ErrorEntity> handleSeatError(SeatNotFoundException e){

        ErrorEntity errorEntity =new ErrorEntity(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                "Seat not found"
        );
        return new ResponseEntity<>(errorEntity, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorEntity> handleEventError(EventNotFoundException e){

        ErrorEntity errorEntity =new ErrorEntity(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                "Event not found"
        );
        return new ResponseEntity<>(errorEntity, HttpStatus.BAD_REQUEST);
    }
}
