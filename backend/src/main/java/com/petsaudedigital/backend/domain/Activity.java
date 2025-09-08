package com.petsaudedigital.backend.domain;

import com.petsaudedigital.backend.domain.enums.ActivityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "activity")
@Getter
@Setter
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "gt_id")
    private Gt gt;

    @ManyToOne
    @JoinColumn(name = "subgroup_id")
    private Subgroup subgroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String data; // YYYY-MM-DD

    @Column(nullable = false)
    private String inicio; // HH:MM

    @Column(nullable = false)
    private String fim; // HH:MM

    private String local;
    private String descricao;
    @Column(name = "evidencias_json")
    private String evidenciasJson;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;
}

