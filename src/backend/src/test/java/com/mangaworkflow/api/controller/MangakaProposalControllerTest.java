package com.mangaworkflow.api.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangaworkflow.api.model.MangaProposalCreateRequest;
import com.mangaworkflow.api.model.MangaProposalSubmitRequest;
import com.mangaworkflow.api.service.InMemoryMangaProposalService;
import com.mangaworkflow.api.service.InMemoryMangakaProductionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
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
}
