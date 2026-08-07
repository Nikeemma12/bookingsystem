package com.nzube.bookingsystem.controller;


import com.nzube.bookingsystem.model.BookingsResponseDto;
import com.nzube.bookingsystem.model.Event;
import com.nzube.bookingsystem.model.Seat;
import com.nzube.bookingsystem.service.EventService;
import com.nzube.bookingsystem.service.SeatService;
import org.apache.coyote.Response;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event){

        return new ResponseEntity<>(eventService.createEvent(event), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Event>> getEvents(){
        return new ResponseEntity<>(eventService.getEvents(), HttpStatus.OK);
    }

    @GetMapping("{event_id}")
    public ResponseEntity<Event> getEvents(@PathVariable int event_id){
        return new ResponseEntity<>(eventService.getEvent(event_id), HttpStatus.OK);
    }

    @GetMapping("{eventId}/seats")
    public ResponseEntity<List<Seat>> eventSeats(@PathVariable int eventId){
        return new ResponseEntity<>(eventService.getEventSeats(eventId), HttpStatus.OK);
    }

    @GetMapping("{eventId}/bookings")
    public ResponseEntity<List<BookingsResponseDto>> eventBookings(@PathVariable int eventId){
        return new ResponseEntity<>(eventService.getEventBookings(eventId), HttpStatus.OK);
    }

    @PostMapping("/{eventId}/seats/{seatId}/hold")
    public ResponseEntity<Seat> holdSeat(@PathVariable int eventId, @PathVariable int seatId, @RequestParam int userId) {
        return new ResponseEntity<>(seatService.holdSeat(eventId, seatId, userId), HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}/seats/{seatId}/hold")
    public ResponseEntity<Void> releaseSeat(@PathVariable int eventId, @PathVariable int seatId, @RequestParam int userId) {
        seatService.releaseHold(eventId, seatId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
