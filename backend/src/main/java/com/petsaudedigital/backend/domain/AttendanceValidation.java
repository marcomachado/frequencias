package com.petsaudedigital.backend.domain;

import com.petsaudedigital.backend.domain.enums.ValidationDecision;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attendance_validation")
@Getter
@Setter
public class AttendanceValidation {
    @Id
    @Column(name = "attendance_id")
    private Long attendanceId;

    @OneToOne
    @JoinColumn(name = "attendance_id", insertable = false, updatable = false)
    private Attendance attendance;

    @ManyToOne(optional = false)
    @JoinColumn(name = "validator_user_id")
    private User validator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidationDecision decision;

    @Column(name = "validated_at", nullable = false)
    private String validatedAt;

    private String note;
}

