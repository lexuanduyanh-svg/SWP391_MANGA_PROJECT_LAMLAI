package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.TaskEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
  List<TaskEntity> findByPage_IdOrderByUpdatedAtDesc(Long pageId);

  List<TaskEntity> findByAssistant_EmailIgnoreCaseOrderByUpdatedAtDesc(String email);
}
