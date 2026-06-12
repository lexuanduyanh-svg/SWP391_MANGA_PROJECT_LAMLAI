package com.mangaworkflow.api.web.board;

import com.mangaworkflow.api.domain.proposal.BoardProposalDecisionRequest;
import com.mangaworkflow.api.web.editor.TantouEditorProposalController;
import com.mangaworkflow.api.domain.proposal.EditorProposalReviewRequest;
import com.mangaworkflow.api.domain.proposal.MangaProposalDto;
import com.mangaworkflow.api.application.proposal.InMemoryMangaProposalService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EditorialBoardProposalControllerTest {
  private final InMemoryMangaProposalService service = new InMemoryMangaProposalService();
  private final TantouEditorProposalController editorController =
      new TantouEditorProposalController(service);
  private final EditorialBoardProposalController controller =
      new EditorialBoardProposalController(service);

  @Test
  public void approve_happy_path() {
    EditorProposalReviewRequest req = new EditorProposalReviewRequest();
    req.setEditorEmail("editor@example.com");
    editorController.forward("3", req);

    BoardProposalDecisionRequest first = new BoardProposalDecisionRequest();
    first.setMemberEmail("board@manga.local");
    first.setNote("Approved");
    MangaProposalDto firstDto = (MangaProposalDto) controller.approve("3", first).getBody();
    Assertions.assertEquals("UnderBoardReview", firstDto.getStatus().name());
    Assertions.assertEquals(Integer.valueOf(1), firstDto.getBoardApproveVotes());
    Assertions.assertEquals(Integer.valueOf(0), firstDto.getBoardRejectVotes());
    Assertions.assertEquals(Integer.valueOf(2), firstDto.getBoardPendingVotes());

    BoardProposalDecisionRequest second = new BoardProposalDecisionRequest();
    second.setMemberEmail("board2@manga.local");
    second.setNote("Approved");
    controller.approve("3", second);

    BoardProposalDecisionRequest third = new BoardProposalDecisionRequest();
    third.setMemberEmail("board3@manga.local");
    third.setNote("Rejected");
    MangaProposalDto dto = (MangaProposalDto) controller.reject("3", third).getBody();
    Assertions.assertEquals("Approved", dto.getStatus().name());
    Assertions.assertEquals(Integer.valueOf(2), dto.getBoardApproveVotes());
    Assertions.assertEquals(Integer.valueOf(1), dto.getBoardRejectVotes());
    Assertions.assertEquals(Integer.valueOf(0), dto.getBoardPendingVotes());
  }
}
