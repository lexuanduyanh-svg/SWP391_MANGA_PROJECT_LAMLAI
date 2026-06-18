package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.PageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageRepository extends JpaRepository<PageEntity, Long> {
  List<PageEntity> findByChapter_IdOrderByUpdatedAtDesc(Long chapterId);
}
