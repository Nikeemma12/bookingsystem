package com.nzube.bookingsystem.service;

import com.nzube.bookingsystem.model.Seat;
import com.nzube.bookingsystem.repo.SeatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SeatHoldExpiryService {

    private final SeatRepo seatRepo;

    @Autowired
    public SeatHoldExpiryService(SeatRepo seatRepo) {
        this.seatRepo = seatRepo;
    }

    @Scheduled(fixedRate = 60000) // runs every 60 seconds
    @Transactional
    public void releaseExpiredHolds() {
        List<Seat> expired = seatRepo.findByStatusAndHeldUntilIsBefore("held", LocalDateTime.now());

        for (Seat seat : expired) {
            seat.setStatus("available");
            seat.setHeldByUserId(0);
            seat.setHeldUntil(null);
            seatRepo.save(seat);
        }
    }

}
