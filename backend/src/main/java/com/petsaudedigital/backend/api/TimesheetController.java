package com.petsaudedigital.backend.api;

import com.petsaudedigital.backend.api.dto.TimesheetDtos;
import com.petsaudedigital.backend.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TimesheetController {
    private final TimesheetService timesheetService;

    @GetMapping("/api/v1/users/{userId}/timesheet")
    public ResponseEntity<TimesheetDtos.Summary> getTimesheet(@PathVariable Long userId,
                                                              @RequestParam String from,
                                                              @RequestParam String to) {
        return ResponseEntity.ok(timesheetService.compute(userId, from, to));
    }

    @PostMapping("/api/v1/users/{userId}/timesheet/pdf")
    public ResponseEntity<byte[]> getTimesheetPdf(@PathVariable Long userId,
                                                  @RequestParam String from,
                                                  @RequestParam String to) {
        byte[] pdf = timesheetService.generatePdf(userId, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=timesheet-" + userId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

