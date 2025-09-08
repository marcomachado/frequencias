package com.petsaudedigital.backend.api;

import com.petsaudedigital.backend.api.dto.ProjectDtos;
import com.petsaudedigital.backend.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectsController {
    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('coordenador_geral')")
    public ResponseEntity<ProjectDtos.View> create(@RequestBody @Valid ProjectDtos.Create req) {
        return ResponseEntity.ok(projectService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<ProjectDtos.View>> list() {
        return ResponseEntity.ok(projectService.list());
    }
}

