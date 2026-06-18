package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
}
