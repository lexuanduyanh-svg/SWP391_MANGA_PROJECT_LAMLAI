package com.mangaworkflow.persistence.repository;

import com.mangaworkflow.persistence.entity.SkillEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<SkillEntity, Long> {
  Optional<SkillEntity> findBySkillNameIgnoreCase(String skillName);

  Optional<SkillEntity> findBySkillName(String skillName);

  boolean existsBySkillNameIgnoreCaseAndIdNot(String skillName, Long id);
}
