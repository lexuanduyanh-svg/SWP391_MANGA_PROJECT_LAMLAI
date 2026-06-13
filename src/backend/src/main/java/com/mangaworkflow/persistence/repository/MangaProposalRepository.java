package com.mangaworkflow.persistence.repository;

import com.mangaworkflow.persistence.entity.MangaProposalEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MangaProposalRepository extends JpaRepository<MangaProposalEntity, Long> {
  List<MangaProposalEntity> findByAuthorEmailIgnoreCase(String authorEmail);

  List<MangaProposalEntity> findByStatusIn(List<String> statuses);
}
