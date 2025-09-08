package com.petsaudedigital.backend.auth;

import com.petsaudedigital.backend.auth.dto.TokenResponse;
import com.petsaudedigital.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JdbcTemplate jdbcTemplate;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.access-token-ttl-seconds}")
    private long accessTtlSeconds;

    @Value("${security.jwt.refresh-token-ttl-seconds}")
    private long refreshTtlSeconds;

    public TokenResponse login(String email, String senha) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, senha));
        UserDetails principal = (UserDetails) auth.getPrincipal();

        Long userId = jdbcTemplate.query(
                "SELECT id FROM \"user\" WHERE email = ?",
                ps -> ps.setString(1, email),
                rs -> rs.next() ? rs.getLong(1) : null
        );
        if (userId == null) throw new IllegalStateException("Usuário não encontrado após autenticação");

        List<String> roles = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .map(a -> a.replaceFirst("ROLE_", ""))
                .collect(Collectors.toList());

        String access = jwtService.generateAccessToken(userId, email, roles);

        UUID jti = UUID.randomUUID();
        String refresh = jwtService.generateRefreshToken(userId, email, jti);

        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setJti(jti);
        rt.setExpiresAt(Instant.now().plusSeconds(refreshTtlSeconds));
        refreshTokenRepository.save(rt);

        return new TokenResponse(access, refresh, accessTtlSeconds);
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Token de refresh inválido");
        }
        String email = jwtService.extractUsername(refreshToken);
        UUID jti = jwtService.extractJti(refreshToken);
        if (email == null || jti == null) throw new IllegalArgumentException("Token inválido");

        RefreshToken stored = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token não encontrado"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expirado ou revogado");
        }

        Long userId = stored.getUserId();

        // Carrega roles atuais
        List<String> roles = jdbcTemplate.query(
                "SELECT role FROM user_roles WHERE user_id = ?",
                (rs, rn) -> rs.getString(1), userId
        );

        String access = jwtService.generateAccessToken(userId, email, roles);

        // Rotaciona refresh: revoga o atual e cria um novo
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        UUID newJti = UUID.randomUUID();
        String newRefresh = jwtService.generateRefreshToken(userId, email, newJti);
        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setJti(newJti);
        rt.setExpiresAt(Instant.now().plusSeconds(refreshTtlSeconds));
        refreshTokenRepository.save(rt);

        return new TokenResponse(access, newRefresh, accessTtlSeconds);
    }

    public void logout(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            return;
        }
        UUID jti = jwtService.extractJti(refreshToken);
        if (jti == null) return;
        refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }
}
