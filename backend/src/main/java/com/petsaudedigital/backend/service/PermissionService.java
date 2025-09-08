package com.petsaudedigital.backend.service;

import com.petsaudedigital.backend.api.dto.PermissionDtos;
import com.petsaudedigital.backend.domain.User;
import com.petsaudedigital.backend.domain.UserPermission;
import com.petsaudedigital.backend.repository.UserPermissionRepository;
import com.petsaudedigital.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final UserPermissionRepository userPermissionRepository;
    private final UserRepository userRepository;

    public void grant(Authentication authentication, PermissionDtos.Grant req) {
        User target = userRepository.findById(req.userId()).orElseThrow();

        UserPermission up = new UserPermission();
        up.setId(new UserPermission.Id(target.getId(), req.permission(), req.scopeType(), req.scopeId()));
        up.setUser(target);
        up.setValidFrom(req.validFrom());
        up.setValidUntil(req.validUntil());

        String email = authentication.getName();
        User granter = userRepository.findByEmail(email).orElse(null);
        if (granter != null) up.setGrantedBy(granter);
        up.setGrantedAt(Instant.now().toString());

        userPermissionRepository.save(up);
    }

    public void revoke(PermissionDtos.Revoke req) {
        UserPermission.Id id = new UserPermission.Id(req.userId(), req.permission(), req.scopeType(), req.scopeId());
        userPermissionRepository.deleteById(id);
    }
}

