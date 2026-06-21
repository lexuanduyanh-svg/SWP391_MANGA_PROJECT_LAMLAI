package com.mangastudio.workflow.controllers;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangastudio.workflow.dtos.SkillCategoryCreateRequest;
import com.mangastudio.workflow.dtos.SkillCategoryUpdateRequest;
import com.mangastudio.workflow.services.InMemoryAccountService;
import com.mangastudio.workflow.services.InMemorySkillCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class AdminSkillsControllerTest {
  private final ObjectMapper mapper = new ObjectMapper();
  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(
        new AdminController(new InMemoryAccountService(), new InMemorySkillCategoryService())).build();
  }

  @Test
  public void list_returnsSeededSkills() throws Exception {
    mockMvc.perform(get("/admin/skills"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name", is("Inking")));
  }

  @Test
  public void createUpdateToggleDelete_flowWorks() throws Exception {
    SkillCategoryCreateRequest create = new SkillCategoryCreateRequest();
    create.setName("Tone");
    create.setDescription("Tone sheets");

    String response = mockMvc.perform(post("/admin/skills")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(create)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    String id = mapper.readTree(response).get("id").asText();

    SkillCategoryUpdateRequest update = new SkillCategoryUpdateRequest();
    update.setName("Tone Art");
    update.setDescription("Updated");

    mockMvc.perform(put("/admin/skills/" + id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(update)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is("Tone Art")));

    mockMvc.perform(put("/admin/skills/" + id + "/status")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"active\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active", is(false)));

    mockMvc.perform(delete("/admin/skills/" + id)).andExpect(status().isNoContent());
  }
}
