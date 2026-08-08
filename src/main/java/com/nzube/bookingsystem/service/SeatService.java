package com.nzube.bookingsystem.service;

import com.nzube.bookingsystem.exception.SeatHoldNotOwnedException;
import com.nzube.bookingsystem.exception.SeatNotFoundException;
import com.nzube.bookingsystem.exception.SeatUnavailableException;
import com.nzube.bookingsystem.model.Seat;
import com.nzube.bookingsystem.repo.SeatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SeatService {

    private final SeatRepo seatRepo;

    @Autowired
    public SeatService(SeatRepo seatRepo) {
        this.seatRepo = seatRepo;
    }

    @Transactional
    public Seat holdSeat(int eventId, int seatId, int userId) {

        Seat seat = seatRepo.findById(seatId)
                .orElseThrow(() -> new SeatNotFoundException("Seat not found"));

        if (seat.getEvent().getId()!=eventId) {
            throw new SeatNotFoundException("Seat does not belong to this event");
        }

        if(seat.getStatus().equals("booked")){
            throw new SeatUnavailableException("Seat already Booked");
        }


        //CHECKS WHETHER SEAT HOLD HAS EXPIRED
        boolean hasExpiredHold = "held".equals(seat.getStatus())
                && seat.getHeldUntil() != null
                && seat.getHeldUntil().isBefore(LocalDateTime.now());

        if ("held".equals(seat.getStatus()) && !hasExpiredHold) {
            throw new SeatUnavailableException("Seat already held by another user");
        }

        seat.setStatus("held");
        seat.setHeldByUserId(userId);
        seat.setHeldUntil(LocalDateTime.now().plusMinutes(5));

        try {
            return seatRepo.save(seat);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new SeatUnavailableException("Seat was just taken by someone else");
        }
    }

    @Transactional
    public void releaseHold(int eventId, int seatId, int userId){
        Seat seat = seatRepo.findById(seatId).orElseThrow(()->new SeatNotFoundException("Seat not found"));

        if (seat.getEvent().getId()!=eventId) {
            throw new SeatNotFoundException("Seat does not belong to this event");
        }

        if (!"held".equals(seat.getStatus())) {
            throw new SeatUnavailableException("Seat is not currently held");
        }

        if(userId!=seat.getHeldByUserId()){
            throw new SeatHoldNotOwnedException("You dont own this seat");
        }

        seat.setStatus("available");
        seat.setHeldByUserId(0);
        seat.setHeldUntil(null);

        try {
        seatRepo.save(seat);
        } catch (ObjectOptimisticLockingFailureException e){
            throw new SeatUnavailableException("Seat state changed while releasing, please refresh and try again");
        }
    }
}
