package com.petsaudedigital.backend.api.dto;

import com.petsaudedigital.backend.domain.enums.ScopeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PermissionDtos {
    public record Grant(@NotNull Long userId,
                        @NotBlank String permission,
                        @NotNull ScopeType scopeType,
                        Long scopeId,
                        String validFrom,
                        String validUntil) {}

    public record Revoke(@NotNull Long userId,
                         @NotBlank String permission,
                         @NotNull ScopeType scopeType,
                         Long scopeId) {}
}

