package com.mangaworkflow.api.controller;

import com.mangaworkflow.api.model.MangaProposalCreateRequest;
import com.mangaworkflow.api.model.MangaProposalDto;
import com.mangaworkflow.api.model.MangaProposalSubmitRequest;
import com.mangaworkflow.api.model.MangaProposalUpdateRequest;
import com.mangaworkflow.api.service.InMemoryMangaProposalService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mangaka/proposals")
public class MangakaProposalController {
  private final InMemoryMangaProposalService service;

  public MangakaProposalController(InMemoryMangaProposalService service) {
    this.service = service;
  }

  @GetMapping
  public List<MangaProposalDto> list(@RequestParam("authorEmail") String authorEmail) {
    return service.listByAuthorEmail(authorEmail);
  }

  @PostMapping
  public ResponseEntity<?> create(@Valid @RequestBody MangaProposalCreateRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    } catch (IllegalArgumentException e) {
      return conflict(e);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(
      @PathVariable String id,
      @RequestParam(value = "authorEmail", required = false) String authorEmail,
      @Valid @RequestBody MangaProposalUpdateRequest request) {
    try {
      MangaProposalDto dto = service.update(id, authorEmail, request);
      return ResponseEntity.ok(dto);
    } catch (IllegalArgumentException e) {
      return status(e);
    }
  }

  @PutMapping("/{id}/submit")
  public ResponseEntity<?> submit(
      @PathVariable String id, @Valid @RequestBody MangaProposalSubmitRequest request) {
    try {
      return ResponseEntity.ok(service.submit(id, request.getAuthorEmail()));
    } catch (IllegalArgumentException e) {
      return status(e);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(
      @PathVariable String id, @RequestParam("authorEmail") String authorEmail) {
    try {
      service.delete(id, authorEmail);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return status(e);
    }
  }

  @PostMapping("/upload")
  public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
    try {
      if (file == null || file.isEmpty())
        throw new IllegalArgumentException("Manuscript file is required");
      String originalName =
          file.getOriginalFilename() == null ? "manuscript" : file.getOriginalFilename();
      String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
      if (safeName.trim().isEmpty()) safeName = "manuscript";
      String storedName = System.currentTimeMillis() + "-" + safeName;
      Path uploadDir = Paths.get(System.getProperty("user.home"), "swp391-uploads", "manuscripts");
      Files.createDirectories(uploadDir);
      Path target = uploadDir.resolve(storedName);
      Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
      Map<String, String> body = new LinkedHashMap<>();
      body.put("fileName", storedName);
      body.put("originalFileName", originalName);
      body.put("path", target.toString());
      return ResponseEntity.status(HttpStatus.CREATED).body(body);
    } catch (IllegalArgumentException e) {
      return conflict(e);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Collections.singletonMap("message", "Could not save manuscript file"));
    }
  }

  @GetMapping("/files/{fileName:.+}")
  public ResponseEntity<?> download(@PathVariable String fileName) {
    try {
      String safeName = Paths.get(fileName).getFileName().toString();
      Path uploadDir = Paths.get(System.getProperty("user.home"), "swp391-uploads", "manuscripts");
      Files.createDirectories(uploadDir);
      Path file = uploadDir.resolve(safeName).normalize();
      if (!Files.exists(file) && safeName.matches("seed-manuscript-[0-9]+\\.pdf"))
        Files.write(
            file, ("Demo manuscript placeholder for " + safeName).getBytes(StandardCharsets.UTF_8));
      if (!Files.exists(file) || !Files.isRegularFile(file))
        throw new IllegalArgumentException("File not found");
      Resource resource = new UrlResource(file.toUri());
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeName + "\"")
          .body(resource);
    } catch (IllegalArgumentException e) {
      return status(e);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Collections.singletonMap("message", "Could not download manuscript file"));
    }
  }

  private ResponseEntity<?> conflict(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Collections.singletonMap("message", e.getMessage()));
  }

  private ResponseEntity<?> status(IllegalArgumentException e) {
    String msg = e.getMessage();
    HttpStatus code =
        msg != null && (msg.contains("not found") || msg.contains("belong"))
            ? HttpStatus.NOT_FOUND
            : HttpStatus.CONFLICT;
    return ResponseEntity.status(code).body(Collections.singletonMap("message", msg));
  }
}
