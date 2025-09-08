package com.petsaudedigital.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_gt")
@Getter
@Setter
public class UserGt {
    @EmbeddedId
    private Id id;

    @MapsId("userId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("gtId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "gt_id")
    private Gt gt;

    @Column(name = "role_in_gt", nullable = false)
    private String roleInGt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements java.io.Serializable {
        @Column(name = "user_id")
        private Long userId;
        @Column(name = "gt_id")
        private Long gtId;
    }
}

