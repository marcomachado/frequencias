package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.ActivityParticipant;
import com.petsaudedigital.backend.domain.ActivityParticipant.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Id> {}

