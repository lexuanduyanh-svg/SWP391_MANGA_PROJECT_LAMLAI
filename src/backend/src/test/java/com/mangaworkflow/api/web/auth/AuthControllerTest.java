package com.mangaworkflow.api.web.auth;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangaworkflow.api.web.auth.AuthController;
import com.mangaworkflow.api.domain.auth.AuthenticatedUser;
import com.mangaworkflow.api.domain.auth.LoginRequest;
import com.mangaworkflow.api.domain.auth.LoginResponse;
import com.mangaworkflow.api.application.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class AuthControllerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private AuthService authService;
  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    authService = Mockito.mock(AuthService.class);
    mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
  }

  @Test
  public void login_withValidCredentials_returns200AndLoginPayload() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setEmail("admin@manga.local");
    request.setPassword("Admin@123");

    LoginResponse response =
        new LoginResponse(
            "token-123",
            new AuthenticatedUser("1", "System Admin", "admin@manga.local", "Admin"),
            "/admin/dashboard");
    Mockito.when(authService.login(Mockito.any(LoginRequest.class)))
        .thenReturn(java.util.Optional.of(response));

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken", is("token-123")))
        .andExpect(jsonPath("$.user.email", is("admin@manga.local")))
        .andExpect(jsonPath("$.user.role", is("Admin")))
        .andExpect(jsonPath("$.dashboardPath", is("/admin/dashboard")));
  }

  @Test
  public void login_withInvalidCredentials_returns401AndMessage() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setEmail("admin@manga.local");
    request.setPassword("wrong-password");

    Mockito.when(authService.login(Mockito.any(LoginRequest.class)))
        .thenReturn(java.util.Optional.<LoginResponse>empty());

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message", notNullValue()));
  }
}
