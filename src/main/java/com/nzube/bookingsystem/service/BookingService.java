package com.nzube.bookingsystem.service;

import com.nzube.bookingsystem.exception.*;
import com.nzube.bookingsystem.model.Bookings;
import com.nzube.bookingsystem.model.BookingsResponseDto;
import com.nzube.bookingsystem.model.Seat;
import com.nzube.bookingsystem.model.User;
import com.nzube.bookingsystem.repo.BookingsRepo;
import com.nzube.bookingsystem.repo.SeatRepo;
import com.nzube.bookingsystem.repo.UserRepo;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final UserRepo userRepo;
    private final SeatRepo seatRepo;
    private final BookingsRepo bookingRepo;


    public BookingService(UserRepo userRepo, SeatRepo seatRepo, BookingsRepo bookingRepo) {
        this.userRepo = userRepo;
        this.seatRepo = seatRepo;
        this.bookingRepo = bookingRepo;
    }

    @Transactional
    public List<BookingsResponseDto> createBookings(int userId, List<Integer> seatIds, String idempotencyKey) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Bookings> result = new ArrayList<>();

        for (int seatId : seatIds) {

            Optional<Bookings> existing = bookingRepo.findByUserIdAndSeatIdAndIdempotencyKey(
                    userId, seatId, idempotencyKey);
            if (existing.isPresent()) {
                result.add(existing.get());
                continue;
            }

            Seat seat = seatRepo.findById(seatId)
                    .orElseThrow(() -> new SeatNotFoundException("Seat not found: " + seatId));

            if(seat.getStatus().equals("booked")){
                throw new SeatUnavailableException("Seat already booked");
            }

            if(userId!=seat.getHeldByUserId() || !"held".equals(seat.getStatus())){
                throw new SeatHoldNotOwnedException("This user doesn't hold this seat");
            }


            if(seat.getHeldUntil().isBefore(LocalDateTime.now())){
                throw new SeatHoldNotOwnedException("Seat hold has expired");
            }

            seat.setStatus("booked");
            seat.setHeldByUserId(0);
            seat.setHeldUntil(null);


            try {
                seatRepo.save(seat);
            } catch (OptimisticLockException e) {
                throw new RuntimeException("Seat " + seatId + " was just booked by someone else");
            }

            Bookings booking = new Bookings();
            booking.setUser(user);
            booking.setSeat(seat);
            booking.setEvent(seat.getEvent());
            booking.setIdempotencyKey(idempotencyKey);
            booking.setStatus("confirmed");
            booking.setBookedAt(LocalDateTime.now());

            result.add(bookingRepo.save(booking));
        }

        return result.stream().map(BookingsResponseDto::from).toList();
    }

    @Transactional
    public void cancelBookings(int userId, String idempotencyKey) {

        List<Bookings> bookings = bookingRepo.findByUserIdAndIdempotencyKey(userId, idempotencyKey);

        if (bookings.isEmpty()) {
            throw new BookingsNotFound("No bookings found for this order");
        }

        for (Bookings booking : bookings) {
            if (!"cancelled".equals(booking.getStatus())) {
                booking.setStatus("cancelled");
                booking.getSeat().setStatus("available");
                bookingRepo.save(booking);
                seatRepo.save(booking.getSeat());
            }
        }
    }
}
