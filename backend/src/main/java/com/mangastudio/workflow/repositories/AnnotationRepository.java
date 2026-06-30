package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.AnnotationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnotationRepository extends JpaRepository<AnnotationEntity, Long> {
  List<AnnotationEntity> findByPage_IdOrderByCreatedAtAsc(Long pageId);
}
