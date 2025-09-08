package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.Axis;
import com.petsaudedigital.backend.domain.Gt;
import com.petsaudedigital.backend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GtRepository extends JpaRepository<Gt, Long> {
    List<Gt> findByAxis(Axis axis);
    List<Gt> findByProject(Project project);
}

