package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.BoardVoteEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardVoteRepository extends JpaRepository<BoardVoteEntity, Long> {
  boolean existsByProposal_IdAndBoardMember_EmailIgnoreCase(Long proposalId, String email);

  List<BoardVoteEntity> findByProposal_Id(Long proposalId);

  Optional<BoardVoteEntity> findByProposal_IdAndBoardMember_EmailIgnoreCase(Long proposalId, String email);
}
