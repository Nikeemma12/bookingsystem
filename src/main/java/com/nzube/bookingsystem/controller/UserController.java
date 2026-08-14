package com.nzube.bookingsystem.controller;


import com.nzube.bookingsystem.dto.BookingsResponseDto;
import com.nzube.bookingsystem.dto.UserLoginDto;
import com.nzube.bookingsystem.dto.UserRegisterDto;
import com.nzube.bookingsystem.model.User;
import com.nzube.bookingsystem.model.UserPrincipal;
import com.nzube.bookingsystem.service.BookingService;
import com.nzube.bookingsystem.service.UserService;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers(){
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @PostMapping("auth/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegisterDto user){
        return new ResponseEntity<>(userService.registerUser(user), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("auth/admin/register")
    public ResponseEntity<User> registerByAdmin(@Valid @RequestBody UserRegisterDto user){
        return new ResponseEntity<>(userService.registerByAdmin(user), HttpStatus.CREATED);
    }

    @PostMapping("auth/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody UserLoginDto user){
        return new ResponseEntity<>(userService.loginUser(user.email(), user.password()), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("{userId}")
    public ResponseEntity<User> getUserById(@PathVariable int userId) {
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("{userId}/bookings")
    public ResponseEntity<List<BookingsResponseDto>> userBookings(@PathVariable int userId){
        return new ResponseEntity<>(userService.getUserBookings(userId), HttpStatus.OK);
    }

    @GetMapping("bookings")
    public ResponseEntity<List<BookingsResponseDto>> userBookings(@AuthenticationPrincipal UserPrincipal principal){
        return new ResponseEntity<>(userService.getUserBookings(principal.user().getId()), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("{userId}/bookings")
    public ResponseEntity<List<BookingsResponseDto>> bookSeat(@PathVariable int userId,
                             @RequestBody List<Integer> seatId,
                             @RequestHeader("Idempotency-Key") String idempotencyKey
    ){
        return new ResponseEntity<>(bookService.createBookings(userId, seatId, idempotencyKey),HttpStatus.OK);
    }

    @PostMapping("bookings")
    public ResponseEntity<List<BookingsResponseDto>> bookSeat(@AuthenticationPrincipal UserPrincipal principal,
                                                              @RequestBody List<Integer> seatId,
                                                              @RequestHeader("Idempotency-Key") String idempotencyKey
    ){
        return new ResponseEntity<>(bookService.createBookings(principal.user().getId(), seatId, idempotencyKey),HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{userId}/bookings/{idempotencyKey}")
    public ResponseEntity<Void> cancelBookings(@PathVariable int userId, @PathVariable String idempotencyKey){
        bookService.cancelBookings(userId, idempotencyKey);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @DeleteMapping("bookings/{idempotencyKey}")
    public ResponseEntity<Void> cancelBookings(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String idempotencyKey){
        bookService.cancelBookings(principal.user().getId(), idempotencyKey);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



}
