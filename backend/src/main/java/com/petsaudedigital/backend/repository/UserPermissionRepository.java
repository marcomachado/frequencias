package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.UserPermission;
import com.petsaudedigital.backend.domain.UserPermission.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPermissionRepository extends JpaRepository<UserPermission, Id> {}

