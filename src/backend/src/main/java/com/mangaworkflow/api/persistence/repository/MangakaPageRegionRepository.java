package com.mangaworkflow.api.persistence.repository;

import com.mangaworkflow.api.persistence.entity.MangakaPageRegionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MangakaPageRegionRepository extends JpaRepository<MangakaPageRegionEntity, Long> {
  List<MangakaPageRegionEntity> findByPageId(Long pageId);
}
