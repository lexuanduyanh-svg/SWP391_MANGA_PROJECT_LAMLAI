package com.mangaworkflow.api.web.editor;

import com.mangaworkflow.api.domain.proposal.EditorProposalReviewRequest;
import com.mangaworkflow.api.domain.proposal.MangaProposalDto;
import com.mangaworkflow.api.application.proposal.InMemoryMangaProposalService;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tantou-editor/proposals")
public class TantouEditorProposalController {
  private final InMemoryMangaProposalService service;

  public TantouEditorProposalController(InMemoryMangaProposalService service) {
    this.service = service;
  }

  @GetMapping
  public List<MangaProposalDto> list(@RequestParam("editorEmail") String editorEmail) {
    return service.listForEditor(editorEmail);
  }

  @PutMapping("/{id}/forward-board")
  public ResponseEntity<?> forward(
      @PathVariable String id, @Valid @RequestBody EditorProposalReviewRequest request) {
    return okOrStatus(
        () -> service.forwardToBoard(id, request.getEditorEmail(), request.getNote()));
  }

  @PutMapping("/{id}/request-revision")
  public ResponseEntity<?> revise(
      @PathVariable String id, @Valid @RequestBody EditorProposalReviewRequest request) {
    return okOrStatus(
        () -> service.requestRevisionByEditor(id, request.getEditorEmail(), request.getNote()));
  }

  @PutMapping("/{id}/reject")
  public ResponseEntity<?> reject(
      @PathVariable String id, @Valid @RequestBody EditorProposalReviewRequest request) {
    return okOrStatus(
        () -> service.rejectByEditor(id, request.getEditorEmail(), request.getNote()));
  }

  private ResponseEntity<?> okOrStatus(Action action) {
    try {
      return ResponseEntity.ok(action.run());
    } catch (IllegalArgumentException e) {
      return status(e);
    }
  }

  private ResponseEntity<?> status(IllegalArgumentException e) {
    String msg = e.getMessage();
    HttpStatus code =
        msg != null && msg.contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
    return ResponseEntity.status(code).body(Collections.singletonMap("message", msg));
  }

  private static interface Action {
    MangaProposalDto run();
  }
}
