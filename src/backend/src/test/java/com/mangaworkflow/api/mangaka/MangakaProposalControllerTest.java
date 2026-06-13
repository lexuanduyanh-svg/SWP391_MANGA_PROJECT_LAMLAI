package com.mangaworkflow.api.mangaka;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangaworkflow.api.mangaka.MangakaProductionController;
import com.mangaworkflow.api.mangaka.MangakaProposalController;
import com.mangaworkflow.domain.proposal.MangaProposalCreateRequest;
import com.mangaworkflow.domain.proposal.MangaProposalSubmitRequest;
import com.mangaworkflow.application.proposal.InMemoryMangaProposalService;
import com.mangaworkflow.application.production.InMemoryMangakaProductionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class MangakaProposalControllerTest {
  private final ObjectMapper mapper = new ObjectMapper();
  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    InMemoryMangaProposalService proposalService = new InMemoryMangaProposalService();
    InMemoryMangakaProductionService productionService =
        new InMemoryMangakaProductionService(proposalService);
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new MangakaProposalController(proposalService),
                new MangakaProductionController(productionService))
            .build();
  }

  @Test
  public void createThenSubmit_requiresManuscriptFile() throws Exception {
    MangaProposalCreateRequest c = new MangaProposalCreateRequest();
    c.setAuthorEmail("mangaka@manga.local");
    c.setTitle("Controller Proposal");
    c.setGenre("Drama");
    c.setTargetAudience("Adult");
    c.setSynopsis("Synopsis");
    c.setManuscriptTitle("Manuscript");
    c.setManuscriptSummary("Summary");
    String response =
        mockMvc
            .perform(
                post("/api/mangaka/proposals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(c)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String id = mapper.readTree(response).get("id").asText();
    MangaProposalSubmitRequest s = new MangaProposalSubmitRequest();
    s.setAuthorEmail("mangaka@manga.local");
    mockMvc
        .perform(
            put("/api/mangaka/proposals/" + id + "/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(s)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message", is("Manuscript file is required before submission")));
  }

  @Test
  public void production_happyPath_onApprovedSeed() throws Exception {
    mockMvc
        .perform(
            get("/api/mangaka/proposals/4/chapters").param("authorEmail", "mangaka@manga.local"))
        .andExpect(status().isOk());
  }

  @Test
  public void previewUpload_rejectsInvalidFileType() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "script.exe", "application/octet-stream", "bad".getBytes());

    mockMvc
        .perform(multipart("/api/mangaka/proposals/preview-upload").file(file))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.valid", is(false)));
  }

  @Test
  public void previewUpload_returnsSummaryForValidTextFile() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "manuscript.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Chapter one begins with a hero entering Tokyo.".getBytes());

    mockMvc
        .perform(multipart("/api/mangaka/proposals/preview-upload").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid", is(true)))
        .andExpect(jsonPath("$.fileName", is("manuscript.txt")));
  }

  @Test
  public void upload_attachesManuscriptMetadataToDraftProposal() throws Exception {
    MangaProposalCreateRequest c = new MangaProposalCreateRequest();
    c.setAuthorEmail("mangaka@manga.local");
    c.setTitle("Upload Proposal");
    c.setGenre("Drama");
    c.setTargetAudience("Teen");
    c.setSynopsis("Synopsis");
    c.setManuscriptTitle("Manuscript");
    c.setManuscriptSummary("Initial summary");
    String response =
        mockMvc
            .perform(
                post("/api/mangaka/proposals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(c)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String id = mapper.readTree(response).get("id").asText();

    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "chapter.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Chapter upload content for backend attachment.".getBytes());

    mockMvc
        .perform(
            multipart("/api/mangaka/proposals/upload")
                .file(file)
                .param("proposalId", id)
                .param("authorEmail", "mangaka@manga.local"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.fileName", containsString("chapter.txt")))
        .andExpect(jsonPath("$.summary", is("Chapter upload content for backend attachment.")))
        .andExpect(jsonPath("$.proposal.id", is(id)))
        .andExpect(jsonPath("$.proposal.manuscriptFileName", containsString("chapter.txt")))
        .andExpect(jsonPath("$.proposal.manuscriptVersion", is(1)));
  }
}
