package com.nzube.bookingsystem.controller;


import com.nzube.bookingsystem.dto.BookingsResponseDto;
import com.nzube.bookingsystem.dto.UserCreateDto;
import com.nzube.bookingsystem.model.User;
import com.nzube.bookingsystem.service.BookingService;
import com.nzube.bookingsystem.service.UserService;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
@NullMarked
public class UserController {

    private final UserService userService;
    private final BookingService bookService;
    
    @Autowired
    public UserController(UserService userService, BookingService bookService) {
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<User> addUser (@Valid @RequestBody UserCreateDto user){
        return new ResponseEntity<>(userService.addUser(user.name()), HttpStatus.CREATED);
    }

    @GetMapping("{userId}")
    public ResponseEntity<User> getUserById(@PathVariable int userId) {
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);

    }

    @GetMapping("{userId}/bookings")
    public ResponseEntity<List<BookingsResponseDto>> userBookings(@PathVariable int userId){
        return new ResponseEntity<>(userService.getUserBookings(userId), HttpStatus.OK);
    }

    @PostMapping("{userId}/bookings")
    public ResponseEntity<List<BookingsResponseDto>> bookSeat(@PathVariable int userId,
                             @RequestBody List<Integer> seatId,
                             @RequestHeader("Idempotency-Key") String idempotencyKey
    ){
        return new ResponseEntity<>(bookService.createBookings(userId, seatId, idempotencyKey),HttpStatus.OK);
    }

    @DeleteMapping("{userId}/bookings/{idempotencyKey}")
    public ResponseEntity<Void> cancelBookings(@PathVariable int userId, @PathVariable String idempotencyKey){
        bookService.cancelBookings(userId, idempotencyKey);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



}
