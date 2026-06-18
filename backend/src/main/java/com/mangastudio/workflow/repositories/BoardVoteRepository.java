package com.mangastudio.workflow.repositories;

import com.mangastudio.workflow.entities.BoardVoteEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardVoteRepository extends JpaRepository<BoardVoteEntity, Long> {
  boolean existsBySeries_IdAndBoardMember_EmailIgnoreCase(Long seriesId, String email);

  List<BoardVoteEntity> findBySeries_Id(Long seriesId);

  Optional<BoardVoteEntity> findBySeries_IdAndBoardMember_EmailIgnoreCase(Long seriesId, String email);
}
