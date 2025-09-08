package com.petsaudedigital.backend.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsaudedigital.backend.TestDataConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDataConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void login_and_refresh_flow() throws Exception {
        // Login
        String payload = "{\n  \"email\": \"user@example.com\",\n  \"senha\": \"secret\"\n}";
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String accessToken = loginJson.get("access_token").asText();
        String refreshToken = loginJson.get("refresh_token").asText();

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        // Refresh
        String refreshPayload = "{\n  \"refresh_token\": \"" + refreshToken + "\"\n}";
        String refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode refreshJson = objectMapper.readTree(refreshResponse);
        String newAccess = refreshJson.get("access_token").asText();
        String newRefresh = refreshJson.get("refresh_token").asText();

        assertThat(newAccess).isNotBlank();
        assertThat(newRefresh).isNotBlank();
        assertThat(newAccess).isNotEqualTo(accessToken);
        assertThat(newRefresh).isNotEqualTo(refreshToken);
    }
}
