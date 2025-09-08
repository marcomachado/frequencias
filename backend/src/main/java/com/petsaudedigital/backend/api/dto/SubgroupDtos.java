package com.petsaudedigital.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public class SubgroupDtos {
    public record Create(@NotBlank String nome, String descricao) {}
    public record View(Long id, Long projectId, Long gtId, String nome, String descricao) {}
}

