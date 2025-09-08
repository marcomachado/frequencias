package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.UserGt;
import com.petsaudedigital.backend.domain.UserGt.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGtRepository extends JpaRepository<UserGt, Id> {}

