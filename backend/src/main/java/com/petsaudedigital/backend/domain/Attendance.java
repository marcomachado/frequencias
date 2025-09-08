package com.petsaudedigital.backend.domain;

import com.petsaudedigital.backend.domain.enums.AttendanceMode;
import com.petsaudedigital.backend.domain.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attendance",
        uniqueConstraints = @UniqueConstraint(name = "uq_attendance_activity_user", columnNames = {"activity_id", "user_id"}))
@Getter
@Setter
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceMode modo;

    @Column(name = "created_at", nullable = false)
    private String createdAt;
}

