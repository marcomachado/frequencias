package com.petsaudedigital.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_subgroup")
@Getter
@Setter
public class UserSubgroup {
    @EmbeddedId
    private Id id;

    @MapsId("userId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("subgroupId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "subgroup_id")
    private Subgroup subgroup;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements java.io.Serializable {
        @Column(name = "user_id")
        private Long userId;
        @Column(name = "subgroup_id")
        private Long subgroupId;
    }
}

