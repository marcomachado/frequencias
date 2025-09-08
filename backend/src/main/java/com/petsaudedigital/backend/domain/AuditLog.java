package com.petsaudedigital.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    private String action;
    private String entity;
    @Column(name = "entity_id")
    private Long entityId;
    @Column(name = "payload_diff")
    private String payloadDiff;
    @Column(name = "created_at")
    private String createdAt;
}

