package com.petsaudedigital.backend.domain;

import com.petsaudedigital.backend.domain.enums.TargetType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "activity_target")
@Getter
@Setter
public class ActivityTarget {
    @Id
    @Column(name = "activity_id")
    private Long activityId;

    @OneToOne
    @JoinColumn(name = "activity_id", insertable = false, updatable = false)
    private Activity activity;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;
}

