package com.mangaworkflow.persistence.repository;

import com.mangaworkflow.persistence.entity.UserSkillEntity;
import com.mangaworkflow.persistence.entity.UserSkillId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSkillRepository extends JpaRepository<UserSkillEntity, UserSkillId> {
  List<UserSkillEntity> findByUser_Id(Long userId);

  void deleteByUser_Id(Long userId);
}
