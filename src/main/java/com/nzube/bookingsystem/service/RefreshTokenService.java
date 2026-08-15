package com.nzube.bookingsystem.service;


import com.nzube.bookingsystem.dto.RefreshResult;
import com.nzube.bookingsystem.exception.RefreshTokenExpiredException;
import com.nzube.bookingsystem.exception.RefreshTokenNotFoundException;
import com.nzube.bookingsystem.exception.RefreshTokenReuseException;
import com.nzube.bookingsystem.model.RefreshToken;
import com.nzube.bookingsystem.model.User;
import com.nzube.bookingsystem.repo.RefreshTokenRepo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;


import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepo refreshTokenRepo;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepo refreshTokenRepo,JwtService jwtService) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.jwtService = jwtService;
    }

    public String generateRefreshToken(User user){

        RefreshToken refreshToken = new RefreshToken();

        String token = UUID.randomUUID().toString();

        String tokenHash = hashToken(token);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plus(Duration.ofDays(7)));

        refreshTokenRepo.save(refreshToken);
        return token;


    }

    private String hashToken(String token){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] tokenHash  = md.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(tokenHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public RefreshResult generateNewTokens(String refreshToken) {

        String tokenHash = hashToken(refreshToken);
        RefreshToken refreshToken1  = refreshTokenRepo.findByTokenHash(tokenHash).orElseThrow(()->new RefreshTokenNotFoundException("Token not found"));
        User user = refreshToken1.getUser();

        if(refreshToken1.isRevoked()){
            List<RefreshToken> activeTokens = refreshTokenRepo.findByUserAndRevokedFalse(user);
            activeTokens.forEach(t->t.setRevoked(true));

            refreshTokenRepo.saveAll(activeTokens);
            throw new RefreshTokenReuseException("Refresh token has been used");
        }

        if(refreshToken1.getExpiresAt().isBefore(Instant.now())){
            throw new RefreshTokenExpiredException("Refresh token has expired");
        }

        refreshToken1.setRevoked(true);
        String accessToken = jwtService.generateToken(user.getEmail());
        String newRefreshToken = generateRefreshToken(user);

        return new RefreshResult(accessToken, newRefreshToken);
    }

    public void logoutUser(String refreshToken) {
        String tokenHash = hashToken(refreshToken);
        refreshTokenRepo.findByTokenHash(tokenHash)
                .ifPresent(
                        token-> {
                            token.setRevoked(true);
                            refreshTokenRepo.save(token);
                        }
                );
    }
}
