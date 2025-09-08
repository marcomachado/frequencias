package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.UserRole;
import com.petsaudedigital.backend.domain.UserRole.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Id> {}

