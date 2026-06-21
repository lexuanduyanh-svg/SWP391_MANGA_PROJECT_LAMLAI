package com.mangastudio.workflow.services;

import com.mangastudio.workflow.dtos.MangaProposalCreateRequest;
import com.mangastudio.workflow.dtos.MangaProposalDto;
import com.mangastudio.workflow.dtos.MangaProposalUpdateRequest;
import com.mangastudio.workflow.services.InMemoryMangaProposalService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InMemoryMangaProposalServiceTest {
  private final InMemoryMangaProposalService service = new InMemoryMangaProposalService();

  @Test
  public void list_returnsSeededDemoData() {
    Assertions.assertTrue(service.listByAuthorEmail("mangaka@manga.local").size() >= 2);
  }

  @Test
  public void createUpdateSubmit_flowWorks() {
    MangaProposalCreateRequest c = new MangaProposalCreateRequest();
    c.setAuthorEmail("mangaka@manga.local");
    c.setTitle("New Proposal");
    c.setGenre("Adventure");
    c.setTargetAudience("Teen");
    c.setSynopsis("Synopsis");
    c.setManuscriptTitle("Manuscript");
    c.setManuscriptSummary("Summary");
    c.setManuscriptFileName("draft-v1.pdf");
    MangaProposalDto created = service.create(c);
    Assertions.assertEquals("Draft", created.getStatus().name());
    Assertions.assertEquals(Integer.valueOf(1), created.getManuscriptVersion());
    MangaProposalUpdateRequest u = new MangaProposalUpdateRequest();
    u.setTitle("New Proposal v2");
    u.setGenre("Adventure");
    u.setTargetAudience("Teen");
    u.setSynopsis("Synopsis 2");
    u.setManuscriptTitle("Manuscript 2");
    u.setManuscriptSummary("Summary 2");
    u.setManuscriptFileName("draft-v2.pdf");
    Assertions.assertEquals(
        "New Proposal v2", service.update(created.getId(), "mangaka@manga.local", u).getTitle());
    Assertions.assertEquals(
        "SubmittedToEditor",
        service.submit(created.getId(), "mangaka@manga.local").getStatus().name());
  }

  @Test
  public void submit_rejectsForbiddenStatusTransition() {
    IllegalArgumentException ex =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> service.submit("3", "mangaka@manga.local"));
    Assertions.assertTrue(ex.getMessage().contains("current status"));
  }

  @Test
  public void boardVote_majorityFinalizesAfterThirdVote() {
    MangaProposalDto forwarded = service.forwardToBoard("3", "editor@example.com", "Go board");
    Assertions.assertEquals("UnderBoardReview", forwarded.getStatus().name());

    service.approveByBoard("3", "board@manga.local", "OK");
    service.approveByBoard("3", "board2@manga.local", "OK");
    MangaProposalDto finalized = service.rejectByBoard("3", "board3@manga.local", "No");

    Assertions.assertEquals("Approved", finalized.getStatus().name());
    Assertions.assertEquals(Integer.valueOf(2), finalized.getBoardApproveVotes());
    Assertions.assertEquals(Integer.valueOf(1), finalized.getBoardRejectVotes());
    Assertions.assertEquals(Integer.valueOf(0), finalized.getBoardPendingVotes());
  }
}
