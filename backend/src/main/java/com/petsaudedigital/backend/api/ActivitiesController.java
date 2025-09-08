package com.petsaudedigital.backend.api;

import com.petsaudedigital.backend.api.dto.ActivityDtos;
import com.petsaudedigital.backend.domain.Activity;
import com.petsaudedigital.backend.domain.Attendance;
import com.petsaudedigital.backend.repository.ActivityRepository;
import com.petsaudedigital.backend.repository.AttendanceRepository;
import com.petsaudedigital.backend.service.ActivityService;
import com.petsaudedigital.backend.service.AttendanceService;
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
    public ResponseEntity<List<Attendance>> listAttendances(@PathVariable Long id) {
        Activity a = activityRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(attendanceRepository.findByActivity(a));
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
