package com.petsaudedigital.backend.domain;

import com.petsaudedigital.backend.domain.enums.ScopeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_permissions")
@Getter
@Setter
public class UserPermission {
    @EmbeddedId
    private Id id;

    @MapsId("userId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "valid_from")
    private String validFrom; // armazenado como TEXT (ISO) no SQLite

    @Column(name = "valid_until")
    private String validUntil;

    @ManyToOne
    @JoinColumn(name = "granted_by")
    private User grantedBy;

    @Column(name = "granted_at", nullable = false)
    private String grantedAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements java.io.Serializable {
        @Column(name = "user_id")
        private Long userId;
        @Column(name = "permission")
        private String permission;
        @Enumerated(EnumType.STRING)
        @Column(name = "scope_type")
        private ScopeType scopeType;
        @Column(name = "scope_id")
        private Long scopeId;
    }
}

