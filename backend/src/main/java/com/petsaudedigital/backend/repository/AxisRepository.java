package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.Axis;
import com.petsaudedigital.backend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AxisRepository extends JpaRepository<Axis, Long> {
    List<Axis> findByProject(Project project);
}

