package com.mangaworkflow.api.web.admin;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangaworkflow.api.web.admin.AdminSkillController;
import com.mangaworkflow.api.domain.skill.SkillCategoryCreateRequest;
import com.mangaworkflow.api.domain.skill.SkillCategoryUpdateRequest;
import com.mangaworkflow.api.application.skill.InMemorySkillCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class AdminSkillControllerTest {
  private final ObjectMapper mapper = new ObjectMapper();

  private MockMvc mockMvc;
  private InMemorySkillCategoryService service;

  @BeforeEach
  public void setup() {
    service = new InMemorySkillCategoryService();
    mockMvc = MockMvcBuilders.standaloneSetup(new AdminSkillController(service)).build();
  }

  @Test
  public void list_returnsSeededSkills() throws Exception {
    mockMvc
        .perform(get("/api/admin/skills"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name", is("Inking")));
  }

  @Test
  public void createUpdateToggleDelete_flowWorks() throws Exception {
    SkillCategoryCreateRequest create = new SkillCategoryCreateRequest();
    create.setName("Tone");
    create.setDescription("Tone sheets");

    String response =
        mockMvc
            .perform(
                post("/api/admin/skills")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(create)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String id = mapper.readTree(response).get("id").asText();

    SkillCategoryUpdateRequest update = new SkillCategoryUpdateRequest();
    update.setName("Tone Art");
    update.setDescription("Updated");

    mockMvc
        .perform(
            put("/api/admin/skills/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(update)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is("Tone Art")));

    mockMvc
        .perform(
            put("/api/admin/skills/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active", is(false)));

    mockMvc.perform(delete("/api/admin/skills/" + id)).andExpect(status().isNoContent());
  }
}
