package com.mangaworkflow.api.persistence.repository;

import com.mangaworkflow.api.persistence.entity.SkillCategoryEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillCategoryRepository extends JpaRepository<SkillCategoryEntity, Long> {
  Optional<SkillCategoryEntity> findByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
