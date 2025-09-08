package com.petsaudedigital.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsaudedigital.backend.domain.AuditLog;
import com.petsaudedigital.backend.domain.User;
import com.petsaudedigital.backend.repository.AuditLogRepository;
import com.petsaudedigital.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper om;

    public void log(Authentication auth, String action, String entity, Long entityId, Object diff) {
        AuditLog l = new AuditLog();
        if (auth != null) {
            userRepository.findByEmail(auth.getName()).ifPresent(l::setActor);
        }
        l.setAction(action);
        l.setEntity(entity);
        l.setEntityId(entityId);
        if (diff != null) {
            try { l.setPayloadDiff(om.writeValueAsString(diff)); } catch (JsonProcessingException ignored) {}
        }
        l.setCreatedAt(Instant.now().toString());
        auditLogRepository.save(l);
    }
}

