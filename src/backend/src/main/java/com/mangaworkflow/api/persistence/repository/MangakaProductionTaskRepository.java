package com.mangaworkflow.api.persistence.repository;

import com.mangaworkflow.api.persistence.entity.MangakaProductionTaskEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MangakaProductionTaskRepository
    extends JpaRepository<MangakaProductionTaskEntity, Long> {
  List<MangakaProductionTaskEntity> findByRegionId(Long regionId);

  List<MangakaProductionTaskEntity> findByAssistantEmailIgnoreCase(String assistantEmail);
}
