package com.petsaudedigital.backend.api;

import com.petsaudedigital.backend.api.dto.ActivityDtos;
import com.petsaudedigital.backend.domain.Activity;
import com.petsaudedigital.backend.domain.Attendance;
import com.petsaudedigital.backend.repository.ActivityRepository;
import com.petsaudedigital.backend.repository.AttendanceRepository;
import com.petsaudedigital.backend.service.ActivityService;
import com.petsaudedigital.backend.service.AttendanceService;
import com.petsaudedigital.backend.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivitiesController {
    private final ActivityService activityService;
    private final ActivityRepository activityRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceService attendanceService;

    @PostMapping("/api/v1/gts/{gtId}/activities")
    public ResponseEntity<Activity> create(@PathVariable Long gtId, @RequestBody @Valid ActivityDtos.Create req, Authentication auth) {
        Activity a = activityService.create(gtId, req, auth);
        return ResponseEntity.ok(a);
    }

    @GetMapping("/api/v1/activities/{id}")
    public ResponseEntity<Activity> get(@PathVariable Long id) {
        return ResponseEntity.of(activityRepository.findById(id));
    }

    @GetMapping("/api/v1/activities/{id}/attendances")
    public ResponseEntity<List<com.petsaudedigital.backend.api.dto.AttendanceDtos.View>> listAttendances(@PathVariable Long id) {
        Activity a = activityRepository.findById(id).orElseThrow();
        List<com.petsaudedigital.backend.api.dto.AttendanceDtos.View> out = attendanceRepository.findByActivity(a)
                .stream().map(com.petsaudedigital.backend.api.dto.AttendanceDtos.View::from).toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/api/v1/gts/{gtId}/activities")
    public ResponseEntity<List<Activity>> listByGt(@PathVariable Long gtId,
                                                   @RequestParam(required = false) String from,
                                                   @RequestParam(required = false) String to,
                                                   @RequestParam(required = false) String tipo) {
        return activityRepository.findById(gtId).map(a -> ResponseEntity.ok(List.of(a))).orElseGet(() -> {
            // Fallback by querying repository (simplified by scanning all in repo)
            List<Activity> all = activityRepository.findAll().stream()
                    .filter(ac -> ac.getGt() != null && ac.getGt().getId().equals(gtId))
                    .filter(ac -> from == null || ac.getData().compareTo(from) >= 0)
                    .filter(ac -> to == null || ac.getData().compareTo(to) <= 0)
                    .filter(ac -> tipo == null || ac.getTipo().name().equalsIgnoreCase(tipo))
                    .toList();
            return ResponseEntity.ok(all);
        });
    }

    @PostMapping("/api/v1/activities/{activityId}/attendances")
    @PreAuthorize("@scope.has(authentication, 'lancar_presenca_validada', 'GT', @scopeResolver.fromActivity(#activityId))")
    public ResponseEntity<Attendance> addManualAttendance(@PathVariable Long activityId,
                                                          @RequestParam Long userId,
                                                          Authentication auth) {
        Activity a = activityRepository.findById(activityId).orElseThrow();
        User u = attendanceService.getUserRepository().findById(userId).orElseThrow();
        var existing = attendanceRepository.findByActivityAndUser(a, u);
        if (existing.isPresent()) {
            return ResponseEntity.status(409).build();
        }
        Attendance att = attendanceService.createManual(a, u, auth);
        return ResponseEntity.ok(att);
    }

    @DeleteMapping("/api/v1/attendances/{id}")
    @PreAuthorize("@scope.has(authentication, 'exportar_dados', 'GT', @scopeResolver.fromAttendance(#id))")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        return attendanceService.deleteIfPending(id) ? ResponseEntity.noContent().build() : ResponseEntity.status(400).build();
    }

    @PatchMapping("/api/v1/activities/{id}")
    @PreAuthorize("@scope.has(authentication, 'exportar_dados', 'GT', @scopeResolver.fromActivity(#id))")
    public ResponseEntity<Activity> patch(@PathVariable Long id, @RequestBody Activity patch) {
        return ResponseEntity.of(activityService.patch(id, patch));
    }

    @DeleteMapping("/api/v1/activities/{id}")
    @PreAuthorize("@scope.has(authentication, 'exportar_dados', 'GT', @scopeResolver.fromActivity(#id))")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        activityRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/attendances/{attendanceId}/validate")
    @PreAuthorize("@scope.has(authentication, 'exportar_dados', 'GT', @scopeResolver.fromAttendance(#attendanceId))")
    public ResponseEntity<Void> validate(@PathVariable Long attendanceId, Authentication auth) {
        attendanceService.validate(attendanceId, auth);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/attendances/{attendanceId}/reject")
    @PreAuthorize("@scope.has(authentication, 'exportar_dados', 'GT', @scopeResolver.fromAttendance(#attendanceId))")
    public ResponseEntity<Void> reject(@PathVariable Long attendanceId, Authentication auth) {
        attendanceService.reject(attendanceId, auth);
        return ResponseEntity.noContent().build();
    }
}
