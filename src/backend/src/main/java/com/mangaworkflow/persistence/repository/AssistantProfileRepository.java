package com.mangaworkflow.persistence.repository;

import com.mangaworkflow.persistence.entity.AssistantProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantProfileRepository extends JpaRepository<AssistantProfileEntity, Long> {
}
