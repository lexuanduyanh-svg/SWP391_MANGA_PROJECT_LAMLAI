package com.mangaworkflow.api.persistence.repository;

import com.mangaworkflow.api.persistence.entity.MangakaChapterEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MangakaChapterRepository extends JpaRepository<MangakaChapterEntity, Long> {
  List<MangakaChapterEntity> findByProposalId(Long proposalId);
}
