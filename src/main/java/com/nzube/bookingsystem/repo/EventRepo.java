package com.nzube.bookingsystem.repo;

import com.nzube.bookingsystem.model.Event;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@NullMarked
public interface EventRepo extends JpaRepository<Event, Integer> {
}
