package com.petsaudedigital.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_participant")
@Getter
@Setter
public class ActivityParticipant {
    @EmbeddedId
    private Id id;

    @MapsId("activityId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "activity_id")
    private Activity activity;

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
        @Column(name = "activity_id")
        private Long activityId;
        @Column(name = "user_id")
        private Long userId;
    }
}

