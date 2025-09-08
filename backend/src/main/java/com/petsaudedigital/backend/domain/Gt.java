package com.petsaudedigital.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gt")
@Getter
@Setter
public class Gt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "axis_id")
    private Axis axis;

    @Column(nullable = false)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "coord_gt_user_id")
    private User coordGtUser;

    @Column(nullable = false)
    private Integer ativo = 1;
}

