package com.mangaworkflow.persistence.repository;

import com.mangaworkflow.persistence.entity.AnnotationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnotationRepository extends JpaRepository<AnnotationEntity, Long> {
}
