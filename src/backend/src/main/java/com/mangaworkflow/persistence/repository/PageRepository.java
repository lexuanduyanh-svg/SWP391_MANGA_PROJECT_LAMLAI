package com.mangaworkflow.persistence.repository;

import com.mangaworkflow.persistence.entity.PageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageRepository extends JpaRepository<PageEntity, Long> {
  List<PageEntity> findByChapter_IdOrderByUpdatedAtDesc(Long chapterId);
}
