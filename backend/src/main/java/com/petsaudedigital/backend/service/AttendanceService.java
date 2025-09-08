package com.petsaudedigital.backend.service;

import com.petsaudedigital.backend.domain.Attendance;
import com.petsaudedigital.backend.domain.Activity;
import com.petsaudedigital.backend.domain.AttendanceValidation;
import com.petsaudedigital.backend.domain.User;
import com.petsaudedigital.backend.domain.enums.ValidationDecision;
import com.petsaudedigital.backend.domain.enums.AttendanceStatus;
import com.petsaudedigital.backend.repository.AttendanceRepository;
import com.petsaudedigital.backend.repository.AttendanceValidationRepository;
import com.petsaudedigital.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final AttendanceValidationRepository attendanceValidationRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public void validate(Long attendanceId, Authentication auth) {
        Attendance att = attendanceRepository.findById(attendanceId).orElseThrow();
        User validator = userRepository.findByEmail(auth.getName()).orElseThrow();

        att.setStatus(AttendanceStatus.validada);
        attendanceRepository.save(att);

        AttendanceValidation av = new AttendanceValidation();
        av.setAttendance(att);
        av.setValidator(validator);
        av.setDecision(ValidationDecision.validar);
        av.setValidatedAt(Instant.now().toString());
        attendanceValidationRepository.save(av);
        auditService.log(auth, "validate_attendance", "attendance", att.getId(), null);
    }

    @Transactional
    public void reject(Long attendanceId, Authentication auth) {
        Attendance att = attendanceRepository.findById(attendanceId).orElseThrow();
        User validator = userRepository.findByEmail(auth.getName()).orElseThrow();

        att.setStatus(AttendanceStatus.rejeitada);
        attendanceRepository.save(att);

        AttendanceValidation av = new AttendanceValidation();
        av.setAttendance(att);
        av.setValidator(validator);
        av.setDecision(ValidationDecision.rejeitar);
        av.setValidatedAt(Instant.now().toString());
        attendanceValidationRepository.save(av);
        auditService.log(auth, "reject_attendance", "attendance", att.getId(), null);
    }

    public UserRepository getUserRepository() { return userRepository; }

    @Transactional
    public Attendance createManual(Activity a, User u, Authentication auth) {
        Attendance att = new Attendance();
        att.setActivity(a);
        att.setUser(u);
        boolean isPrivileged = auth.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().contains("coordenador") || ga.getAuthority().contains("tutor"));
        att.setStatus(isPrivileged ? AttendanceStatus.validada : AttendanceStatus.pendente);
        att.setModo(com.petsaudedigital.backend.domain.enums.AttendanceMode.manual);
        att.setCreatedAt(java.time.Instant.now().toString());
        return attendanceRepository.save(att);
    }

    @Transactional
    public boolean deleteIfPending(Long id) {
        return attendanceRepository.findById(id).map(att -> {
            if (att.getStatus() == AttendanceStatus.pendente) {
                attendanceRepository.delete(att);
                return true;
            }
            return false;
        }).orElse(false);
    }
}
