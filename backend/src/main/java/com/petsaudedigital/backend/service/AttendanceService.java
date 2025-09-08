package com.petsaudedigital.backend.service;

import com.petsaudedigital.backend.domain.Attendance;
import com.petsaudedigital.backend.domain.AttendanceValidation;
import com.petsaudedigital.backend.domain.User;
import com.petsaudedigital.backend.domain.enums.ValidationDecision;
import com.petsaudedigital.backend.domain.enums.AttendanceStatus;
import com.petsaudedigital.backend.repository.AttendanceRepository;
import com.petsaudedigital.backend.repository.AttendanceValidationRepository;
import com.petsaudedigital.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final AttendanceValidationRepository attendanceValidationRepository;
    private final UserRepository userRepository;

    public void validate(Long attendanceId, Authentication auth) {
        Attendance att = attendanceRepository.findById(attendanceId).orElseThrow();
        User validator = userRepository.findByEmail(auth.getName()).orElseThrow();

        att.setStatus(AttendanceStatus.validada);
        attendanceRepository.save(att);

        AttendanceValidation av = new AttendanceValidation();
        av.setAttendance(att);
        av.setAttendanceId(att.getId());
        av.setValidator(validator);
        av.setDecision(ValidationDecision.validar);
        av.setValidatedAt(Instant.now().toString());
        attendanceValidationRepository.save(av);
    }

    public void reject(Long attendanceId, Authentication auth) {
        Attendance att = attendanceRepository.findById(attendanceId).orElseThrow();
        User validator = userRepository.findByEmail(auth.getName()).orElseThrow();

        att.setStatus(AttendanceStatus.rejeitada);
        attendanceRepository.save(att);

        AttendanceValidation av = new AttendanceValidation();
        av.setAttendance(att);
        av.setAttendanceId(att.getId());
        av.setValidator(validator);
        av.setDecision(ValidationDecision.rejeitar);
        av.setValidatedAt(Instant.now().toString());
        attendanceValidationRepository.save(av);
    }
}

