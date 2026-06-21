package com.mangastudio.workflow.controllers;

import com.mangastudio.workflow.dtos.AccountCreateRequest;
import com.mangastudio.workflow.dtos.AccountDto;
import com.mangastudio.workflow.dtos.AccountStatus;
import com.mangastudio.workflow.dtos.AccountUpdateRequest;
import com.mangastudio.workflow.dtos.SkillCategoryCreateRequest;
import com.mangastudio.workflow.dtos.SkillCategoryDto;
import com.mangastudio.workflow.dtos.SkillCategoryUpdateRequest;
import com.mangastudio.workflow.dtos.SkillStatusRequest;
import com.mangastudio.workflow.services.InMemoryAccountService;
import com.mangastudio.workflow.services.InMemorySkillCategoryService;
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
@RequestMapping("/admin")
public class AdminController {

  private final InMemoryAccountService accountService;
  private final InMemorySkillCategoryService skillService;

  public AdminController(InMemoryAccountService accountService,
                         InMemorySkillCategoryService skillService) {
    this.accountService = accountService;
    this.skillService = skillService;
  }

  // ========== Account endpoints ==========

  @GetMapping("/accounts")
  public List<AccountDto> listAccounts() {
    return accountService.listAccounts();
  }

  @PostMapping("/accounts")
  public ResponseEntity<?> createAccount(@Valid @RequestBody AccountCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    } catch (IllegalArgumentException exception) {
      return conflict(exception);
    }
  }

  @PutMapping("/accounts/{id}")
  public ResponseEntity<?> updateAccount(
      @PathVariable String id, @Valid @RequestBody AccountUpdateRequest request) {
    try {
      return ResponseEntity.ok(accountService.updateAccount(id, request));
    } catch (IllegalArgumentException exception) {
      return notFound(exception);
    }
  }

  @PutMapping("/accounts/{id}/status")
  public ResponseEntity<?> setAccountStatus(
      @PathVariable String id, @Valid @RequestBody StatusRequest request) {
    try {
      return ResponseEntity.ok(accountService.setStatus(id, request.getStatus()));
    } catch (IllegalArgumentException exception) {
      return notFound(exception);
    }
  }

  @PutMapping("/accounts/{id}/skills")
  public ResponseEntity<?> updateAccountSkills(
      @PathVariable String id, @Valid @RequestBody SkillsRequest request) {
    try {
      return ResponseEntity.ok(accountService.updateSkills(id, request.getSkillIds()));
    } catch (IllegalArgumentException exception) {
      return notFound(exception);
    }
  }

  @DeleteMapping("/accounts/{id}")
  public ResponseEntity<?> deleteAccount(@PathVariable String id) {
    accountService.deleteAccount(id);
    return ResponseEntity.noContent().build();
  }

  // ========== Skill endpoints ==========

  @GetMapping("/skills")
  public List<SkillCategoryDto> listSkills() {
    return skillService.listSkills();
  }

  @PostMapping("/skills")
  public ResponseEntity<?> createSkill(@Valid @RequestBody SkillCategoryCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED).body(skillService.create(request));
    } catch (IllegalArgumentException exception) {
      return conflict(exception);
    }
  }

  @PutMapping("/skills/{id}")
  public ResponseEntity<?> updateSkill(
      @PathVariable String id, @Valid @RequestBody SkillCategoryUpdateRequest request) {
    try {
      return ResponseEntity.ok(skillService.update(id, request));
    } catch (IllegalArgumentException exception) {
      return notFound(exception);
    }
  }

  @PutMapping("/skills/{id}/status")
  public ResponseEntity<?> setSkillStatus(
      @PathVariable String id, @Valid @RequestBody SkillStatusRequest request) {
    try {
      return ResponseEntity.ok(skillService.setActive(id, Boolean.TRUE.equals(request.getActive())));
    } catch (IllegalArgumentException exception) {
      return notFound(exception);
    }
  }

  @DeleteMapping("/skills/{id}")
  public ResponseEntity<?> deleteSkill(@PathVariable String id) {
    try {
      skillService.delete(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException exception) {
      return notFound(exception);
    }
  }

  // ========== Helpers ==========

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
