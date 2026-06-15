package com.mangaworkflow.persistence.repository;

import com.mangaworkflow.persistence.entity.ChapterEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<ChapterEntity, Long> {
  List<ChapterEntity> findBySeries_IdOrderByUpdatedAtDesc(Long seriesId);
}
