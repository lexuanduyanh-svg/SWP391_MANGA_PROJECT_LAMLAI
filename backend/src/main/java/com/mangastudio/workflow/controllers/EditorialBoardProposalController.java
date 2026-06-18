package com.mangastudio.workflow.controllers;

import com.mangastudio.workflow.dtos.BoardProposalDecisionRequest;
import com.mangastudio.workflow.dtos.MangaProposalDto;
import com.mangastudio.workflow.services.InMemoryMangaProposalService;
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
@RequestMapping("/api/editorial-board/proposals")
public class EditorialBoardProposalController {
  private final InMemoryMangaProposalService service;

  public EditorialBoardProposalController(InMemoryMangaProposalService service) {
    this.service = service;
  }

  @GetMapping
  public List<MangaProposalDto> list(@RequestParam("memberEmail") String memberEmail) {
    return service.listForBoard(memberEmail);
  }

  @PutMapping("/{id}/approve")
  public ResponseEntity<?> approve(
      @PathVariable String id, @Valid @RequestBody BoardProposalDecisionRequest request) {
    return okOrStatus(
        () -> service.approveByBoard(id, request.getMemberEmail(), request.getNote()));
  }

  @PutMapping("/{id}/reject")
  public ResponseEntity<?> reject(
      @PathVariable String id, @Valid @RequestBody BoardProposalDecisionRequest request) {
    return okOrStatus(() -> service.rejectByBoard(id, request.getMemberEmail(), request.getNote()));
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
