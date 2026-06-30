package com.mangastudio.workflow.services;

import com.mangastudio.workflow.dtos.AnnotationCreateRequest;
import com.mangastudio.workflow.dtos.AnnotationDto;
import com.mangastudio.workflow.entities.AnnotationEntity;
import com.mangastudio.workflow.entities.PageEntity;
import com.mangastudio.workflow.entities.UserEntity;
import com.mangastudio.workflow.repositories.AnnotationRepository;
import com.mangastudio.workflow.repositories.PageRepository;
import com.mangastudio.workflow.repositories.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class InMemoryAnnotationService {
  private final AnnotationRepository annotationRepository;
  private final UserRepository userRepository;
  private final PageRepository pageRepository;
  private final Map<String, AnnotationRecord> annotations = new LinkedHashMap<String, AnnotationRecord>();
  private final AtomicLong seq = new AtomicLong(900);
  private final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  public InMemoryAnnotationService() {
    this.annotationRepository = null;
    this.userRepository = null;
    this.pageRepository = null;
    seed();
  }

  @Autowired
  public InMemoryAnnotationService(
      @Nullable AnnotationRepository annotationRepository,
      @Nullable UserRepository userRepository,
      @Nullable PageRepository pageRepository) {
    this.annotationRepository = annotationRepository;
    this.userRepository = userRepository;
    this.pageRepository = pageRepository;
    seed();
  }

  private boolean dbMode() {
    return annotationRepository != null && userRepository != null && pageRepository != null;
  }

  public synchronized List<AnnotationDto> listAnnotations(String pageId) {
    if (dbMode()) return listAnnotationsDb(pageId);
    List<AnnotationDto> out = new ArrayList<AnnotationDto>();
    for (AnnotationRecord r : annotations.values())
      if (pageId.equals(r.pageId)) out.add(r.toDto());
    return out;
  }

  public synchronized AnnotationDto createAnnotation(String pageId, AnnotationCreateRequest request) {
    if (request == null || request.getContent() == null || request.getContent().trim().isEmpty())
      throw new IllegalArgumentException("Annotation content is required");
    if (request.getEditorEmail() == null || request.getEditorEmail().trim().isEmpty())
      throw new IllegalArgumentException("Editor email is required");
    if (dbMode()) return createAnnotationDb(pageId, request);
    AnnotationRecord r = new AnnotationRecord(
        String.valueOf(seq.incrementAndGet()), pageId,
        request.getEditorEmail().trim().toLowerCase(Locale.ROOT),
        request.getSpatialCoordinates(), request.getContent().trim(),
        false, now());
    annotations.put(r.id, r);
    return r.toDto();
  }

  public synchronized AnnotationDto resolveAnnotation(String annotationId) {
    if (dbMode()) return resolveAnnotationDb(annotationId);
    AnnotationRecord r = annotations.get(annotationId);
    if (r == null) throw new IllegalArgumentException("Annotation not found");
    r.resolved = true;
    return r.toDto();
  }

  // ---- DB mode ----

  private List<AnnotationDto> listAnnotationsDb(String pageId) {
    List<AnnotationEntity> entities = annotationRepository
        .findByPage_IdOrderByCreatedAtAsc(Long.parseLong(pageId));
    List<AnnotationDto> out = new ArrayList<AnnotationDto>();
    for (AnnotationEntity e : entities) out.add(toDto(e));
    return out;
  }

  private AnnotationDto createAnnotationDb(String pageId, AnnotationCreateRequest request) {
    PageEntity page = pageRepository.findById(Long.parseLong(pageId))
        .orElseThrow(() -> new IllegalArgumentException("Page not found"));
    UserEntity editor = userRepository.findByEmailIgnoreCase(request.getEditorEmail().trim())
        .orElseThrow(() -> new IllegalArgumentException("Editor not found"));
    AnnotationEntity e = new AnnotationEntity();
    e.setPage(page);
    e.setEditor(editor);
    e.setSpatialCoordinates(request.getSpatialCoordinates());
    e.setContent(request.getContent().trim());
    e.setResolved(false);
    e.setCreatedAt(LocalDateTime.now());
    return toDto(annotationRepository.save(e));
  }

  private AnnotationDto resolveAnnotationDb(String annotationId) {
    AnnotationEntity e = annotationRepository.findById(Long.parseLong(annotationId))
        .orElseThrow(() -> new IllegalArgumentException("Annotation not found"));
    e.setResolved(true);
    return toDto(annotationRepository.save(e));
  }

  private AnnotationDto toDto(AnnotationEntity e) {
    return new AnnotationDto(
        String.valueOf(e.getId()),
        String.valueOf(e.getPage() != null ? e.getPage().getId() : null),
        e.getEditor() != null ? e.getEditor().getEmail() : null,
        e.getSpatialCoordinates(),
        e.getContent(),
        String.valueOf(e.getResolved()),
        e.getCreatedAt() != null ? e.getCreatedAt().format(f) : null);
  }

  // ---- Seed ----

  private void seed() {
    if (!annotations.isEmpty()) return;
    AnnotationRecord r = new AnnotationRecord(
        String.valueOf(seq.incrementAndGet()), "601",
        "editor@manga.local",
        "{\"x\":45.5,\"y\":30.2}",
        "Please adjust the line weight in this panel. The strokes are too thin for print.",
        false, now());
    annotations.put(r.id, r);
    r = new AnnotationRecord(
        String.valueOf(seq.incrementAndGet()), "601",
        "editor2@manga.local",
        "{\"x\":72.0,\"y\":65.8}",
        "The screentone pattern is too dense here. Consider using a lighter one.",
        true, now());
    annotations.put(r.id, r);
  }

  // ---- Helpers ----

  private String now() { return LocalDateTime.now().format(f); }

  private static class AnnotationRecord {
    String id, pageId, editorEmail, spatialCoordinates, content, createdAt;
    boolean resolved;

    AnnotationRecord(String id, String pageId, String editorEmail, String spatialCoordinates,
                     String content, boolean resolved, String createdAt) {
      this.id = id; this.pageId = pageId; this.editorEmail = editorEmail;
      this.spatialCoordinates = spatialCoordinates; this.content = content;
      this.resolved = resolved; this.createdAt = createdAt;
    }

    AnnotationDto toDto() {
      return new AnnotationDto(id, pageId, editorEmail, spatialCoordinates, content,
          String.valueOf(resolved), createdAt);
    }
  }
}
