package com.petsaudedigital.backend.api;

import com.petsaudedigital.backend.domain.AuditLog;
import com.petsaudedigital.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuditController {
    private final AuditLogRepository repo;

    @GetMapping("/api/v1/audit")
    @PreAuthorize("@scope.has(authentication, 'exportar_dados', 'GLOBAL', null)")
    public ResponseEntity<List<AuditLog>> list() {
        return ResponseEntity.ok(repo.findAll());
    }
}

