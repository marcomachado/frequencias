package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.UserSubgroup;
import com.petsaudedigital.backend.domain.UserSubgroup.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSubgroupRepository extends JpaRepository<UserSubgroup, Id> {}

