package com.petsaudedigital.backend.api;

import com.petsaudedigital.backend.api.dto.GtDtos;
import com.petsaudedigital.backend.service.GtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/axes/{axisId}/gts")
@RequiredArgsConstructor
public class GtsController {
    private final GtService gtService;

    @PostMapping
    @PreAuthorize("hasRole('coordenador_geral') or hasRole('coordenador_eixo')")
    public ResponseEntity<GtDtos.View> create(@PathVariable Long axisId, @RequestBody @Valid GtDtos.Create req) {
        return ResponseEntity.ok(gtService.create(axisId, req));
    }

    @GetMapping
    public ResponseEntity<List<GtDtos.View>> list(@PathVariable Long axisId) {
        return ResponseEntity.ok(gtService.listByAxis(axisId));
    }
}

