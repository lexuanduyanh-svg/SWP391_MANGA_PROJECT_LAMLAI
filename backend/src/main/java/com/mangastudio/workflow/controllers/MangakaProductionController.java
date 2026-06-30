package com.mangastudio.workflow.controllers;

import com.mangastudio.workflow.dtos.*;
import com.mangastudio.workflow.services.InMemoryMangakaProductionService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mangaka/series/{seriesId}")
public class MangakaProductionController {
  private final InMemoryMangakaProductionService service;

  public MangakaProductionController(InMemoryMangakaProductionService service) {
    this.service = service;
  }

  // --- Chapter endpoints ---

  @GetMapping("/chapters")
  public List<MangakaChapterDto> list(
      @PathVariable String seriesId, @RequestParam("authorEmail") String authorEmail) {
    return service.listChapters(seriesId, authorEmail);
  }

  @PostMapping("/chapters")
  public ResponseEntity<?> create(
      @PathVariable String seriesId,
      @RequestParam("authorEmail") String authorEmail,
      @Valid @RequestBody MangakaChapterCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.createChapter(seriesId, authorEmail, request));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  // --- Page endpoints ---

  @PostMapping("/chapters/{chapterId}/pages")
  public ResponseEntity<?> addPage(
      @PathVariable String seriesId,
      @PathVariable String chapterId,
      @RequestParam("authorEmail") String authorEmail,
      @Valid @RequestBody MangakaPageCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.addPage(seriesId, chapterId, authorEmail, request));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  // --- Region endpoints (restored to full scope) ---

  @GetMapping("/chapters/{chapterId}/pages/{pageId}/regions")
  public ResponseEntity<?> listRegions(
      @PathVariable String seriesId,
      @PathVariable String chapterId,
      @PathVariable String pageId,
      @RequestParam("authorEmail") String authorEmail) {
    try {
      return ResponseEntity.ok(service.listRegions(seriesId, chapterId, pageId, authorEmail));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  @PostMapping("/chapters/{chapterId}/pages/{pageId}/regions")
  public ResponseEntity<?> createRegion(
      @PathVariable String seriesId,
      @PathVariable String chapterId,
      @PathVariable String pageId,
      @RequestParam("authorEmail") String authorEmail,
      @Valid @RequestBody MangakaPageRegionCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.createRegion(seriesId, chapterId, pageId, authorEmail, request));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  @DeleteMapping("/chapters/{chapterId}/pages/{pageId}/regions/{regionId}")
  public ResponseEntity<?> deleteRegion(
      @PathVariable String seriesId,
      @PathVariable String chapterId,
      @PathVariable String pageId,
      @PathVariable String regionId,
      @RequestParam("authorEmail") String authorEmail) {
    try {
      service.deleteRegion(seriesId, chapterId, pageId, regionId, authorEmail);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  // --- Task endpoints (restored with region support) ---

  @PostMapping("/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks")
  public ResponseEntity<?> addTask(
      @PathVariable String seriesId,
      @PathVariable String chapterId,
      @PathVariable String pageId,
      @PathVariable String regionId,
      @RequestParam("authorEmail") String authorEmail,
      @Valid @RequestBody MangakaProductionTaskCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.assignTask(seriesId, chapterId, pageId, regionId, authorEmail, request));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  @PostMapping("/chapters/{chapterId}/pages/{pageId}/tasks")
  public ResponseEntity<?> addTaskPageLevel(
      @PathVariable String seriesId,
      @PathVariable String chapterId,
      @PathVariable String pageId,
      @RequestParam("authorEmail") String authorEmail,
      @Valid @RequestBody MangakaProductionTaskCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.assignTask(seriesId, chapterId, pageId, null, authorEmail, request));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  @PutMapping("/chapters/{chapterId}/pages/{pageId}/tasks/{taskId}/approve")
  public ResponseEntity<?> approve(
      @PathVariable String seriesId,
      @PathVariable String chapterId,
      @PathVariable String pageId,
      @PathVariable String taskId,
      @RequestParam("authorEmail") String authorEmail) {
    try {
      return ResponseEntity.ok(
          service.approveTask(seriesId, chapterId, pageId, taskId, authorEmail));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  @PutMapping("/chapters/{chapterId}/pages/{pageId}/tasks/{taskId}/redo")
  public ResponseEntity<?> redo(
      @PathVariable String seriesId,
      @PathVariable String chapterId,
      @PathVariable String pageId,
      @PathVariable String taskId,
      @RequestParam("authorEmail") String authorEmail) {
    try {
      return ResponseEntity.ok(
          service.requestRedoTask(seriesId, chapterId, pageId, taskId, authorEmail));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  // --- Chapter completion ---

  @PutMapping("/chapters/{chapterId}/complete")
  public ResponseEntity<?> completeChapter(
      @PathVariable String seriesId,
      @PathVariable String chapterId,
      @RequestParam("authorEmail") String authorEmail) {
    try {
      return ResponseEntity.ok(service.completeChapter(seriesId, chapterId, authorEmail));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  private ResponseEntity<?> error(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Collections.singletonMap("message", e.getMessage()));
  }
}