package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.ActivityTargetRole;
import com.petsaudedigital.backend.domain.ActivityTargetRole.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityTargetRoleRepository extends JpaRepository<ActivityTargetRole, Id> {}

