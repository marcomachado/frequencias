package com.petsaudedigital.backend.api.dto;

import com.petsaudedigital.backend.domain.Attendance;
import com.petsaudedigital.backend.domain.User;
import com.petsaudedigital.backend.domain.enums.AttendanceMode;
import com.petsaudedigital.backend.domain.enums.AttendanceStatus;

public class AttendanceDtos {
    public record View(
            Long id,
            SimpleUser user,
            AttendanceStatus status,
            AttendanceMode modo,
            String createdAt
    ) {
        public static View from(Attendance a) {
            return new View(
                    a.getId(),
                    SimpleUser.from(a.getUser()),
                    a.getStatus(),
                    a.getModo(),
                    a.getCreatedAt()
            );
        }
    }

    public record SimpleUser(Long id, String nome, String email) {
        public static SimpleUser from(User u) {
            return new SimpleUser(u.getId(), u.getNome(), u.getEmail());
        }
    }
}

