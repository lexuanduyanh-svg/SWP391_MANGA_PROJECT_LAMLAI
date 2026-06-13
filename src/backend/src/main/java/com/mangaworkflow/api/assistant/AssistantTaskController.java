package com.mangaworkflow.api.assistant;

import com.mangaworkflow.domain.task.*;
import com.mangaworkflow.application.production.InMemoryMangakaProductionService;
import java.util.HashMap;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant/tasks")
public class AssistantTaskController {
  private final InMemoryMangakaProductionService service;

  public AssistantTaskController(InMemoryMangakaProductionService service) {
    this.service = service;
  }

  @GetMapping
  public List<AssistantTaskDto> list(@RequestParam("assistantEmail") String assistantEmail) {
    return service.listAssistantTasks(assistantEmail);
  }

  @PutMapping("/{taskId}/start")
  public ResponseEntity<?> start(
      @PathVariable String taskId, @RequestBody AssistantTaskActionRequest request) {
    try {
      return ResponseEntity.ok(service.startAssistantTask(taskId, request.getAssistantEmail()));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  @PutMapping("/{taskId}/submit")
  public ResponseEntity<?> submit(
      @PathVariable String taskId, @RequestBody AssistantTaskSubmitRequest request) {
    try {
      return ResponseEntity.ok(
          service.submitAssistantTask(
              taskId,
              request.getAssistantEmail(),
              request.getSubmittedFileName(),
              request.getSubmissionNote()));
    } catch (IllegalArgumentException e) {
      return error(e);
    }
  }

  private ResponseEntity<?> error(IllegalArgumentException e) {
    HttpStatus status =
        (e.getMessage() != null && e.getMessage().contains("not found"))
            ? HttpStatus.NOT_FOUND
            : HttpStatus.CONFLICT;
    return ResponseEntity.status(status)
        .body(
            new HashMap<String, String>() {
              {
                put("message", e.getMessage());
              }
            });
  }
}
