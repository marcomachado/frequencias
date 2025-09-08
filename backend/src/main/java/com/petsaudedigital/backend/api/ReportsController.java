package com.petsaudedigital.backend.api;

import com.petsaudedigital.backend.api.dto.ReportDtos;
import com.petsaudedigital.backend.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReportsController {
    private final ReportsService reportsService;

    @GetMapping("/api/v1/reports/hours-by-user")
    public ResponseEntity<ReportDtos.HoursByUser> hoursByUser(@RequestParam Long gtId,
                                                              @RequestParam String month) {
        return ResponseEntity.ok(reportsService.hoursByUser(gtId, month));
    }

    @GetMapping("/api/v1/reports/pending-attendances")
    public ResponseEntity<ReportDtos.PendingAttendances> pending(@RequestParam Long gtId) {
        return ResponseEntity.ok(reportsService.pendingAttendances(gtId));
    }
}

