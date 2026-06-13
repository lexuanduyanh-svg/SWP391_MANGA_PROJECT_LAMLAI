package com.mangaworkflow.api.mangaka;

import com.mangaworkflow.domain.production.*;
import com.mangaworkflow.application.production.InMemoryMangakaProductionService;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mangaka/proposals/{proposalId}")
public class MangakaProductionController {
  private final InMemoryMangakaProductionService service;

  public MangakaProductionController(InMemoryMangakaProductionService service) {
    this.service = service;
  }

  @GetMapping("/chapters")
  public List<MangakaChapterDto> list(
      @PathVariable String proposalId, @RequestParam("authorEmail") String authorEmail) {
    return service.listChapters(proposalId, authorEmail);
  }

  @PostMapping("/chapters")
  public ResponseEntity<?> create(
      @PathVariable String proposalId,
      @RequestParam("authorEmail") String authorEmail,
      @Valid @RequestBody MangakaChapterCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.createChapter(proposalId, authorEmail, request));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  @PostMapping("/chapters/{chapterId}/pages")
  public ResponseEntity<?> addPage(
      @PathVariable String proposalId,
      @PathVariable String chapterId,
      @RequestParam("authorEmail") String authorEmail,
      @Valid @RequestBody MangakaPageCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.addPage(proposalId, chapterId, authorEmail, request));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  @PostMapping("/chapters/{chapterId}/pages/{pageId}/regions")
  public ResponseEntity<?> addRegion(
      @PathVariable String proposalId,
      @PathVariable String chapterId,
      @PathVariable String pageId,
      @RequestParam("authorEmail") String authorEmail,
      @Valid @RequestBody MangakaPageRegionCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.addRegion(proposalId, chapterId, pageId, authorEmail, request));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  @PostMapping("/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks")
  public ResponseEntity<?> addTask(
      @PathVariable String proposalId,
      @PathVariable String chapterId,
      @PathVariable String pageId,
      @PathVariable String regionId,
      @RequestParam("authorEmail") String authorEmail,
      @Valid @RequestBody MangakaProductionTaskCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.assignTask(proposalId, chapterId, pageId, regionId, authorEmail, request));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  @PutMapping("/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/approve")
  public ResponseEntity<?> approve(
      @PathVariable String proposalId,
      @PathVariable String chapterId,
      @PathVariable String pageId,
      @PathVariable String regionId,
      @PathVariable String taskId,
      @RequestParam("authorEmail") String authorEmail) {
    try {
      return ResponseEntity.ok(
          service.approveTask(proposalId, chapterId, pageId, regionId, taskId, authorEmail));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  @PutMapping("/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/redo")
  public ResponseEntity<?> redo(
      @PathVariable String proposalId,
      @PathVariable String chapterId,
      @PathVariable String pageId,
      @PathVariable String regionId,
      @PathVariable String taskId,
      @RequestParam("authorEmail") String authorEmail) {
    try {
      return ResponseEntity.ok(
          service.requestRedoTask(proposalId, chapterId, pageId, regionId, taskId, authorEmail));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  private ResponseEntity<?> error(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Collections.singletonMap("message", e.getMessage()));
  }
}
