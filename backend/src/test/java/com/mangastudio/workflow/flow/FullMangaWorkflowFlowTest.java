package com.mangastudio.workflow.flow;

import com.mangastudio.workflow.services.InMemoryMangakaProductionService;
import com.mangastudio.workflow.services.InMemoryMangaProposalService;
import com.mangastudio.workflow.dtos.MangakaChapterCreateRequest;
import com.mangastudio.workflow.dtos.MangakaPageCreateRequest;
import com.mangastudio.workflow.dtos.MangakaProductionTaskCreateRequest;
import com.mangastudio.workflow.dtos.MangakaChapterDto;
import com.mangastudio.workflow.dtos.MangakaPageDto;
import com.mangastudio.workflow.dtos.MangakaProductionTaskDto;
import com.mangastudio.workflow.dtos.AssistantTaskDto;
import com.mangastudio.workflow.dtos.MangaProposalCreateRequest;
import com.mangastudio.workflow.dtos.MangaProposalDto;
import com.mangastudio.workflow.dtos.MangaProposalStatus;
import com.mangastudio.workflow.dtos.MangaProposalSubmitRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FullMangaWorkflowFlowTest {
  @Test
  public void fullProposalToProductionWorkflow_completesHappyPath() {
    InMemoryMangaProposalService proposalService = new InMemoryMangaProposalService();
    InMemoryMangakaProductionService productionService =
        new InMemoryMangakaProductionService(proposalService);

    MangaProposalCreateRequest create = new MangaProposalCreateRequest();
    create.setAuthorEmail("mangaka@manga.local");
    create.setTitle("Workflow Proposal");
    create.setGenre("Action");
    create.setTargetAudience("Teen");
    create.setSynopsis("A full workflow test proposal.");
    create.setManuscriptTitle("Workflow Manuscript");
    create.setManuscriptSummary("Initial manuscript metadata");

    MangaProposalDto draft = proposalService.create(create);
    Assertions.assertEquals(MangaProposalStatus.Draft, draft.getStatus());

    MangaProposalDto withManuscript =
        proposalService.attachManuscriptMetadata(
            draft.getId(), "mangaka@manga.local", "workflow-manuscript-v1.pdf",
            "Uploaded manuscript metadata");
    Assertions.assertEquals("workflow-manuscript-v1.pdf", withManuscript.getManuscriptFileName());
    Assertions.assertEquals(Integer.valueOf(1), withManuscript.getManuscriptVersion());

    MangaProposalSubmitRequest submit = new MangaProposalSubmitRequest();
    submit.setAuthorEmail("mangaka@manga.local");
    MangaProposalDto submitted = proposalService.submit(draft.getId(), submit.getAuthorEmail());
    Assertions.assertEquals(MangaProposalStatus.SubmittedToEditor, submitted.getStatus());

    MangaProposalDto forwarded =
        proposalService.forwardToBoard(draft.getId(), "editor@example.com", "Please review");
    Assertions.assertEquals(MangaProposalStatus.UnderBoardReview, forwarded.getStatus());

    proposalService.approveByBoard(draft.getId(), "board@manga.local", "OK");
    proposalService.approveByBoard(draft.getId(), "board2@manga.local", "OK");
    MangaProposalDto approved =
        proposalService.approveByBoard(draft.getId(), "board3@manga.local", "OK");
    Assertions.assertEquals(MangaProposalStatus.Approved, approved.getStatus());

    // After approval, the proposal should have a seriesId
    String seriesId = approved.getSeriesId();
    Assertions.assertNotNull(seriesId, "Approved proposal should have a seriesId");

    // --- Flow 2: Production workflow using seriesId ---

    MangakaChapterCreateRequest chapterRequest = new MangakaChapterCreateRequest();
    chapterRequest.setTitle("Chapter 1");
    chapterRequest.setChapterNumber(1);
    MangakaChapterDto chapter =
        productionService.createChapter(seriesId, "mangaka@manga.local", chapterRequest);
    Assertions.assertEquals("Draft", chapter.getStatus().name());

    MangakaPageCreateRequest pageRequest = new MangakaPageCreateRequest();
    pageRequest.setPageNumber(1);
    pageRequest.setFileName("page-1.psd");
    MangakaPageDto page =
        productionService.addPage(seriesId, chapter.getId(), "mangaka@manga.local", pageRequest);
    Assertions.assertEquals("Uploaded", page.getStatus().name());

    MangakaProductionTaskCreateRequest taskRequest = new MangakaProductionTaskCreateRequest();
    taskRequest.setAssistantEmail("assistant@manga.local");
    taskRequest.setInstructions("Letter the main dialogue.");
    MangakaProductionTaskDto task =
        productionService.assignTask(
            seriesId, chapter.getId(), page.getId(), null, "mangaka@manga.local", taskRequest);
    Assertions.assertEquals("Pending", task.getStatus().name());

    AssistantTaskDto started =
        productionService.startAssistantTask(task.getId(), "assistant@manga.local");
    Assertions.assertEquals("InProgress", started.getStatus().name());

    AssistantTaskDto submittedTask =
        productionService.submitAssistantTask(
            task.getId(), "assistant@manga.local", "lettered-page-1.psd", "Ready for review");
    Assertions.assertEquals("Submitted", submittedTask.getStatus().name());

    MangakaProductionTaskDto approvedTask =
        productionService.approveTask(
            seriesId, chapter.getId(), page.getId(), task.getId(), "mangaka@manga.local");
    Assertions.assertEquals("Approved", approvedTask.getStatus().name());
  }

  @Test
  public void productionCreation_rejectsNonExistentSeries() {
    InMemoryMangaProposalService proposalService = new InMemoryMangaProposalService();
    InMemoryMangakaProductionService productionService =
        new InMemoryMangakaProductionService(proposalService);

    MangakaChapterCreateRequest chapterRequest = new MangakaChapterCreateRequest();
    chapterRequest.setTitle("Chapter 1");
    chapterRequest.setChapterNumber(1);

    IllegalArgumentException ex =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> productionService.createChapter("999", "mangaka@manga.local", chapterRequest));
    Assertions.assertTrue(ex.getMessage().contains("Series not found"));
  }
}
