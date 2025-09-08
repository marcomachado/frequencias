package com.petsaudedigital.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UserRecord user = jdbcTemplate.queryForObject(
                    "SELECT id, email, password_hash, ativo FROM \"user\" WHERE email = ?",
                    new Object[]{username}, new UserRowMapper());

            List<String> roles = jdbcTemplate.query(
                    "SELECT role FROM user_roles WHERE user_id = ?",
                    (rs, rowNum) -> rs.getString("role"),
                    user.id()
            );

            List<GrantedAuthority> authorities = roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());

            if (user.ativo() == 0) {
                throw new UsernameNotFoundException("Usuário inativo");
            }

            return User.withUsername(user.email())
                    .password(user.passwordHash())
                    .authorities(authorities)
                    .build();
        } catch (EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
    }

    record UserRecord(long id, String email, String passwordHash, int ativo) {}

    static class UserRowMapper implements RowMapper<UserRecord> {
        @Override
        public UserRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new UserRecord(
                    rs.getLong("id"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getInt("ativo")
            );
        }
    }
}

