package com.mangastudio.workflow.controllers;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangastudio.workflow.controllers.AssistantTaskController;
import com.mangastudio.workflow.dtos.*;
import com.mangastudio.workflow.services.InMemoryMangaProposalService;
import com.mangastudio.workflow.controllers.MangakaProductionController;
import com.mangastudio.workflow.services.InMemoryMangakaProductionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class AssistantTaskControllerTest {
  private final ObjectMapper mapper = new ObjectMapper();
  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    InMemoryMangaProposalService proposalService = new InMemoryMangaProposalService();
    InMemoryMangakaProductionService productionService =
        new InMemoryMangakaProductionService(proposalService);
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new AssistantTaskController(productionService),
                new MangakaProductionController(productionService))
            .build();
  }

  @Test
  public void listStartSubmit_flowWorks() throws Exception {
    mockMvc
        .perform(get("/api/assistant/tasks").param("assistantEmail", "assistant@manga.local"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status", is("Pending")));
    AssistantTaskActionRequest start = new AssistantTaskActionRequest();
    start.setAssistantEmail("assistant@manga.local");
    mockMvc
        .perform(
            put("/api/assistant/tasks/603/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(start)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("InProgress")));
    AssistantTaskSubmitRequest submit = new AssistantTaskSubmitRequest();
    submit.setAssistantEmail("assistant@manga.local");
    submit.setSubmittedFileName("translated-page-1.png");
    submit.setSubmissionNote("Done");
    mockMvc
        .perform(
            put("/api/assistant/tasks/603/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(submit)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("Submitted")))
        .andExpect(jsonPath("$.submittedFileName", is("translated-page-1.png")));
    mockMvc
        .perform(
            put("/api/mangaka/proposals/4/chapters/600/pages/601/regions/602/tasks/603/approve")
                .param("authorEmail", "mangaka@manga.local"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("Approved")));
    mockMvc
        .perform(
            put("/api/mangaka/proposals/4/chapters/600/pages/601/regions/602/tasks/603/redo")
                .param("authorEmail", "mangaka@manga.local"))
        .andExpect(status().isConflict());
  }

  @Test
  public void submitBeforeStart_isRejected() throws Exception {
    AssistantTaskSubmitRequest submit = new AssistantTaskSubmitRequest();
    submit.setAssistantEmail("assistant@manga.local");
    submit.setSubmittedFileName("translated-page-1.png");
    submit.setSubmissionNote("Done");

    mockMvc
        .perform(
            put("/api/assistant/tasks/603/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(submit)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message", is("Task cannot be submitted in current status")));
  }
}
