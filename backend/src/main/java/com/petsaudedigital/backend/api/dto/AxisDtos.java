package com.petsaudedigital.backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AxisDtos {
    public record Create(@NotBlank String nome, Long coordEixoUserId) {}
    public record View(Long id, @NotNull Long projectId, String nome, Long coordEixoUserId, Integer ativo) {}
}

