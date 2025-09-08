package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.SystemSetting;
import com.petsaudedigital.backend.domain.SystemSetting.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, Id> {
    @Query("select s from SystemSetting s where s.id.key = :key and (s.id.scopeType = :scopeType or :scopeType is null) and (s.id.scopeId = :scopeId or :scopeId is null)")
    List<SystemSetting> findByKeyAndScope(@Param("key") String key, @Param("scopeType") String scopeType, @Param("scopeId") Long scopeId);
}

