package com.petsaudedigital.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_target_role")
@Getter
@Setter
public class ActivityTargetRole {
    @EmbeddedId
    private Id id;

    @MapsId("activityId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements java.io.Serializable {
        @Column(name = "activity_id")
        private Long activityId;
        @Column(name = "role")
        private String role;
    }
}

