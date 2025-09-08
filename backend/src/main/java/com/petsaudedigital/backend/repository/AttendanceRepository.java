package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.Activity;
import com.petsaudedigital.backend.domain.Attendance;
import com.petsaudedigital.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByActivityAndUser(Activity activity, User user);
    List<Attendance> findByActivity(Activity activity);

    @Query("select a from Attendance a where a.user.id = :userId and a.activity.data between :from and :to")
    List<Attendance> findByUserAndDateRange(@Param("userId") Long userId, @Param("from") String from, @Param("to") String to);

    @Query("select a from Attendance a where a.activity.gt.id = :gtId and a.activity.data between :from and :to")
    List<Attendance> findByGtAndDateRange(@Param("gtId") Long gtId, @Param("from") String from, @Param("to") String to);

    @Query("select a from Attendance a where a.activity.gt.id = :gtId and a.status = 'pendente'")
    List<Attendance> findPendingByGt(@Param("gtId") Long gtId);
}
