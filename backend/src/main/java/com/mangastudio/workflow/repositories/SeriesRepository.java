package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.SeriesEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<SeriesEntity, Long> {
  List<SeriesEntity> findByMangaka_EmailIgnoreCaseOrderByUpdatedAtDesc(String email);

  Optional<SeriesEntity> findByIdAndMangaka_EmailIgnoreCase(Long id, String email);
}
