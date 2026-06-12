package com.mangaworkflow.api.web.admin;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangaworkflow.api.web.admin.AdminAccountController;
import com.mangaworkflow.api.domain.account.AccountCreateRequest;
import com.mangaworkflow.api.domain.account.AccountStatus;
import com.mangaworkflow.api.domain.account.AccountUpdateRequest;
import com.mangaworkflow.api.domain.account.UserRole;
import com.mangaworkflow.api.application.account.InMemoryAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class AdminAccountControllerTest {
  private final ObjectMapper mapper = new ObjectMapper();

  private MockMvc mockMvc;
  private InMemoryAccountService service;

  @BeforeEach
  public void setup() {
    service = new InMemoryAccountService();
    mockMvc = MockMvcBuilders.standaloneSetup(new AdminAccountController(service)).build();
  }

  @Test
  public void list_returnsSeededAccounts() throws Exception {
    mockMvc
        .perform(get("/api/admin/accounts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email", is("admin@manga.local")));
  }

  @Test
  public void createUpdateDelete_flowWorks() throws Exception {
    AccountCreateRequest create = new AccountCreateRequest();
    create.setFullName("Temp User");
    create.setEmail("temp@manga.local");
    create.setPassword("Temp@123");
    create.setRole(UserRole.Assistant);

    String response =
        mockMvc
            .perform(
                post("/api/admin/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(create)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String id = mapper.readTree(response).get("id").asText();

    AccountUpdateRequest update = new AccountUpdateRequest();
    update.setFullName("Temp User 2");
    update.setEmail("temp2@manga.local");
    update.setRole(UserRole.Admin);
    update.setStatus(AccountStatus.Active);

    mockMvc
        .perform(
            put("/api/admin/accounts/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(update)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName", is("Temp User 2")));

    mockMvc
        .perform(
            put("/api/admin/accounts/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"Inactive\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("Inactive")));

    mockMvc.perform(delete("/api/admin/accounts/" + id)).andExpect(status().isNoContent());
  }
}
