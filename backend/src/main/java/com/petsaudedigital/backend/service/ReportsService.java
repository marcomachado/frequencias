package com.petsaudedigital.backend.service;

import com.petsaudedigital.backend.api.dto.ReportDtos;
import com.petsaudedigital.backend.domain.Attendance;
import com.petsaudedigital.backend.domain.enums.AttendanceStatus;
import com.petsaudedigital.backend.repository.AttendanceRepository;
import com.petsaudedigital.backend.repository.UserGtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportsService {
    private final AttendanceRepository attendanceRepository;
    private final UserGtRepository userGtRepository;

    public ReportDtos.HoursByUser hoursByUser(Long gtId, String month) {
        // month format: YYYY-MM — compute [YYYY-MM-01, YYYY-MM-31] conservatively
        String from = month + "-01";
        String to = month + "-31";
        List<Attendance> list = attendanceRepository.findByGtAndDateRange(gtId, from, to);
        Map<Long, Double> hours = new HashMap<>();
        Map<Long, String> emails = new HashMap<>();
        for (Attendance a : list) {
            if (a.getStatus() != AttendanceStatus.validada) continue;
            long min = minutes(a);
            hours.merge(a.getUser().getId(), min / 60.0, Double::sum);
            emails.putIfAbsent(a.getUser().getId(), a.getUser().getEmail());
        }
        List<ReportDtos.HoursByUserItem> items = hours.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new ReportDtos.HoursByUserItem(e.getKey(), emails.get(e.getKey()), Math.round(e.getValue() * 100.0) / 100.0))
                .toList();
        return new ReportDtos.HoursByUser(items);
    }

    public ReportDtos.PendingAttendances pendingAttendances(Long gtId) {
        List<Attendance> pend = attendanceRepository.findPendingByGt(gtId);
        List<ReportDtos.PendingItem> items = pend.stream()
                .map(a -> new ReportDtos.PendingItem(
                        a.getId(), a.getActivity().getId(), a.getActivity().getTitulo(), a.getUser().getId(), a.getUser().getEmail(),
                        a.getActivity().getData(), a.getActivity().getInicio(), a.getActivity().getFim()))
                .toList();
        return new ReportDtos.PendingAttendances(items);
    }

    private long minutes(Attendance a) {
        LocalTime i = LocalTime.parse(a.getActivity().getInicio());
        LocalTime f = LocalTime.parse(a.getActivity().getFim());
        return Duration.between(i, f).toMinutes();
    }
}
