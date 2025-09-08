package com.petsaudedigital.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "axis")
@Getter
@Setter
public class Axis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "coord_eixo_user_id")
    private User coordEixoUser;

    @Column(nullable = false)
    private Integer ativo = 1;
}

