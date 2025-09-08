package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {}

