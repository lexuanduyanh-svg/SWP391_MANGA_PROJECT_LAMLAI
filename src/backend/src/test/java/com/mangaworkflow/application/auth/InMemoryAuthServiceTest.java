package com.mangaworkflow.application.auth;

import com.mangaworkflow.domain.account.AccountCreateRequest;
import com.mangaworkflow.domain.account.UserRole;
import com.mangaworkflow.domain.auth.AuthenticatedUser;
import com.mangaworkflow.domain.auth.LoginRequest;
import com.mangaworkflow.domain.auth.LoginResponse;
import com.mangaworkflow.application.auth.InMemoryAuthService;
import com.mangaworkflow.application.account.InMemoryAccountService;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InMemoryAuthServiceTest {
  private final InMemoryAccountService accountService = new InMemoryAccountService();
  private final InMemoryAuthService authService = new InMemoryAuthService(accountService);

  @Test
  public void login_withValidAdminCredentials_returnsAdminDashboardResponse() {
    LoginRequest request = new LoginRequest();
    request.setEmail("admin@manga.local");
    request.setPassword("Admin@123");

    Optional<LoginResponse> response = authService.login(request);

    Assertions.assertTrue(response.isPresent());
    Assertions.assertNotNull(response.get().getAccessToken());
    Assertions.assertFalse(response.get().getAccessToken().isEmpty());
    Assertions.assertEquals("/admin/dashboard", response.get().getDashboardPath());

    AuthenticatedUser user = response.get().getUser();
    Assertions.assertNotNull(user);
    Assertions.assertEquals("Admin", user.getRole());
    Assertions.assertEquals("admin@manga.local", user.getEmail());
  }

  @Test
  public void login_withWrongPassword_returnsEmpty() {
    LoginRequest request = new LoginRequest();
    request.setEmail("admin@manga.local");
    request.setPassword("wrong-password");

    Optional<LoginResponse> response = authService.login(request);

    Assertions.assertFalse(response.isPresent());
  }

  @Test
  public void login_withNewlyCreatedActiveAccount_returnsRoleDashboard() {
    AccountCreateRequest createRequest = new AccountCreateRequest();
    createRequest.setFullName("New Assistant");
    createRequest.setEmail("newassistant@manga.local");
    createRequest.setPassword("Assistant@456");
    createRequest.setRole(UserRole.Assistant);
    accountService.createAccount(createRequest);

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("newassistant@manga.local");
    loginRequest.setPassword("Assistant@456");

    Optional<LoginResponse> response = authService.login(loginRequest);

    Assertions.assertTrue(response.isPresent());
    Assertions.assertEquals("Assistant", response.get().getUser().getRole());
    Assertions.assertEquals("/assistant/tasks", response.get().getDashboardPath());
  }
}
