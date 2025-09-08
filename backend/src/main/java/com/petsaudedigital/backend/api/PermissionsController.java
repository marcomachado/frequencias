package com.petsaudedigital.backend.api;

import com.petsaudedigital.backend.api.dto.PermissionDtos;
import com.petsaudedigital.backend.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionsController {
    private final PermissionService permissionService;

    @PostMapping("/grant")
    @PreAuthorize("@scope.has(authentication, 'exportar_dados', #req.scopeType, #req.scopeId)")
    public ResponseEntity<Void> grant(Authentication authentication, @RequestBody @Valid PermissionDtos.Grant req) {
        permissionService.grant(authentication, req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/revoke")
    @PreAuthorize("@scope.has(authentication, 'exportar_dados', #req.scopeType, #req.scopeId)")
    public ResponseEntity<Void> revoke(@RequestBody @Valid PermissionDtos.Revoke req) {
        permissionService.revoke(req);
        return ResponseEntity.noContent().build();
    }
}

