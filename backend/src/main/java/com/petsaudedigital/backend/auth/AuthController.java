package com.petsaudedigital.backend.auth;

import com.petsaudedigital.backend.auth.dto.LoginRequest;
import com.petsaudedigital.backend.auth.dto.RefreshRequest;
import com.petsaudedigital.backend.auth.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest req) {
        TokenResponse tokens = authService.login(req.email(), req.senha());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest req) {
        TokenResponse tokens = authService.refresh(req.refresh_token());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshRequest req) {
        authService.logout(req.refresh_token());
        return ResponseEntity.noContent().build();
    }
}

