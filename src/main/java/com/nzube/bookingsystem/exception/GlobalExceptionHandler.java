package com.nzube.bookingsystem.exception;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorEntity> handleUserNotFoundError(UserNotFoundException e){

        ErrorEntity errorEntity =new ErrorEntity(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                e.getMessage(),
                "User not found"
        );
        return new ResponseEntity<>(errorEntity, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SeatNotFoundException.class)
    public ResponseEntity<ErrorEntity> handleSeatNotFoundError(SeatNotFoundException e){

        ErrorEntity errorEntity =new ErrorEntity(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                e.getMessage(),
                "Seat not found"
        );
        return new ResponseEntity<>(errorEntity, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorEntity> handleEventNotFoundError(EventNotFoundException e){

        ErrorEntity errorEntity =new ErrorEntity(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                e.getMessage(),
                "Event not found"
        );
        return new ResponseEntity<>(errorEntity, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BookingsNotFound.class)
    public ResponseEntity<ErrorEntity> handleBookingsNotFoundError(BookingsNotFound e){
        ErrorEntity errorEntity = new ErrorEntity(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                e.getMessage(),
                "Bookings not found"
        );

        return new ResponseEntity<>(errorEntity, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SeatHoldNotOwnedException.class)
    public ResponseEntity<ErrorEntity> handleSeatNotHeldByUser(SeatHoldNotOwnedException e){
        ErrorEntity errorEntity = new ErrorEntity(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                e.getMessage(),
                "User doesn't own this seat"
        );

        return new ResponseEntity<>(errorEntity, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SeatUnavailableException.class)
    public ResponseEntity<ErrorEntity> handleSeatUnavailable(SeatUnavailableException e){
        ErrorEntity errorEntity = new ErrorEntity(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                e.getMessage(),
                "Seat is unavailable"
        );

        return new ResponseEntity<>(errorEntity, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorEntity> handleGenericErrors(Exception e){

        ErrorEntity errorEntity = new ErrorEntity(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getMessage(),
                "Something went wrong"
        );

        return new ResponseEntity<>(errorEntity, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorEntity> handleBadCredentialsError(BadCredentialsException e){

        ErrorEntity errorEntity = new ErrorEntity(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage(),
                "Something went wrong"
        );

        return new ResponseEntity<>(errorEntity, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorEntity> handleNonValidErrorTypes(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream().map(fieldError ->
                fieldError
                        .getField() + ": " + (fieldError
                        .getDefaultMessage() != null ? fieldError.getDefaultMessage(): "Invalid value"))
                .collect(Collectors.joining(","));

        ErrorEntity errorEntity = new ErrorEntity(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                message,
                "Invalid Value"

        );
        return new ResponseEntity<>(errorEntity, HttpStatus.BAD_REQUEST);
    }
}
