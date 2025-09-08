package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.Activity;
import com.petsaudedigital.backend.domain.Attendance;
import com.petsaudedigital.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByActivityAndUser(Activity activity, User user);
    List<Attendance> findByActivity(Activity activity);
}

