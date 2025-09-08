package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.Activity;
import com.petsaudedigital.backend.domain.Gt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByGt(Gt gt);
}

