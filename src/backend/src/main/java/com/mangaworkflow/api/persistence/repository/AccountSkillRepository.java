package com.mangaworkflow.api.persistence.repository;

import com.mangaworkflow.api.persistence.entity.AccountSkillEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountSkillRepository extends JpaRepository<AccountSkillEntity, Long> {
  List<AccountSkillEntity> findByAccountId(Long accountId);

  void deleteByAccountId(Long accountId);
}
