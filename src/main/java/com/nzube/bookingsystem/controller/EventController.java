package com.nzube.bookingsystem.controller;


import com.nzube.bookingsystem.dto.BookingsResponseDto;
import com.nzube.bookingsystem.dto.CreateEventDto;
import com.nzube.bookingsystem.model.Event;
import com.nzube.bookingsystem.model.Seat;
import com.nzube.bookingsystem.dto.UserPrincipal;
import com.nzube.bookingsystem.service.EventService;
import com.nzube.bookingsystem.service.SeatService;
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
@RequestMapping("api/v1/events")
@NullMarked
public class EventController {

    private final EventService eventService;
    private final SeatService seatService;

    @Autowired
    public EventController(EventService eventService, SeatService seatService){
        this.eventService = eventService;
        this.seatService = seatService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody CreateEventDto createEvent){
        return new ResponseEntity<>(
                eventService.createEvent(createEvent.name(),createEvent.rows(),createEvent.seatPerRows()),
                HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Event>> getEvents(){
        return new ResponseEntity<>(eventService.getEvents(), HttpStatus.OK);
    }

    @GetMapping("{eventId}")
    public ResponseEntity<Event> getEvents(@PathVariable int eventId){
        return new ResponseEntity<>(eventService.getEvent(eventId), HttpStatus.OK);
    }

    @GetMapping("{eventId}/seats")
    public ResponseEntity<List<Seat>> eventSeats(@PathVariable int eventId){
        return new ResponseEntity<>(eventService.getEventSeats(eventId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("{eventId}/bookings")
    public ResponseEntity<List<BookingsResponseDto>> eventBookings(@PathVariable int eventId){
        return new ResponseEntity<>(eventService.getEventBookings(eventId), HttpStatus.OK);
    }

    @PostMapping("{eventId}/seats/{seatId}/hold")
    public ResponseEntity<Seat> holdSeat(@PathVariable int eventId, @PathVariable int seatId, @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(seatService.holdSeat(eventId, seatId, principal.user().getId()), HttpStatus.OK);
    }

    @DeleteMapping("{eventId}/seats/{seatId}/hold")
    public ResponseEntity<Void> releaseSeat(@PathVariable int eventId, @PathVariable int seatId, @AuthenticationPrincipal UserPrincipal principal) {
        seatService.releaseHold(eventId, seatId, principal.user().getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
