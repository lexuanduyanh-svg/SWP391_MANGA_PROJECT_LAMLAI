package com.mangaworkflow.persistence.repository;

import com.mangaworkflow.persistence.entity.SeriesEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<SeriesEntity, Long> {
  List<SeriesEntity> findByMangaka_EmailIgnoreCaseOrderByUpdatedAtDesc(String email);

  List<SeriesEntity> findByTantouEditor_EmailIgnoreCaseOrderByUpdatedAtDesc(String email);

  List<SeriesEntity> findByIdInOrderByUpdatedAtDesc(List<Long> ids);

  List<SeriesEntity> findByStatusInOrderByUpdatedAtDesc(List<String> statuses);

  Optional<SeriesEntity> findByIdAndMangaka_EmailIgnoreCase(Long id, String email);
}
