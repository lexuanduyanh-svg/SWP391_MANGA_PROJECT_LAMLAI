package com.mangastudio.workflow.controllers;

import com.mangastudio.workflow.dtos.ReaderMetricCreateRequest;
import com.mangastudio.workflow.dtos.ReaderMetricDto;
import com.mangastudio.workflow.dtos.SeriesRankingDto;
import com.mangastudio.workflow.services.InMemoryReaderMetricService;
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
public class ReaderMetricController {
  private final InMemoryReaderMetricService service;

  public ReaderMetricController(InMemoryReaderMetricService service) {
    this.service = service;
  }

  @GetMapping("/api/series/{seriesId}/metrics")
  public List<ReaderMetricDto> listMetrics(@PathVariable String seriesId) {
    return service.listMetrics(seriesId);
  }

  @PostMapping("/api/series/{seriesId}/metrics")
  public ResponseEntity<?> createMetric(
      @PathVariable String seriesId,
      @Valid @RequestBody ReaderMetricCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.createMetric(seriesId, request));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(Collections.singletonMap("message", e.getMessage()));
    }
  }

  @GetMapping("/api/series/{seriesId}/rankings")
  public ResponseEntity<?> getRanking(@PathVariable String seriesId) {
    try {
      return ResponseEntity.ok(service.computeRanking(seriesId));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(Collections.singletonMap("message", e.getMessage()));
    }
  }

  @GetMapping("/api/rankings")
  public List<SeriesRankingDto> getAllRankings() {
    return service.computeAllRankings();
  }
}
