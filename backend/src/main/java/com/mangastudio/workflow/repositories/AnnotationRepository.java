package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.AnnotationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnotationRepository extends JpaRepository<AnnotationEntity, Long> {
}
