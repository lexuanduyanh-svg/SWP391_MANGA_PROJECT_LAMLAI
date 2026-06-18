package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.UserSkillEntity;
import com.mangastudio.workflow.entities.UserSkillId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSkillRepository extends JpaRepository<UserSkillEntity, UserSkillId> {
  List<UserSkillEntity> findByUser_Id(Long userId);

  void deleteByUser_Id(Long userId);
}
