package com.nzube.bookingsystem.service;

import com.nzube.bookingsystem.exception.EventNotFoundException;
import com.nzube.bookingsystem.model.Bookings;
import com.nzube.bookingsystem.dto.BookingsResponseDto;
import com.nzube.bookingsystem.model.Event;
import com.nzube.bookingsystem.model.Seat;
import com.nzube.bookingsystem.repo.BookingsRepo;
import com.nzube.bookingsystem.repo.EventRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    private final EventRepo eventRepo;
    private final BookingsRepo bookingsRepo;


    public EventService (EventRepo eventRepo, BookingsRepo bookingsRepo){
        this.eventRepo = eventRepo;
        this.bookingsRepo = bookingsRepo;
    }

    public Event createEvent(String name,int rows, int seatPerRows) {

        List<Seat> seats = new ArrayList<>();

        Event event = new Event();
        event.setName(name);
        event.setRows(rows);
        event.setSeatPerRows(seatPerRows);

        for (int i = 0; i < rows; i++) {
            char row = (char) ('A' + i);

            for (int j = 1; j <= seatPerRows; j++) {
                Seat seat = new Seat();
                seat.setEvent(event);
                seat.setRow(row);
                seat.setStatus("available");
                seat.setSeatNumber(j);
                seats.add(seat);
            }
        }
        event.setSeats(seats);
        return eventRepo.save(event);
    }

    public List<Event> getEvents() {

        return eventRepo.findAll();
    }

    public List<Seat> getEventSeats(int eventId) {
        Event event = eventRepo.findById(eventId).orElseThrow(()->new EventNotFoundException("Event not found"));
        return event.getSeats();
    }

    public Event getEvent(int eventId) {
        return eventRepo.findById(eventId).orElseThrow(()->new EventNotFoundException("Event not found"));

    }

    public List<BookingsResponseDto> getEventBookings(int eventId) {
        List<Bookings> bookings = bookingsRepo.findByEvent_Id(eventId);

        return bookings.stream().map(BookingsResponseDto::from).toList();
    }
}
