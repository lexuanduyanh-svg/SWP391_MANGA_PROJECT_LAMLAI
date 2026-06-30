package com.mangastudio.workflow.controllers;

import com.mangastudio.workflow.dtos.AnnotationCreateRequest;
import com.mangastudio.workflow.dtos.AnnotationDto;
import com.mangastudio.workflow.services.InMemoryAnnotationService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pages/{pageId}/annotations")
public class AnnotationController {
  private final InMemoryAnnotationService service;

  public AnnotationController(InMemoryAnnotationService service) {
    this.service = service;
  }

  @GetMapping
  public List<AnnotationDto> list(@PathVariable String pageId) {
    return service.listAnnotations(pageId);
  }

  @PostMapping
  public ResponseEntity<?> create(
      @PathVariable String pageId,
      @Valid @RequestBody AnnotationCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.createAnnotation(pageId, request));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  @PutMapping("/{annotationId}/resolve")
  public ResponseEntity<?> resolve(
      @PathVariable String pageId,
      @PathVariable String annotationId) {
    try {
      return ResponseEntity.ok(service.resolveAnnotation(annotationId));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  private ResponseEntity<?> error(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Collections.singletonMap("message", e.getMessage()));
  }
}
