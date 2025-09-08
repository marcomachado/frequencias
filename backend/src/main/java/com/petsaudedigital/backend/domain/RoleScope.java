package com.petsaudedigital.backend.domain;

import com.petsaudedigital.backend.domain.enums.ScopeType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_scopes")
@Getter
@Setter
public class RoleScope {
    @EmbeddedId
    private Id id;

    @MapsId("userId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements java.io.Serializable {
        @Column(name = "user_id")
        private Long userId;
        @Column(name = "role")
        private String role;
        @Enumerated(EnumType.STRING)
        @Column(name = "scope_type")
        private ScopeType scopeType;
        @Column(name = "scope_id")
        private Long scopeId;
    }
}

