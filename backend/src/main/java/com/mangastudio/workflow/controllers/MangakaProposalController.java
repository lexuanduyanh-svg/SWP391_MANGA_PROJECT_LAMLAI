package com.mangastudio.workflow.controllers;

import com.mangastudio.workflow.dtos.MangaProposalCreateRequest;
import com.mangastudio.workflow.dtos.MangaProposalDto;
import com.mangastudio.workflow.dtos.MangaProposalSubmitRequest;
import com.mangastudio.workflow.dtos.MangaProposalUpdateRequest;
import com.mangastudio.workflow.services.InMemoryMangaProposalService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
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
  private static final long MAX_MANUSCRIPT_BYTES = 25L * 1024L * 1024L;

  @Value("${app.storage.manuscripts-dir:storage-server/manuscripts}")
  private String manuscriptStoragePath;

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

  @PostMapping("/preview-upload")
  public ResponseEntity<?> previewUpload(@RequestParam("file") MultipartFile file) {
    Map<String, Object> body = buildPreview(file);
    return ResponseEntity.status(Boolean.TRUE.equals(body.get("valid")) ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
        .body(body);
  }

  @PostMapping("/upload")
  public ResponseEntity<?> upload(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "proposalId", required = false) String proposalId,
      @RequestParam(value = "authorEmail", required = false) String authorEmail) {
    try {
      List<String> warnings = validateManuscript(file);
      if (!warnings.isEmpty()) throw new IllegalArgumentException(warnings.get(0));

      String originalName =
          file.getOriginalFilename() == null ? "manuscript" : file.getOriginalFilename();
      String summary = createSummary(file);
      String safeName = sanitizeFileName(originalName);
      String storedName = System.currentTimeMillis() + "-" + safeName;
      Path uploadDir = manuscriptStorageDirectory();
      Files.createDirectories(uploadDir);
      Path target = uploadDir.resolve(storedName);
      Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

      Map<String, Object> body = new LinkedHashMap<String, Object>();
      body.put("fileName", storedName);
      body.put("originalFileName", originalName);
      body.put("fileType", safeContentType(file));
      body.put("path", target.toString());
      body.put("summary", summary);

      if (!blank(proposalId) || !blank(authorEmail)) {
        if (blank(proposalId) || blank(authorEmail))
          throw new IllegalArgumentException("proposalId and authorEmail must be provided together");
        MangaProposalDto proposal =
            service.attachManuscriptMetadata(proposalId, authorEmail, storedName, summary);
        body.put("proposal", proposal);
      }

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
      Path uploadDir = manuscriptStorageDirectory();
      Files.createDirectories(uploadDir);
      Path file = uploadDir.resolve(safeName).normalize();
      if (!Files.exists(file)) {
        Path legacyFile = legacyManuscriptStorageDirectory().resolve(safeName).normalize();
        if (Files.exists(legacyFile) && Files.isRegularFile(legacyFile)) {
          file = legacyFile;
        }
      }
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

  private Path manuscriptStorageDirectory() {
    Path configured = Paths.get(manuscriptStoragePath == null ? "storage-server/manuscripts" : manuscriptStoragePath);
    if (configured.isAbsolute()) {
      return configured.normalize();
    }

    Path workingDirectory = Paths.get("").toAbsolutePath().normalize();
    Path projectRoot = workingDirectory;
    if ("backend".equals(workingDirectory.getFileName().toString())
        && workingDirectory.getParent() != null
        && "src".equals(workingDirectory.getParent().getFileName().toString())
        && workingDirectory.getParent().getParent() != null) {
      projectRoot = workingDirectory.getParent().getParent();
    }
    return projectRoot.resolve(configured).normalize();
  }

  private Path legacyManuscriptStorageDirectory() {
    return Paths.get(System.getProperty("user.home"), "swp391-uploads", "manuscripts")
        .toAbsolutePath()
        .normalize();
  }

  private Map<String, Object> buildPreview(MultipartFile file) {
    List<String> warnings = validateManuscript(file);
    Map<String, Object> body = new LinkedHashMap<String, Object>();
    String fileName = file == null ? null : file.getOriginalFilename();
    body.put("valid", warnings.isEmpty());
    body.put("summary", createSummary(file));
    body.put("warningMessages", warnings);
    body.put("fileName", fileName);
    body.put("fileType", file == null ? null : safeContentType(file));
    body.put("fileSize", file == null ? 0L : file.getSize());
    return body;
  }

  private static List<String> validateManuscript(MultipartFile file) {
    List<String> warnings = new ArrayList<String>();
    if (file == null || file.isEmpty()) {
      warnings.add("Manuscript file is required");
      return warnings;
    }
    if (file.getSize() > MAX_MANUSCRIPT_BYTES) {
      warnings.add("Manuscript file must be 25MB or smaller");
    }
    String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
    String extension = fileExtension(originalName);
    if (!("pdf".equals(extension)
        || "png".equals(extension)
        || "jpg".equals(extension)
        || "jpeg".equals(extension)
        || "webp".equals(extension)
        || "txt".equals(extension))) {
      warnings.add("Only PDF, PNG, JPG, JPEG, WEBP, or TXT manuscripts are allowed");
    }
    return warnings;
  }

  private static String createSummary(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return "No manuscript selected.";
    }
    String name = file.getOriginalFilename() == null ? "manuscript" : file.getOriginalFilename();
    String extension = fileExtension(name);
    long kb = Math.max(1L, file.getSize() / 1024L);
    if ("txt".equals(extension)) {
      try {
        byte[] bytes = file.getBytes();
        String content = new String(bytes, StandardCharsets.UTF_8).replaceAll("\\s+", " ").trim();
        if (!content.isEmpty()) {
          return content.length() > 180 ? content.substring(0, 180) + "..." : content;
        }
      } catch (IOException ignored) {
        return "Could not extract text content. File metadata preview is still available.";
      }
    }
    return "AI preview: "
        + name
        + " looks like a "
        + extension.toUpperCase(Locale.ROOT)
        + " manuscript file, about "
        + kb
        + " KB. Please confirm the title, genre, and file before submitting.";
  }

  private static String sanitizeFileName(String originalName) {
    String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
    return safeName.trim().isEmpty() ? "manuscript" : safeName;
  }

  private static String fileExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
      return "";
    }
    return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
  }

  private static boolean blank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private static String safeContentType(MultipartFile file) {
    return file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType();
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
