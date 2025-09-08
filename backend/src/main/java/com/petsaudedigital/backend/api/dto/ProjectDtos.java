package com.petsaudedigital.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectDtos {
    public record Create(@NotBlank String nome, String descricao) {}
    public record View(Long id, String nome, String descricao, Integer ativo, String createdAt) {}
}

