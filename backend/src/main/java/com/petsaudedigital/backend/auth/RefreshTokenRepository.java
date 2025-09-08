package com.petsaudedigital.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByJti(UUID jti);
    void deleteByExpiresAtBefore(Instant cutoff);
}

