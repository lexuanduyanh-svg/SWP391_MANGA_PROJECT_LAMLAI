package com.mangastudio.workflow.controllers;

import com.mangastudio.workflow.dtos.EditorProposalReviewRequest;
import com.mangastudio.workflow.dtos.MangaProposalDto;
import com.mangastudio.workflow.services.InMemoryMangaProposalService;
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
