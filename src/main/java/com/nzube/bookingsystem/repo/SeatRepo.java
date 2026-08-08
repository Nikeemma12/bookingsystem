package com.nzube.bookingsystem.repo;

import com.nzube.bookingsystem.model.Seat;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@NullMarked
public interface SeatRepo extends JpaRepository<Seat, Integer> {

    List<Seat> findByStatusAndHeldUntilIsBefore(String held, LocalDateTime now);
}
