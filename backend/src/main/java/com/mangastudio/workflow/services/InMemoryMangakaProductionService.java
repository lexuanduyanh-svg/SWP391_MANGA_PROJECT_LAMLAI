package com.mangastudio.workflow.services;

import com.mangastudio.workflow.services.InMemoryMangaProposalService.SeriesRecord;
import com.mangastudio.workflow.dtos.*;
import com.mangastudio.workflow.entities.*;
import com.mangastudio.workflow.repositories.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class InMemoryMangakaProductionService {
  private final InMemoryMangaProposalService proposals;
  private final SeriesRepository seriesRepository;
  private final ChapterRepository chapterRepository;
  private final PageRepository pageRepository;
  private final TaskRepository taskRepository;
  private final SubmissionRepository submissionRepository;
  private final UserRepository userRepository;
  private final Map<String, ChapterRecord> chapters = new LinkedHashMap<String, ChapterRecord>();
  private final AtomicLong seq = new AtomicLong(500);
  private final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  /** 1-arg constructor for tests (memory mode only). */
  public InMemoryMangakaProductionService(InMemoryMangaProposalService proposals) {
    this(proposals, null, null, null, null, null, null);
  }

  @Autowired
  public InMemoryMangakaProductionService(
      InMemoryMangaProposalService proposals,
      @Nullable SeriesRepository seriesRepository,
      @Nullable ChapterRepository chapterRepository,
      @Nullable PageRepository pageRepository,
      @Nullable TaskRepository taskRepository,
      @Nullable SubmissionRepository submissionRepository,
      @Nullable UserRepository userRepository) {
    this.proposals = proposals;
    this.seriesRepository = seriesRepository;
    this.chapterRepository = chapterRepository;
    this.pageRepository = pageRepository;
    this.taskRepository = taskRepository;
    this.submissionRepository = submissionRepository;
    this.userRepository = userRepository;
    seedMemory();
    seedDbIfPossible();
  }

  // ------------------------------------------------------------------
  // Public API
  // ------------------------------------------------------------------

  public synchronized List<MangakaChapterDto> listChapters(String seriesId, String authorEmail) {
    return dbMode() ? listChaptersDb(seriesId, authorEmail) : listChaptersMemory(seriesId, authorEmail);
  }

  public synchronized MangakaChapterDto createChapter(
      String seriesId, String authorEmail, MangakaChapterCreateRequest r) {
    return dbMode() ? createChapterDb(seriesId, authorEmail, r) : createChapterMemory(seriesId, authorEmail, r);
  }

  public synchronized MangakaPageDto addPage(
      String seriesId, String chapterId, String authorEmail, MangakaPageCreateRequest r) {
    return dbMode() ? addPageDb(seriesId, chapterId, authorEmail, r) : addPageMemory(seriesId, chapterId, authorEmail, r);
  }

  // --- Region CRUD ---

  public synchronized List<MangakaPageRegionDto> listRegions(
      String seriesId, String chapterId, String pageId, String authorEmail) {
    return listRegionsMemory(seriesId, chapterId, pageId, authorEmail);
  }

  public synchronized MangakaPageRegionDto createRegion(
      String seriesId, String chapterId, String pageId, String authorEmail, MangakaPageRegionCreateRequest r) {
    return createRegionMemory(seriesId, chapterId, pageId, authorEmail, r);
  }

  public synchronized void deleteRegion(
      String seriesId, String chapterId, String pageId, String regionId, String authorEmail) {
    deleteRegionMemory(seriesId, chapterId, pageId, regionId, authorEmail);
  }

  // --- Task assignment (regionId can be null for page-level fallback) ---

  public synchronized MangakaProductionTaskDto assignTask(
      String seriesId, String chapterId, String pageId, String regionId,
      String authorEmail, MangakaProductionTaskCreateRequest request) {
    return dbMode()
        ? assignTaskDb(seriesId, chapterId, pageId, regionId, authorEmail, request)
        : assignTaskMemory(seriesId, chapterId, pageId, regionId, authorEmail, request);
  }

  public synchronized List<AssistantTaskDto> listAssistantTasks(String assistantEmail) {
    return dbMode() ? listAssistantTasksDb(assistantEmail) : listAssistantTasksMemory(assistantEmail);
  }

  public synchronized AssistantTaskDto startAssistantTask(String taskId, String assistantEmail) {
    return dbMode() ? startAssistantTaskDb(taskId, assistantEmail) : startAssistantTaskMemory(taskId, assistantEmail);
  }

  public synchronized AssistantTaskDto submitAssistantTask(
      String taskId, String assistantEmail, String submittedFileName, String submissionNote) {
    return dbMode()
        ? submitAssistantTaskDb(taskId, assistantEmail, submittedFileName, submissionNote)
        : submitAssistantTaskMemory(taskId, assistantEmail, submittedFileName, submissionNote);
  }

  public synchronized MangakaProductionTaskDto approveTask(
      String seriesId, String chapterId, String pageId, String taskId, String authorEmail) {
    return dbMode()
        ? approveTaskDb(seriesId, chapterId, pageId, taskId, authorEmail)
        : approveTaskMemory(seriesId, chapterId, pageId, taskId, authorEmail);
  }

  public synchronized MangakaProductionTaskDto requestRedoTask(
      String seriesId, String chapterId, String pageId, String taskId, String authorEmail) {
    return dbMode()
        ? requestRedoTaskDb(seriesId, chapterId, pageId, taskId, authorEmail)
        : requestRedoTaskMemory(seriesId, chapterId, pageId, taskId, authorEmail);
  }

  /** Complete a chapter: all tasks must be Approved, then status → COMPLETED. */
  public synchronized MangakaChapterDto completeChapter(
      String seriesId, String chapterId, String authorEmail) {
    return dbMode()
        ? completeChapterDb(seriesId, chapterId, authorEmail)
        : completeChapterMemory(seriesId, chapterId, authorEmail);
  }

  // ------------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------------

  private boolean dbMode() {
    return seriesRepository != null && chapterRepository != null && pageRepository != null
        && taskRepository != null && submissionRepository != null && userRepository != null;
  }

  private String now() { return LocalDateTime.now().format(f); }
  private String id() { return String.valueOf(seq.incrementAndGet()); }
  private boolean blank(String s) { return s == null || s.trim().isEmpty(); }

  private String toTaskDbStatus(MangakaTaskStatus status) {
    switch (status) {
      case Pending:       return "ASSIGNED";
      case InProgress:    return "IN_PROGRESS";
      case Submitted:     return "PENDING_REVIEW";
      case Approved:      return "APPROVED";
      case RedoRequested: return "REVISION_REQUESTED";
      default:            return status.name();
    }
  }

  private MangakaTaskStatus fromTaskDbStatus(String dbStatus) {
    if (dbStatus == null) return MangakaTaskStatus.Pending;
    switch (dbStatus) {
      case "ASSIGNED":           return MangakaTaskStatus.Pending;
      case "IN_PROGRESS":        return MangakaTaskStatus.InProgress;
      case "PENDING_REVIEW":     return MangakaTaskStatus.Submitted;
      case "APPROVED":           return MangakaTaskStatus.Approved;
      case "REVISION_REQUESTED": return MangakaTaskStatus.RedoRequested;
      default:                   return MangakaTaskStatus.Pending;
    }
  }

  // ------------------------------------------------------------------
  // Seed
  // ------------------------------------------------------------------

  private void seedMemory() {
    if (!chapters.isEmpty()) return;

    SeriesRecord sr = proposals.getSeriesRecord("801");
    if (sr == null) sr = findSeriesForProposal("4");
    String seriesId = sr != null ? sr.id : "4";

    // Seed with region
    ChapterRecord legacyChapter =
        new ChapterRecord("600", seriesId, "Seed Production Chapter", 1, MangakaChapterStatus.IN_PROGRESS);
    PageRecord legacyPage =
        new PageRecord("601", "600", 1, "seed-page.png", MangakaPageStatus.DRAFT);
    RegionRecord legacyRegion =
        new RegionRecord("701", "601", "panel", 10.0, 10.0, 80.0, 80.0, "Main panel");
    legacyRegion.task =
        new TaskRecord(
            "603", "601", "701",
            "assistant@manga.local",
            "Clean and letter this panel",
            null,
            MangakaTaskStatus.Pending);
    legacyPage.regions.put(legacyRegion.id, legacyRegion);
    legacyChapter.pages.put(legacyPage.id, legacyPage);
    chapters.put(legacyChapter.id, legacyChapter);

    String pid = "201";
    String cid = id();
    String pageId = id();
    ChapterRecord c = new ChapterRecord(cid, pid, "Seed Chapter", 1, MangakaChapterStatus.IN_PROGRESS);
    PageRecord p = new PageRecord(pageId, cid, 1, "seed-page.png", MangakaPageStatus.DRAFT);
    c.pages.put(pageId, p);
    chapters.put(cid, c);
  }

  private SeriesRecord findSeriesForProposal(String proposalId) {
    for (long i = 801; i <= 810; i++) {
      SeriesRecord r = proposals.getSeriesRecord(String.valueOf(i));
      if (r != null && proposalId.equals(r.proposalId)) return r;
    }
    return null;
  }

  private void seedDbIfPossible() {}

  // ------------------------------------------------------------------
  // Memory mode helpers
  // ------------------------------------------------------------------

  private void ensureAllowedMemory(String seriesId, String authorEmail) {
    SeriesRecord sr = proposals.getSeriesRecord(seriesId);
    if (sr == null) throw new IllegalArgumentException("Series not found");
    if (authorEmail == null || !normalize(authorEmail).equals(normalize(sr.mangakaEmail)))
      throw new IllegalArgumentException("Series does not belong to this mangaka");
    if (!"ACTIVE".equals(sr.status)) throw new IllegalArgumentException("Series is not active");
  }

  // ------------------------------------------------------------------
  // MEMORY MODE — chapters
  // ------------------------------------------------------------------

  private List<MangakaChapterDto> listChaptersMemory(String seriesId, String authorEmail) {
    ensureAllowedMemory(seriesId, authorEmail);
    List<MangakaChapterDto> out = new ArrayList<MangakaChapterDto>();
    for (ChapterRecord c : chapters.values())
      if (seriesId.equals(c.proposalId)) out.add(c.toDto(loadPagesMemory(c.id)));
    return out;
  }

  private MangakaChapterDto createChapterMemory(String seriesId, String authorEmail, MangakaChapterCreateRequest r) {
    ensureAllowedMemory(seriesId, authorEmail);
    if (r == null || blank(r.getTitle()) || r.getChapterNumber() <= 0)
      throw new IllegalArgumentException("Chapter data is required");
    ChapterRecord c = new ChapterRecord(id(), seriesId, r.getTitle().trim(), r.getChapterNumber(), MangakaChapterStatus.DRAFT);
    chapters.put(c.id, c);
    return c.toDto(new ArrayList<MangakaPageDto>());
  }

  private MangakaPageDto addPageMemory(String seriesId, String chapterId, String authorEmail, MangakaPageCreateRequest r) {
    ChapterRecord c = chapterRequiredMemory(seriesId, chapterId, authorEmail);
    if (r == null || r.getPageNumber() <= 0 || blank(r.getFileName()))
      throw new IllegalArgumentException("Page data is required");
    PageRecord p = new PageRecord(id(), c.id, r.getPageNumber(), r.getFileName().trim(), MangakaPageStatus.DRAFT);
    c.pages.put(p.id, p);
    c.status = MangakaChapterStatus.IN_PROGRESS;
    c.updatedAt = now();
    return p.toDto(null);
  }

  // ------------------------------------------------------------------
  // MEMORY MODE — regions
  // ------------------------------------------------------------------

  private List<MangakaPageRegionDto> listRegionsMemory(
      String seriesId, String chapterId, String pageId, String authorEmail) {
    PageRecord p = pageRequiredMemory(seriesId, chapterId, pageId, authorEmail);
    List<MangakaPageRegionDto> out = new ArrayList<MangakaPageRegionDto>();
    for (RegionRecord r : p.regions.values()) out.add(r.toDto());
    return out;
  }

  private MangakaPageRegionDto createRegionMemory(
      String seriesId, String chapterId, String pageId, String authorEmail, MangakaPageRegionCreateRequest r) {
    PageRecord p = pageRequiredMemory(seriesId, chapterId, pageId, authorEmail);
    RegionRecord rr = new RegionRecord(id(), pageId, r.getRegionType(), r.getX(), r.getY(), r.getWidthPct(), r.getHeightPct(), r.getNote());
    p.regions.put(rr.id, rr);
    return rr.toDto();
  }

  private void deleteRegionMemory(
      String seriesId, String chapterId, String pageId, String regionId, String authorEmail) {
    PageRecord p = pageRequiredMemory(seriesId, chapterId, pageId, authorEmail);
    RegionRecord removed = p.regions.remove(regionId);
    if (removed == null) throw new IllegalArgumentException("Region not found");
    // no status change needed (DRAFT stays DRAFT)
  }

  // ------------------------------------------------------------------
  // MEMORY MODE — tasks
  // ------------------------------------------------------------------

  private MangakaProductionTaskDto assignTaskMemory(
      String seriesId, String chapterId, String pageId, String regionId,
      String authorEmail, MangakaProductionTaskCreateRequest request) {
    PageRecord p = pageRequiredMemory(seriesId, chapterId, pageId, authorEmail);

    if (regionId != null && !regionId.isEmpty()) {
      // Region-level task
      RegionRecord rr = p.regions.get(regionId);
      if (rr == null) throw new IllegalArgumentException("Region not found");
      rr.task = new TaskRecord(
          id(), pageId, regionId,
          request.getAssistantEmail(),
          request.getInstructions(),
          request.getDeadline(),
          MangakaTaskStatus.Pending);
      p.status = MangakaPageStatus.IN_TASK;
      return rr.task.toDto(p.fileName);
    } else {
      // Page-level task (fallback for backward compat)
      p.task = new TaskRecord(
          id(), pageId, null,
          request.getAssistantEmail(),
          request.getInstructions(),
          request.getDeadline(),
          MangakaTaskStatus.Pending);
      p.status = MangakaPageStatus.IN_TASK;
      return p.task.toDto(p.fileName);
    }
  }

  private List<AssistantTaskDto> listAssistantTasksMemory(String assistantEmail) {
    List<AssistantTaskDto> out = new ArrayList<AssistantTaskDto>();
    for (ChapterRecord ch : chapters.values()) {
      SeriesRecord sr = proposals.getSeriesRecord(ch.proposalId);
      String seriesTitle = sr != null ? sr.title : "";
      for (PageRecord p : ch.pages.values()) {
        // Page-level tasks
        if (p.task != null && p.task.assistantEmail.equalsIgnoreCase(assistantEmail))
          out.add(p.task.toAssistantDto(p.id, ch.proposalId, seriesTitle, ch.id, ch.title, p.pageNumber, p.fileName));
        // Region-level tasks
        for (RegionRecord rr : p.regions.values()) {
          if (rr.task != null && rr.task.assistantEmail.equalsIgnoreCase(assistantEmail))
            out.add(rr.task.toAssistantDto(p.id, ch.proposalId, seriesTitle, ch.id, ch.title, p.pageNumber, p.fileName));
        }
      }
    }
    return out;
  }

  private AssistantTaskDto buildAssistantDto(TaskRecord t) {
    for (ChapterRecord ch : chapters.values()) {
      for (PageRecord p : ch.pages.values()) {
        if (p.task != null && p.task.id.equals(t.id))
          return t.toAssistantDto(p.id, ch.proposalId, null, ch.id, ch.title, p.pageNumber, p.fileName);
        for (RegionRecord rr : p.regions.values()) {
          if (rr.task != null && rr.task.id.equals(t.id))
            return t.toAssistantDto(p.id, ch.proposalId, null, ch.id, ch.title, p.pageNumber, p.fileName);
        }
      }
    }
    return t.toAssistantDto(t.pageId, null, null, null, null, 0, null);
  }

  private String findTaskPageFileName(String taskId) {
    for (ChapterRecord ch : chapters.values()) {
      for (PageRecord p : ch.pages.values()) {
        if (p.task != null && p.task.id.equals(taskId)) return p.fileName;
        for (RegionRecord rr : p.regions.values()) {
          if (rr.task != null && rr.task.id.equals(taskId)) return p.fileName;
        }
      }
    }
    return null;
  }

  private AssistantTaskDto startAssistantTaskMemory(String taskId, String assistantEmail) {
    TaskRecord t = taskRequiredMemory(taskId, assistantEmail);
    if (!(t.status == MangakaTaskStatus.Pending || t.status == MangakaTaskStatus.RedoRequested))
      throw new IllegalArgumentException("Task cannot be started in current status");
    t.status = MangakaTaskStatus.InProgress;
    t.updatedAt = now();
    return buildAssistantDto(t);
  }

  private AssistantTaskDto submitAssistantTaskMemory(String taskId, String assistantEmail, String submittedFileName, String submissionNote) {
    TaskRecord t = taskRequiredMemory(taskId, assistantEmail);
    if (t.status != MangakaTaskStatus.InProgress)
      throw new IllegalArgumentException("Task cannot be submitted in current status");
    t.status = MangakaTaskStatus.Submitted;
    t.submittedFileName = submittedFileName;
    t.submissionNote = submissionNote;
    t.submittedAt = now();
    t.updatedAt = t.submittedAt;
    return buildAssistantDto(t);
  }

  private MangakaProductionTaskDto approveTaskMemory(String seriesId, String chapterId, String pageId, String taskId, String authorEmail) {
    ensureAllowedMemory(seriesId, authorEmail);
    TaskRecord t = taskRequiredMemory(taskId, null);
    if (t.status != MangakaTaskStatus.Submitted)
      throw new IllegalArgumentException("Task cannot be approved in current status");
    t.status = MangakaTaskStatus.Approved;
    t.updatedAt = now();
    return t.toDto(findTaskPageFileName(taskId));
  }

  private MangakaProductionTaskDto requestRedoTaskMemory(String seriesId, String chapterId, String pageId, String taskId, String authorEmail) {
    ensureAllowedMemory(seriesId, authorEmail);
    TaskRecord t = taskRequiredMemory(taskId, null);
    if (t.status == MangakaTaskStatus.Approved)
      throw new IllegalArgumentException("Approved task cannot be sent back for redo");
    t.status = MangakaTaskStatus.RedoRequested;
    t.updatedAt = now();
    return t.toDto(findTaskPageFileName(taskId));
  }

  private MangakaChapterDto completeChapterMemory(String seriesId, String chapterId, String authorEmail) {
    ChapterRecord c = chapterRequiredMemory(seriesId, chapterId, authorEmail);
    for (PageRecord p : c.pages.values()) {
      if (p.task != null && p.task.status != MangakaTaskStatus.Approved)
        throw new IllegalArgumentException("Page task is not yet approved");
      for (RegionRecord rr : p.regions.values()) {
        if (rr.task != null && rr.task.status != MangakaTaskStatus.Approved)
          throw new IllegalArgumentException("Region task is not yet approved");
      }
    }
    c.status = MangakaChapterStatus.COMPLETED;
    c.updatedAt = now();
    return c.toDto(loadPagesMemory(c.id));
  }

  private MangakaChapterDto completeChapterDb(String seriesId, String chapterId, String authorEmail) {
    return completeChapterMemory(seriesId, chapterId, authorEmail);
  }

  private ChapterRecord chapterRequiredMemory(String seriesId, String chapterId, String authorEmail) {
    ensureAllowedMemory(seriesId, authorEmail);
    ChapterRecord c = chapters.get(chapterId);
    if (c == null || !seriesId.equals(c.proposalId)) throw new IllegalArgumentException("Chapter not found");
    return c;
  }

  private PageRecord pageRequiredMemory(String seriesId, String chapterId, String pageId, String authorEmail) {
    ChapterRecord c = chapterRequiredMemory(seriesId, chapterId, authorEmail);
    PageRecord p = c.pages.get(pageId);
    if (p == null) throw new IllegalArgumentException("Page not found");
    return p;
  }

  private TaskRecord taskRequiredMemory(String taskId, String assistantEmail) {
    for (ChapterRecord ch : chapters.values()) {
      for (PageRecord p : ch.pages.values()) {
        if (p.task != null && p.task.id.equals(taskId)
            && (assistantEmail == null || p.task.assistantEmail.equalsIgnoreCase(assistantEmail)))
          return p.task;
        for (RegionRecord rr : p.regions.values()) {
          if (rr.task != null && rr.task.id.equals(taskId)
              && (assistantEmail == null || rr.task.assistantEmail.equalsIgnoreCase(assistantEmail)))
            return rr.task;
        }
      }
    }
    throw new IllegalArgumentException("Task not found");
  }

  private List<MangakaPageDto> loadPagesMemory(String chapterId) {
    ChapterRecord c = chapters.get(chapterId);
    List<MangakaPageDto> out = new ArrayList<MangakaPageDto>();
    if (c != null) {
      for (PageRecord p : c.pages.values()) {
        List<MangakaPageRegionDto> regionDtos = new ArrayList<MangakaPageRegionDto>();
        for (RegionRecord rr : p.regions.values()) regionDtos.add(rr.toDto());
        out.add(p.toDto(regionDtos.isEmpty() ? null : regionDtos));
      }
    }
    return out;
  }

  // ------------------------------------------------------------------
  // DB MODE
  // ------------------------------------------------------------------

  private void ensureAllowedDb(String seriesId, String authorEmail) {
    SeriesEntity series = seriesRepository.findById(Long.valueOf(seriesId))
        .orElseThrow(() -> new IllegalArgumentException("Series not found"));
    if (authorEmail == null) throw new IllegalArgumentException("authorEmail is required");
    String normalizedAuthor = normalize(authorEmail);
    if (series.getMangaka() == null || !normalizedAuthor.equals(normalize(series.getMangaka().getEmail())))
      throw new IllegalArgumentException("Series does not belong to this mangaka");
    if (!"ACTIVE".equals(series.getStatus())) throw new IllegalArgumentException("Series is not active");
  }

  private String normalize(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private List<MangakaChapterDto> listChaptersDb(String seriesId, String authorEmail) {
    return listChaptersMemory(seriesId, authorEmail);
  }

  private MangakaChapterDto createChapterDb(String seriesId, String authorEmail, MangakaChapterCreateRequest r) {
    ensureAllowedDb(seriesId, authorEmail);
    if (r == null || blank(r.getTitle()) || r.getChapterNumber() <= 0)
      throw new IllegalArgumentException("Chapter data is required");
    SeriesEntity series = seriesRepository.findById(Long.valueOf(seriesId))
        .orElseThrow(() -> new IllegalArgumentException("Series not found"));
    ChapterEntity chapter = new ChapterEntity();
    chapter.setSeries(series);
    chapter.setTitle(r.getTitle().trim());
    chapter.setChapterNumber(r.getChapterNumber());
    chapter.setStatus(MangakaChapterStatus.DRAFT.name());
    chapter.setCreatedAt(LocalDateTime.now());
    chapter.setUpdatedAt(LocalDateTime.now());
    ChapterEntity saved = chapterRepository.save(chapter);
    return new MangakaChapterDto(
        String.valueOf(saved.getId()), seriesId, saved.getTitle(), saved.getChapterNumber(),
        MangakaChapterStatus.valueOf(saved.getStatus()),
        saved.getCreatedAt().format(f), saved.getUpdatedAt().format(f), new ArrayList<MangakaPageDto>());
  }

  private MangakaPageDto addPageDb(String seriesId, String chapterId, String authorEmail, MangakaPageCreateRequest r) {
    ensureAllowedDb(seriesId, authorEmail);
    if (r == null || r.getPageNumber() <= 0 || blank(r.getFileName()))
      throw new IllegalArgumentException("Page data is required");
    ChapterEntity chapter = chapterRepository.findById(Long.parseLong(chapterId))
        .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
    PageEntity page = new PageEntity();
    page.setChapter(chapter);
    page.setPageNumber(r.getPageNumber());
    page.setManuscriptFilePath(r.getFileName().trim());
    page.setStatus(MangakaPageStatus.DRAFT.name());
    page.setCreatedAt(LocalDateTime.now());
    page.setUpdatedAt(LocalDateTime.now());
    PageEntity saved = pageRepository.save(page);
    chapter.setStatus(MangakaChapterStatus.IN_PROGRESS.name());
    chapter.setUpdatedAt(LocalDateTime.now());
    chapterRepository.save(chapter);
    return new MangakaPageDto(
        String.valueOf(saved.getId()), chapterId, saved.getPageNumber(),
        saved.getManuscriptFilePath(), MangakaPageStatus.valueOf(saved.getStatus()),
        saved.getCreatedAt().format(f));
  }

  private MangakaProductionTaskDto assignTaskDb(
      String seriesId, String chapterId, String pageId, String regionId,
      String authorEmail, MangakaProductionTaskCreateRequest request) {
    ensureAllowedDb(seriesId, authorEmail);
    PageEntity page = pageRepository.findById(Long.parseLong(pageId))
        .orElseThrow(() -> new IllegalArgumentException("Page not found"));
    UserEntity assistant = userRepository.findByEmailIgnoreCase(request.getAssistantEmail())
        .orElseThrow(() -> new IllegalArgumentException("Assistant not found"));

    TaskEntity task = new TaskEntity();
    task.setPage(page);
    task.setAssistant(assistant);
    task.setStatus(toTaskDbStatus(MangakaTaskStatus.Pending));
    task.setFeedbackNotes(request.getInstructions());
    if (request.getDeadline() != null && !request.getDeadline().isBlank())
      task.setDeadline(LocalDateTime.parse(request.getDeadline(), f));
    task.setCreatedAt(LocalDateTime.now());
    task.setUpdatedAt(LocalDateTime.now());

    TaskEntity saved = taskRepository.save(task);
    String pageFileName = page.getManuscriptFilePath();

    MangakaProductionTaskDto dto = new MangakaProductionTaskDto(
        String.valueOf(saved.getId()), pageId, assistant.getEmail(),
        saved.getFeedbackNotes(),
        saved.getDeadline() != null ? saved.getDeadline().format(f) : null,
        pageFileName, fromTaskDbStatus(saved.getStatus()),
        saved.getCreatedAt().format(f), saved.getUpdatedAt().format(f), null, null, null);
    return dto;
  }

  private List<AssistantTaskDto> listAssistantTasksDb(String assistantEmail) {
    if (blank(assistantEmail)) throw new IllegalArgumentException("Assistant email is required");
    List<TaskEntity> tasks = taskRepository.findByAssistant_EmailIgnoreCaseOrderByUpdatedAtDesc(assistantEmail);
    List<AssistantTaskDto> out = new ArrayList<AssistantTaskDto>();
    for (TaskEntity t : tasks) out.add(toAssistantTaskDto(t));
    return out;
  }

  private AssistantTaskDto startAssistantTaskDb(String taskId, String assistantEmail) {
    TaskEntity task = taskRepository.findById(Long.parseLong(taskId))
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    String dbStatus = task.getStatus();
    if (!"ASSIGNED".equals(dbStatus) && !"REVISION_REQUESTED".equals(dbStatus))
      throw new IllegalArgumentException("Task cannot be started in current status");
    task.setStatus(toTaskDbStatus(MangakaTaskStatus.InProgress));
    task.setUpdatedAt(LocalDateTime.now());
    TaskEntity saved = taskRepository.save(task);
    return toAssistantTaskDto(saved);
  }

  private AssistantTaskDto submitAssistantTaskDb(
      String taskId, String assistantEmail, String submittedFileName, String submissionNote) {
    TaskEntity task = taskRepository.findById(Long.parseLong(taskId))
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    if (!"IN_PROGRESS".equals(task.getStatus()))
      throw new IllegalArgumentException("Task cannot be submitted in current status");
    task.setStatus(toTaskDbStatus(MangakaTaskStatus.Submitted));
    task.setUpdatedAt(LocalDateTime.now());
    TaskEntity savedTask = taskRepository.save(task);
    SubmissionEntity submission = new SubmissionEntity();
    submission.setTask(savedTask);
    submission.setAssetFilePath(submittedFileName);
    submission.setSubmittedAt(LocalDateTime.now());
    submissionRepository.save(submission);
    return toAssistantTaskDto(savedTask);
  }

  private MangakaProductionTaskDto approveTaskDb(String seriesId, String chapterId, String pageId, String taskId, String authorEmail) {
    ensureAllowedDb(seriesId, authorEmail);
    TaskEntity task = taskRepository.findById(Long.parseLong(taskId))
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    if (!"PENDING_REVIEW".equals(task.getStatus()))
      throw new IllegalArgumentException("Task cannot be approved in current status");
    task.setStatus(toTaskDbStatus(MangakaTaskStatus.Approved));
    task.setUpdatedAt(LocalDateTime.now());
    TaskEntity saved = taskRepository.save(task);
    String pageFileName = task.getPage() != null ? task.getPage().getManuscriptFilePath() : null;
    MangakaProductionTaskDto dto = new MangakaProductionTaskDto(
        String.valueOf(saved.getId()), pageId, saved.getAssistant().getEmail(),
        saved.getFeedbackNotes(),
        saved.getDeadline() != null ? saved.getDeadline().format(f) : null,
        pageFileName, fromTaskDbStatus(saved.getStatus()),
        saved.getCreatedAt().format(f), saved.getUpdatedAt().format(f), null, null, null);
    return dto;
  }

  private MangakaProductionTaskDto requestRedoTaskDb(String seriesId, String chapterId, String pageId, String taskId, String authorEmail) {
    ensureAllowedDb(seriesId, authorEmail);
    TaskEntity task = taskRepository.findById(Long.parseLong(taskId))
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    if ("APPROVED".equals(task.getStatus()))
      throw new IllegalArgumentException("Approved task cannot be sent back for redo");
    task.setStatus(toTaskDbStatus(MangakaTaskStatus.RedoRequested));
    task.setUpdatedAt(LocalDateTime.now());
    TaskEntity saved = taskRepository.save(task);
    String pageFileName = task.getPage() != null ? task.getPage().getManuscriptFilePath() : null;
    MangakaProductionTaskDto dto = new MangakaProductionTaskDto(
        String.valueOf(saved.getId()), pageId, saved.getAssistant().getEmail(),
        saved.getFeedbackNotes(),
        saved.getDeadline() != null ? saved.getDeadline().format(f) : null,
        pageFileName, fromTaskDbStatus(saved.getStatus()),
        saved.getCreatedAt().format(f), saved.getUpdatedAt().format(f), null, null, null);
    return dto;
  }

  private AssistantTaskDto toAssistantTaskDto(TaskEntity t) {
    String pageId = t.getPage() != null ? String.valueOf(t.getPage().getId()) : null;
    String chapterId = null, chapterTitle = null, seriesId = null, seriesTitle = null;
    int pageNumber = 0; String pageFileName = null;
    if (t.getPage() != null) {
      pageNumber = t.getPage().getPageNumber();
      pageFileName = t.getPage().getManuscriptFilePath();
      if (t.getPage().getChapter() != null) {
        chapterId = String.valueOf(t.getPage().getChapter().getId());
        chapterTitle = t.getPage().getChapter().getTitle();
        if (t.getPage().getChapter().getSeries() != null) {
          seriesId = String.valueOf(t.getPage().getChapter().getSeries().getId());
          seriesTitle = t.getPage().getChapter().getSeries().getTitle();
        }
      }
    }
    AssistantTaskDto dto = new AssistantTaskDto(
        String.valueOf(t.getId()), seriesId, seriesTitle, chapterId, chapterTitle,
        pageId, pageNumber, pageFileName,
        t.getAssistant() != null ? t.getAssistant().getEmail() : null,
        t.getFeedbackNotes(),
        t.getDeadline() != null ? t.getDeadline().format(f) : null,
        fromTaskDbStatus(t.getStatus()), null, null,
        t.getCreatedAt() != null ? t.getCreatedAt().format(f) : null,
        t.getUpdatedAt() != null ? t.getUpdatedAt().format(f) : null, null);
    return dto;
  }

  // ------------------------------------------------------------------
  // Inner records
  // ------------------------------------------------------------------

  private static class ChapterRecord {
    String id, proposalId, title, createdAt, updatedAt;
    int chapterNumber;
    MangakaChapterStatus status;
    Map<String, PageRecord> pages = new LinkedHashMap<String, PageRecord>();

    ChapterRecord(String id, String proposalId, String title, int chapterNumber, MangakaChapterStatus status) {
      this.id = id; this.proposalId = proposalId; this.title = title;
      this.chapterNumber = chapterNumber; this.status = status;
      this.createdAt = this.updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    MangakaChapterDto toDto(List<MangakaPageDto> pages) {
      return new MangakaChapterDto(id, proposalId, title, chapterNumber, status, createdAt, updatedAt, pages);
    }
  }

  private static class PageRecord {
    String id, chapterId, fileName, uploadedAt;
    int pageNumber;
    MangakaPageStatus status;
    TaskRecord task; // page-level task (fallback)
    Map<String, RegionRecord> regions = new LinkedHashMap<String, RegionRecord>();

    PageRecord(String id, String chapterId, int pageNumber, String fileName, MangakaPageStatus status) {
      this.id = id; this.chapterId = chapterId; this.pageNumber = pageNumber;
      this.fileName = fileName; this.status = status;
      this.uploadedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    MangakaPageDto toDto(List<MangakaPageRegionDto> regionDtos) {
      return new MangakaPageDto(id, chapterId, pageNumber, fileName, status, uploadedAt, regionDtos);
    }
  }

  private static class RegionRecord {
    String id, pageId, regionType, note;
    double x, y, widthPct, heightPct;
    TaskRecord task;

    RegionRecord(String id, String pageId, String regionType, double x, double y, double widthPct, double heightPct, String note) {
      this.id = id; this.pageId = pageId; this.regionType = regionType;
      this.x = x; this.y = y; this.widthPct = widthPct; this.heightPct = heightPct; this.note = note;
    }

    MangakaPageRegionDto toDto() {
      List<MangakaProductionTaskDto> taskDtos = null;
      if (task != null) {
        taskDtos = new ArrayList<MangakaProductionTaskDto>();
        taskDtos.add(task.toDto(null));
      }
      return new MangakaPageRegionDto(id, pageId, regionType, x, y, widthPct, heightPct, note, taskDtos);
    }
  }

  private static class TaskRecord {
    String id, pageId, regionId, assistantEmail, instructions, deadline,
           createdAt, updatedAt, submittedFileName, submissionNote, submittedAt;
    MangakaTaskStatus status;

    TaskRecord(String id, String pageId, String regionId, String assistantEmail,
               String instructions, String deadline, MangakaTaskStatus status) {
      this.id = id; this.pageId = pageId; this.regionId = regionId;
      this.assistantEmail = assistantEmail; this.instructions = instructions;
      this.deadline = deadline; this.status = status;
      this.createdAt = this.updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    MangakaProductionTaskDto toDto(String pageFileName) {
      return new MangakaProductionTaskDto(
          id, pageId, assistantEmail, instructions, deadline, pageFileName,
          status, createdAt, updatedAt, submittedFileName, submissionNote, submittedAt);
    }

    AssistantTaskDto toAssistantDto(String pageId, String seriesId, String seriesTitle,
                                    String chapterId, String chapterTitle, int pageNumber, String pageFileName) {
      AssistantTaskDto dto = new AssistantTaskDto(
          id, seriesId, seriesTitle, chapterId, chapterTitle, pageId, pageNumber,
          pageFileName, assistantEmail, instructions, deadline, status,
          submittedFileName, submissionNote, createdAt, updatedAt, submittedAt);
      dto.setRegionId(regionId);
      return dto;
    }
  }
}