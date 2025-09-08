package com.petsaudedigital.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "project")
@Getter
@Setter
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    private String descricao;
    @Column(nullable = false)
    private Integer ativo = 1;
    @Column(name = "created_at", nullable = false)
    private String createdAt;
}

