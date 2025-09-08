package com.petsaudedigital.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("scope")
@RequiredArgsConstructor
public class ScopeService {

    private final JdbcTemplate jdbcTemplate;

    public boolean has(Authentication authentication, String permission, String scopeType, Long scopeId) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        String username = authentication.getName();

        Long userId = jdbcTemplate.query(
                "SELECT id FROM \"user\" WHERE email = ?",
                ps -> ps.setString(1, username),
                rs -> rs.next() ? rs.getLong(1) : null
        );
        if (userId == null) return false;

        // Coordenador Geral (GLOBAL) tem acesso amplo.
        List<String> roles = jdbcTemplate.query(
                "SELECT role FROM user_roles WHERE user_id = ?",
                (rs, rn) -> rs.getString(1), userId
        );
        boolean isCoordGeral = roles.stream().anyMatch(r -> r.equalsIgnoreCase("coordenador_geral"));
        if (isCoordGeral) return true;

        // Permissões explícitas por escopo.
        Integer count = jdbcTemplate.query(
                "SELECT COUNT(1) FROM user_permissions WHERE user_id = ? AND permission = ? AND scope_type = ? AND (scope_id IS NULL OR scope_id = ?)",
                ps -> {
                    ps.setLong(1, userId);
                    ps.setString(2, permission);
                    ps.setString(3, scopeType);
                    if (scopeId == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setLong(4, scopeId);
                }, rs -> rs.next() ? rs.getInt(1) : 0);
        return count != null && count > 0;
    }
}
