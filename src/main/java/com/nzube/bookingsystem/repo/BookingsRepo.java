package com.nzube.bookingsystem.repo;

import com.nzube.bookingsystem.model.Bookings;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@NullMarked
public interface BookingsRepo extends JpaRepository<Bookings, Integer> {
    Optional<Bookings> findByUserIdAndSeatIdAndIdempotencyKey(int userId, int seatId, String idempotencyKey);

    List<Bookings> findByEvent_Id(int eventId);


    List<Bookings> findByUserIdAndIdempotencyKey(int userId, String idempotencyKey);
}
