package com.nzube.bookingsystem.repo;

import com.nzube.bookingsystem.model.RefreshToken;
import com.nzube.bookingsystem.model.User;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@NullMarked
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserAndRevokedFalse(User user);
}
