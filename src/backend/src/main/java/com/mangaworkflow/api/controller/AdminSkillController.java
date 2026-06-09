package com.mangaworkflow.api.controller;

import com.mangaworkflow.api.model.SkillCategoryCreateRequest;
import com.mangaworkflow.api.model.SkillCategoryDto;
import com.mangaworkflow.api.model.SkillCategoryUpdateRequest;
import com.mangaworkflow.api.model.SkillStatusRequest;
import com.mangaworkflow.api.service.InMemorySkillCategoryService;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
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
@RequestMapping("/api/admin/skills")
public class AdminSkillController {
  private final InMemorySkillCategoryService service;

  public AdminSkillController(InMemorySkillCategoryService service) {
    this.service = service;
  }

  @GetMapping
  public List<SkillCategoryDto> list() {
    return service.listSkills();
  }

  @PostMapping
  public ResponseEntity<?> create(@Valid @RequestBody SkillCategoryCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    } catch (IllegalArgumentException exception) {
      return conflict(exception);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(
      @PathVariable String id, @Valid @RequestBody SkillCategoryUpdateRequest request) {
    try {
      return ResponseEntity.ok(service.update(id, request));
    } catch (IllegalArgumentException exception) {
      return notFound(exception);
    }
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<?> status(
      @PathVariable String id, @Valid @RequestBody SkillStatusRequest request) {
    try {
      return ResponseEntity.ok(service.setActive(id, Boolean.TRUE.equals(request.getActive())));
    } catch (IllegalArgumentException exception) {
      return notFound(exception);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable String id) {
    try {
      service.delete(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException exception) {
      return notFound(exception);
    }
  }

  private ResponseEntity<?> conflict(IllegalArgumentException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Collections.singletonMap("message", exception.getMessage()));
  }

  private ResponseEntity<?> notFound(IllegalArgumentException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Collections.singletonMap("message", exception.getMessage()));
  }
}
