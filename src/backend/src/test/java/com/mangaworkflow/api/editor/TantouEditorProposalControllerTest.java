package com.mangaworkflow.api.editor;

import com.mangaworkflow.domain.proposal.EditorProposalReviewRequest;
import com.mangaworkflow.domain.proposal.MangaProposalDto;
import com.mangaworkflow.application.proposal.InMemoryMangaProposalService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TantouEditorProposalControllerTest {
  private final InMemoryMangaProposalService service = new InMemoryMangaProposalService();
  private final TantouEditorProposalController controller =
      new TantouEditorProposalController(service);

  @Test
  public void forward_board_happy_path() {
    EditorProposalReviewRequest req = new EditorProposalReviewRequest();
    req.setEditorEmail("editor@example.com");
    req.setNote("Forwarding");
    MangaProposalDto dto = (MangaProposalDto) controller.forward("3", req).getBody();
    Assertions.assertEquals("UnderBoardReview", dto.getStatus().name());
    Assertions.assertEquals("editor@example.com", dto.getEditorEmail());
  }
}
