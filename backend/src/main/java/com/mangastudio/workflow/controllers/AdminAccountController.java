package com.mangastudio.workflow.controllers;

import com.mangastudio.workflow.dtos.AccountCreateRequest;
import com.mangastudio.workflow.dtos.AccountDto;
import com.mangastudio.workflow.dtos.AccountStatus;
import com.mangastudio.workflow.dtos.AccountUpdateRequest;
import com.mangastudio.workflow.services.InMemoryAccountService;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {
  private final InMemoryAccountService accountService;

  public AdminAccountController(InMemoryAccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping
  public List<AccountDto> list() {
    return accountService.listAccounts();
  }

  @PostMapping
  public ResponseEntity<?> create(@Valid @RequestBody AccountCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    } catch (IllegalArgumentException exception) {
      return conflict(exception);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(
      @PathVariable String id, @Valid @RequestBody AccountUpdateRequest request) {
    try {
      return ResponseEntity.ok(accountService.updateAccount(id, request));
    } catch (IllegalArgumentException exception) {
      return notFound(exception);
    }
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<?> status(
      @PathVariable String id, @Valid @RequestBody StatusRequest request) {
    try {
      return ResponseEntity.ok(accountService.setStatus(id, request.getStatus()));
    } catch (IllegalArgumentException exception) {
      return notFound(exception);
    }
  }

  @PutMapping("/{id}/skills")
  public ResponseEntity<?> skills(
      @PathVariable String id, @Valid @RequestBody SkillsRequest request) {
    try {
      return ResponseEntity.ok(accountService.updateSkills(id, request.getSkillIds()));
    } catch (IllegalArgumentException exception) {
      return notFound(exception);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable String id) {
    accountService.deleteAccount(id);
    return ResponseEntity.noContent().build();
  }

  private ResponseEntity<?> conflict(IllegalArgumentException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Collections.singletonMap("message", exception.getMessage()));
  }

  private ResponseEntity<?> notFound(IllegalArgumentException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Collections.singletonMap("message", exception.getMessage()));
  }

  public static class StatusRequest {
    @NotNull private AccountStatus status;

    public AccountStatus getStatus() {
      return status;
    }

    public void setStatus(AccountStatus status) {
      this.status = status;
    }
  }

  public static class SkillsRequest {
    @NotNull private List<String> skillIds = Collections.<String>emptyList();

    public List<String> getSkillIds() {
      return skillIds;
    }

    public void setSkillIds(List<String> skillIds) {
      this.skillIds = skillIds;
    }
  }
}
