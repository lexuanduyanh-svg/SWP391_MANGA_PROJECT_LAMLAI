package com.mangaworkflow.api.persistence.repository;

import com.mangaworkflow.api.persistence.entity.MangakaPageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MangakaPageRepository extends JpaRepository<MangakaPageEntity, Long> {
  List<MangakaPageEntity> findByChapterId(Long chapterId);
}
