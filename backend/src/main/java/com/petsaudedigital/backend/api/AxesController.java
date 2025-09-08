package com.petsaudedigital.backend.api;

import com.petsaudedigital.backend.api.dto.AxisDtos;
import com.petsaudedigital.backend.service.AxisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/axes")
@RequiredArgsConstructor
public class AxesController {
    private final AxisService axisService;

    @PostMapping
    @PreAuthorize("hasRole('coordenador_geral')")
    public ResponseEntity<AxisDtos.View> create(@PathVariable Long projectId, @RequestBody @Valid AxisDtos.Create req) {
        return ResponseEntity.ok(axisService.create(projectId, req));
    }

    @GetMapping
    public ResponseEntity<List<AxisDtos.View>> list(@PathVariable Long projectId) {
        return ResponseEntity.ok(axisService.list(projectId));
    }
}

