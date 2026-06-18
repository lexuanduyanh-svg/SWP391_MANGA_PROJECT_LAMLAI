package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.SubmissionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {
  List<SubmissionEntity> findByTask_IdOrderBySubmittedAtDesc(Long taskId);
}
