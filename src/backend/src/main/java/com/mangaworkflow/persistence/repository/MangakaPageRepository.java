package com.mangaworkflow.persistence.repository;

import com.mangaworkflow.persistence.entity.MangakaPageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MangakaPageRepository extends JpaRepository<MangakaPageEntity, Long> {
  List<MangakaPageEntity> findByChapterId(Long chapterId);
}
