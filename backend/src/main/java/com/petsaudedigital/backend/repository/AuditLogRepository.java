package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}

