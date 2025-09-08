package com.petsaudedigital.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "export_log")
@Getter
@Setter
public class ExportLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "scope_type")
    private String scopeType; // GLOBAL|AXIS|GT

    @Column(name = "scope_id")
    private Long scopeId;

    @Column(name = "filters_json")
    private String filtersJson;

    @Column(name = "schema_version")
    private String schemaVersion;

    @Column(name = "created_at")
    private String createdAt;
}

