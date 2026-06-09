package com.mangaworkflow.api.persistence.repository;

import com.mangaworkflow.api.persistence.entity.AccountEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
  Optional<AccountEntity> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
