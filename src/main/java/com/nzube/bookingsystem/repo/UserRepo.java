package com.nzube.bookingsystem.repo;

import com.nzube.bookingsystem.model.User;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@NullMarked
public interface UserRepo extends JpaRepository<User, Integer> {
}
