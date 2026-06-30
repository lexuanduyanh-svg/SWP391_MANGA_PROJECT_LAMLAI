package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.ProposalEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProposalRepository extends JpaRepository<ProposalEntity, Long> {
  List<ProposalEntity> findByMangaka_EmailIgnoreCaseOrderByUpdatedAtDesc(String email);

  List<ProposalEntity> findByTantouEditor_EmailIgnoreCaseOrderByUpdatedAtDesc(String email);

  List<ProposalEntity> findByStatusInOrderByUpdatedAtDesc(List<String> statuses);

  Optional<ProposalEntity> findByIdAndMangaka_EmailIgnoreCase(Long id, String email);
}
