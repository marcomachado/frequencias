package com.petsaudedigital.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public class GtDtos {
    public record Create(@NotBlank String nome, Long coordGtUserId) {}
    public record View(Long id, Long projectId, Long axisId, String nome, Long coordGtUserId, Integer ativo) {}
}

