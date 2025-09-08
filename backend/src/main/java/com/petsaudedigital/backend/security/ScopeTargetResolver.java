package com.petsaudedigital.backend.security;

import com.petsaudedigital.backend.domain.Activity;
import com.petsaudedigital.backend.domain.Attendance;
import com.petsaudedigital.backend.repository.ActivityRepository;
import com.petsaudedigital.backend.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("scopeResolver")
@RequiredArgsConstructor
public class ScopeTargetResolver {
    private final ActivityRepository activityRepository;
    private final AttendanceRepository attendanceRepository;

    public Long fromActivity(Long activityId) {
        if (activityId == null) return null;
        return activityRepository.findById(activityId)
                .map(Activity::getGt)
                .map(gt -> gt.getId())
                .orElse(null);
    }

    public Long fromAttendance(Long attendanceId) {
        if (attendanceId == null) return null;
        return attendanceRepository.findById(attendanceId)
                .map(Attendance::getActivity)
                .map(Activity::getGt)
                .map(gt -> gt.getId())
                .orElse(null);
    }
}

