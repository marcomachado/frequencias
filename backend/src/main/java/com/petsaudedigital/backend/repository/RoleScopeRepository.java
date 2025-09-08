package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.RoleScope;
import com.petsaudedigital.backend.domain.RoleScope.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleScopeRepository extends JpaRepository<RoleScope, Id> {}

