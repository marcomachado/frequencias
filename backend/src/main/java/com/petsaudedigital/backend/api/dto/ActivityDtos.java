package com.petsaudedigital.backend.api.dto;

import com.petsaudedigital.backend.domain.enums.ActivityType;
import com.petsaudedigital.backend.domain.enums.TargetType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ActivityDtos {
    public record Create(
            @NotNull ActivityType tipo,
            @NotBlank String titulo,
            @NotBlank String data,
            @NotBlank String inicio,
            @NotBlank String fim,
            String local,
            String descricao,
            List<String> evidencias,
            @Valid Target target
    ) {}

    public record Target(@NotNull TargetType type, List<String> roles, List<Long> users) {}

    public record View(Long id) {}
}

