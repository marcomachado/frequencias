package com.petsaudedigital.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestDataConfig {

    @Bean
    CommandLineRunner seedTestData(JdbcTemplate jdbc, PasswordEncoder encoder) {
        return args -> {
            // Cria usuário de teste
            String email = "user@example.com";
            String nome = "Usuário Teste";
            String hash = encoder.encode("secret");

            jdbc.update("INSERT INTO \"user\" (nome, email, password_hash, ativo) VALUES (?,?,?,1)",
                    nome, email, hash);

            Long uid = jdbc.query("SELECT id FROM \"user\" WHERE email = ?",
                    ps -> ps.setString(1, email), rs -> rs.next() ? rs.getLong(1) : null);

            if (uid != null) {
                jdbc.update("INSERT INTO user_roles (user_id, role) VALUES (?,?)", uid, "tutor");
            }
        };
    }
}

