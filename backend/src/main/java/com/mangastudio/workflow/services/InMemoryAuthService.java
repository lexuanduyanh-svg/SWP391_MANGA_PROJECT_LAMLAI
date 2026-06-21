package com.mangastudio.workflow.services;

import com.mangastudio.workflow.dtos.AccountDto;
import com.mangastudio.workflow.dtos.UserRole;
import com.mangastudio.workflow.dtos.AuthenticatedUser;
import com.mangastudio.workflow.dtos.LoginRequest;
import com.mangastudio.workflow.dtos.LoginResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class InMemoryAuthService implements AuthService {
  private final InMemoryAccountService accountService;

  public InMemoryAuthService(InMemoryAccountService accountService) {
    this.accountService = accountService;
  }

  @Override
  public Optional<LoginResponse> login(LoginRequest request) {
    if (request == null) {
      return Optional.empty();
    }

    Optional<AccountDto> account =
        accountService.authenticate(request.getEmail(), request.getPassword());
    if (!account.isPresent()) {
      return Optional.empty();
    }

    AccountDto authenticatedAccount = account.get();
    AuthenticatedUser authenticatedUser =
        new AuthenticatedUser(
            authenticatedAccount.getId(),
            authenticatedAccount.getFullName(),
            authenticatedAccount.getEmail(),
            authenticatedAccount.getRole().name());

    return Optional.of(
        new LoginResponse(
            createDevelopmentToken(authenticatedAccount),
            authenticatedUser,
            getDashboardPath(authenticatedAccount.getRole())));
  }

  private static String getDashboardPath(UserRole role) {
    switch (role) {
      case Admin:
        return "/admin/dashboard";
      case Mangaka:
        return "/mangaka/workspace";
      case Assistant:
        return "/assistant/tasks";
      case TantouEditor:
        return "/editor/dashboard";
      case EditorialBoardMember:
        return "/board/dashboard";
      default:
        return "/";
    }
  }

  private static String createDevelopmentToken(AccountDto account) {
    String rawToken =
        account.getId()
            + ":"
            + account.getEmail()
            + ":"
            + account.getRole()
            + ":"
            + Instant.now().toString();
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 algorithm is not available", exception);
    }
  }
}
