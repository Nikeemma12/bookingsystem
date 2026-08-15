package com.nzube.bookingsystem.controller;


import com.nzube.bookingsystem.dto.*;
import com.nzube.bookingsystem.model.User;
import com.nzube.bookingsystem.service.BookingService;
import com.nzube.bookingsystem.service.RefreshTokenService;
import com.nzube.bookingsystem.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.*;


import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("api/v1/users")
@NullMarked
public class UserController {

    private final UserService userService;
    private final BookingService bookService;
    private final RefreshTokenService refreshTokenService;
    
    @Autowired
    public UserController(UserService userService, BookingService bookService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.bookService = bookService;
        this.refreshTokenService = refreshTokenService;
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
    public ResponseEntity<String> loginUser(@Valid @RequestBody UserLoginDto user, HttpServletResponse httpServletResponse){
        LoginResult loginResult = userService.loginUser(user.email(), user.password());
        String accessToken = loginResult.accessToken();
        String refreshToken  = loginResult.refreshToken();

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/users/auth")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return new ResponseEntity<>(accessToken, HttpStatus.OK);
    }

    @DeleteMapping("auth/logout")
    public ResponseEntity<Void> logoutUser(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse httpServletResponse){
        if(refreshToken!=null) userService.logoutUser(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/users/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("auth/refresh")
    public ResponseEntity<String> refreshToken(@CookieValue("refreshToken") String refreshToken, HttpServletResponse httpServletResponse){

        RefreshResult refreshResult = refreshTokenService.generateNewTokens(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshResult.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/users/auth")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new ResponseEntity<>(refreshResult.accessToken(), HttpStatus.OK);
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
