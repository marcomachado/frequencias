package com.petsaudedigital.backend.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.access-token-ttl-seconds}")
    private long accessTtlSeconds;

    @Value("${security.jwt.refresh-token-ttl-seconds}")
    private long refreshTtlSeconds;

    public String extractUsername(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new MACVerifier(secret.getBytes()))) return false;
            Date exp = jwt.getJWTClaimsSet().getExpirationTime();
            if (exp == null || exp.before(new Date())) return false;
            String sub = jwt.getJWTClaimsSet().getSubject();
            return userDetails.getUsername().equals(sub);
        } catch (Exception e) {
            return false;
        }
    }

    public String generateAccessToken(long userId, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlSeconds);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(email)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .claim("uid", userId)
                .claim("roles", roles)
                .claim("typ", "access")
                .build();
        return sign(claims);
    }

    public String generateRefreshToken(long userId, String email, UUID jti) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshTtlSeconds);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(email)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .jwtID(jti.toString())
                .claim("uid", userId)
                .claim("typ", "refresh")
                .build();
        return sign(claims);
    }

    public boolean isRefreshToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            Object typ = jwt.getJWTClaimsSet().getClaim("typ");
            return "refresh".equals(typ);
        } catch (Exception e) {
            return false;
        }
    }

    public UUID extractJti(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            String jti = jwt.getJWTClaimsSet().getJWTID();
            return jti != null ? UUID.fromString(jti) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(JWTClaimsSet claims) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            SignedJWT signedJWT = new SignedJWT(header, claims);
            JWSSigner signer = new MACSigner(secret.getBytes());
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }
}

