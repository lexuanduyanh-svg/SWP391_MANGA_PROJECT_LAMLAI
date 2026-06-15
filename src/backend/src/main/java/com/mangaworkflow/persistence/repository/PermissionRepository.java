package com.mangaworkflow.persistence.repository;

import com.mangaworkflow.persistence.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
}
