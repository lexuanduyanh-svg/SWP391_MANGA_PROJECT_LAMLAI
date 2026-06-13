package com.mangaworkflow.flow;

import com.mangaworkflow.application.production.InMemoryMangakaProductionService;
import com.mangaworkflow.application.proposal.InMemoryMangaProposalService;
import com.mangaworkflow.domain.production.MangakaChapterCreateRequest;
import com.mangaworkflow.domain.production.MangakaPageCreateRequest;
import com.mangaworkflow.domain.production.MangakaPageRegionCreateRequest;
import com.mangaworkflow.domain.production.MangakaProductionTaskCreateRequest;
import com.mangaworkflow.domain.production.MangakaChapterDto;
import com.mangaworkflow.domain.production.MangakaPageDto;
import com.mangaworkflow.domain.production.MangakaPageRegionDto;
import com.mangaworkflow.domain.production.MangakaProductionTaskDto;
import com.mangaworkflow.domain.task.AssistantTaskDto;
import com.mangaworkflow.domain.proposal.MangaProposalCreateRequest;
import com.mangaworkflow.domain.proposal.MangaProposalDto;
import com.mangaworkflow.domain.proposal.MangaProposalStatus;
import com.mangaworkflow.domain.proposal.MangaProposalSubmitRequest;
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
            draft.getId(), "mangaka@manga.local", "workflow-manuscript-v1.pdf", "Uploaded manuscript metadata");
    Assertions.assertEquals("workflow-manuscript-v1.pdf", withManuscript.getManuscriptFileName());
    Assertions.assertEquals(Integer.valueOf(1), withManuscript.getManuscriptVersion());

    MangaProposalSubmitRequest submit = new MangaProposalSubmitRequest();
    submit.setAuthorEmail("mangaka@manga.local");
    MangaProposalDto submitted = proposalService.submit(draft.getId(), submit.getAuthorEmail());
    Assertions.assertEquals(MangaProposalStatus.SubmittedToEditor, submitted.getStatus());

    MangaProposalDto forwarded = proposalService.forwardToBoard(draft.getId(), "editor@example.com", "Please review");
    Assertions.assertEquals(MangaProposalStatus.UnderBoardReview, forwarded.getStatus());

    proposalService.approveByBoard(draft.getId(), "board@manga.local", "OK");
    proposalService.approveByBoard(draft.getId(), "board2@manga.local", "OK");
    MangaProposalDto approved = proposalService.approveByBoard(draft.getId(), "board3@manga.local", "OK");
    Assertions.assertEquals(MangaProposalStatus.Approved, approved.getStatus());

    MangakaChapterCreateRequest chapterRequest = new MangakaChapterCreateRequest();
    chapterRequest.setTitle("Chapter 1");
    chapterRequest.setChapterNumber(1);
    MangakaChapterDto chapter = productionService.createChapter(draft.getId(), "mangaka@manga.local", chapterRequest);
    Assertions.assertEquals("Draft", chapter.getStatus().name());

    MangakaPageCreateRequest pageRequest = new MangakaPageCreateRequest();
    pageRequest.setPageNumber(1);
    pageRequest.setFileName("page-1.psd");
    MangakaPageDto page = productionService.addPage(draft.getId(), chapter.getId(), "mangaka@manga.local", pageRequest);
    Assertions.assertEquals("Uploaded", page.getStatus().name());

    MangakaPageRegionCreateRequest regionRequest = new MangakaPageRegionCreateRequest();
    regionRequest.setRegionType("SpeechBubble");
    regionRequest.setX(5);
    regionRequest.setY(5);
    regionRequest.setWidthPct(20);
    regionRequest.setHeightPct(15);
    regionRequest.setNote("Main dialogue bubble");
    MangakaPageRegionDto region =
        productionService.addRegion(draft.getId(), chapter.getId(), page.getId(), "mangaka@manga.local", regionRequest);
    Assertions.assertEquals("SpeechBubble", region.getRegionType());

    MangakaProductionTaskCreateRequest taskRequest = new MangakaProductionTaskCreateRequest();
    taskRequest.setAssistantEmail("assistant@manga.local");
    taskRequest.setTaskType("Lettering");
    taskRequest.setInstructions("Letter the main dialogue.");
    taskRequest.setReferenceFileName("dialogue-reference.txt");
    MangakaProductionTaskDto task =
        productionService.assignTask(
            draft.getId(), chapter.getId(), page.getId(), region.getId(), "mangaka@manga.local", taskRequest);
    Assertions.assertEquals("Pending", task.getStatus().name());

    AssistantTaskDto started = productionService.startAssistantTask(task.getId(), "assistant@manga.local");
    Assertions.assertEquals("InProgress", started.getStatus().name());

    AssistantTaskDto submittedTask =
        productionService.submitAssistantTask(
            task.getId(), "assistant@manga.local", "lettered-page-1.psd", "Ready for review");
    Assertions.assertEquals("Submitted", submittedTask.getStatus().name());

    MangakaProductionTaskDto approvedTask =
        productionService.approveTask(
            draft.getId(), chapter.getId(), page.getId(), region.getId(), task.getId(), "mangaka@manga.local");
    Assertions.assertEquals("Approved", approvedTask.getStatus().name());
  }
}
