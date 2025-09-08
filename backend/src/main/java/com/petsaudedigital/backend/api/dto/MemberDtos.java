package com.petsaudedigital.backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MemberDtos {
    public record Add(@NotNull Long userId, @NotBlank String roleInGt) {}
    public record Patch(@NotBlank String roleInGt) {}
}

