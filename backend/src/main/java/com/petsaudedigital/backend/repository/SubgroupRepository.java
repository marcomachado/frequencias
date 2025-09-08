package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.Gt;
import com.petsaudedigital.backend.domain.Subgroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubgroupRepository extends JpaRepository<Subgroup, Long> {
    List<Subgroup> findByGt(Gt gt);
}

