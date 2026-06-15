package com.mangaworkflow.persistence.repository;

import com.mangaworkflow.persistence.entity.ReaderMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReaderMetricRepository extends JpaRepository<ReaderMetricEntity, Long> {
}
