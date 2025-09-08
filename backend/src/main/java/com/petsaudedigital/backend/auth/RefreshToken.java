package com.petsaudedigital.backend.auth;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import com.petsaudedigital.backend.config.InstantStringConverter;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID jti;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Convert(converter = InstantStringConverter.class)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Convert(converter = InstantStringConverter.class)
    private Instant createdAt = Instant.now();
}
