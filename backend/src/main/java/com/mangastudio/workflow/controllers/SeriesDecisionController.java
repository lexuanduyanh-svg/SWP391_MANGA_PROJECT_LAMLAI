package com.mangastudio.workflow.controllers;

import com.mangastudio.workflow.dtos.SeriesDecisionDto;
import com.mangastudio.workflow.dtos.SeriesDecisionRequest;
import com.mangastudio.workflow.services.InMemorySeriesDecisionService;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeriesDecisionController {
  private final InMemorySeriesDecisionService service;

  public SeriesDecisionController(InMemorySeriesDecisionService service) {
    this.service = service;
  }

  @GetMapping("/api/series/{seriesId}/decisions")
  public List<SeriesDecisionDto> listDecisions(@PathVariable String seriesId) {
    return service.listDecisions(seriesId);
  }

  @PostMapping("/api/series/{seriesId}/decisions")
  public ResponseEntity<?> makeDecision(
      @PathVariable String seriesId,
      @Valid @RequestBody SeriesDecisionRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.makeDecision(seriesId, request));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(Collections.singletonMap("message", e.getMessage()));
    }
  }

  @GetMapping("/api/decisions")
  public List<SeriesDecisionDto> listAllDecisions() {
    return service.listAllDecisions();
  }
}
