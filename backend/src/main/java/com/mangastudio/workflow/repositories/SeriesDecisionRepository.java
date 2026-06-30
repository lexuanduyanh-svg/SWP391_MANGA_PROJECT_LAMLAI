package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.SeriesDecisionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesDecisionRepository extends JpaRepository<SeriesDecisionEntity, Long> {
  List<SeriesDecisionEntity> findBySeries_IdOrderByDecidedAtDesc(Long seriesId);
  List<SeriesDecisionEntity> findAllByOrderByDecidedAtDesc();
}
