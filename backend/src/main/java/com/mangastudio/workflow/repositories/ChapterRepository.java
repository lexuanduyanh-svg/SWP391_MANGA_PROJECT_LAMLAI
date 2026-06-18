package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.ChapterEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<ChapterEntity, Long> {
  List<ChapterEntity> findBySeries_IdOrderByUpdatedAtDesc(Long seriesId);
}
