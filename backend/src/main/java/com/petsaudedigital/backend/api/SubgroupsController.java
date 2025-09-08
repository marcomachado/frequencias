package com.petsaudedigital.backend.api;

import com.petsaudedigital.backend.api.dto.SubgroupDtos;
import com.petsaudedigital.backend.service.SubgroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gts/{gtId}/subgroups")
@RequiredArgsConstructor
public class SubgroupsController {
    private final SubgroupService subgroupService;

    @PostMapping
    @PreAuthorize("hasRole('coordenador_gt')")
    public ResponseEntity<SubgroupDtos.View> create(@PathVariable Long gtId, @RequestBody @Valid SubgroupDtos.Create req) {
        return ResponseEntity.ok(subgroupService.create(gtId, req));
    }

    @GetMapping
    public ResponseEntity<List<SubgroupDtos.View>> list(@PathVariable Long gtId) {
        return ResponseEntity.ok(subgroupService.list(gtId));
    }
}

