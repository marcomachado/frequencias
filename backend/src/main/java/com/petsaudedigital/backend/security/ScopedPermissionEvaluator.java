package com.petsaudedigital.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class ScopedPermissionEvaluator implements PermissionEvaluator {

    private final ScopeService scopeService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // Não utilizado neste projeto; utilizamos a assinatura com targetId e targetType.
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (!(permission instanceof String)) return false;
        String perm = (String) permission;
        Long id = (targetId instanceof Number) ? ((Number) targetId).longValue() : null;
        return scopeService.has(authentication, perm, targetType, id);
    }
}

