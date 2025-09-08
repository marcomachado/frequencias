package com.petsaudedigital.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
public class SystemSetting {
    @EmbeddedId
    private Id id;
    @Column(name = "value")
    private String value;

    @Embeddable
    @Getter
    @Setter
    public static class Id implements java.io.Serializable {
        @Column(name = "project_id")
        private Long projectId;
        @Column(name = "key")
        private String key;
        @Column(name = "scope_type")
        private String scopeType; // GLOBAL|AXIS|GT
        @Column(name = "scope_id")
        private Long scopeId;
        @Column(name = "effective_from")
        private String effectiveFrom; // ISO date
    }
}

