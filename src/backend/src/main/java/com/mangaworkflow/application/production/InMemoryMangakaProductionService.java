package com.mangaworkflow.application.production;

import com.mangaworkflow.domain.task.AssistantTaskDto;
import com.mangaworkflow.domain.production.*;
import com.mangaworkflow.domain.proposal.MangaProposalDto;
import com.mangaworkflow.domain.proposal.MangaProposalStatus;
import com.mangaworkflow.application.proposal.InMemoryMangaProposalService;
import com.mangaworkflow.persistence.entity.*;
import com.mangaworkflow.persistence.repository.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class InMemoryMangakaProductionService {
  private final InMemoryMangaProposalService proposals;
  private final MangakaChapterRepository chapterRepository;
  private final MangakaPageRepository pageRepository;
  private final MangakaPageRegionRepository regionRepository;
  private final MangakaProductionTaskRepository taskRepository;
  private final Map<String, ChapterRecord> chapters = new LinkedHashMap<String, ChapterRecord>();
  private final AtomicLong seq = new AtomicLong(500);
  private final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  public InMemoryMangakaProductionService(InMemoryMangaProposalService proposals) {
    this(proposals, null, null, null, null);
  }

  @Autowired
  public InMemoryMangakaProductionService(
      InMemoryMangaProposalService proposals,
      @Nullable MangakaChapterRepository chapterRepository,
      @Nullable MangakaPageRepository pageRepository,
      @Nullable MangakaPageRegionRepository regionRepository,
      @Nullable MangakaProductionTaskRepository taskRepository) {
    this.proposals = proposals;
    this.chapterRepository = chapterRepository;
    this.pageRepository = pageRepository;
    this.regionRepository = regionRepository;
    this.taskRepository = taskRepository;
    seedAssistantSample();
  }

  public synchronized List<MangakaChapterDto> listChapters(String proposalId, String authorEmail) {
    if (dbMode()) return listChaptersDb(proposalId, authorEmail);
    ensureAllowed(proposalId, authorEmail);
    List<MangakaChapterDto> out = new ArrayList<MangakaChapterDto>();
    for (ChapterRecord c : chapters.values())
      if (c.proposalId.equals(proposalId)) out.add(c.toDto(loadPagesMemory(c.id)));
    return out;
  }

  public synchronized MangakaChapterDto createChapter(
      String proposalId, String authorEmail, MangakaChapterCreateRequest r) {
    if (dbMode()) return createChapterDb(proposalId, authorEmail, r);
    ensureAllowed(proposalId, authorEmail);
    if (r == null || blank(r.getTitle()) || r.getChapterNumber() <= 0)
      throw new IllegalArgumentException("Chapter data is required");
    ChapterRecord c =
        new ChapterRecord(
            id(),
            proposalId,
            r.getTitle().trim(),
            r.getChapterNumber(),
            MangakaChapterStatus.Draft);
    chapters.put(c.id, c);
    return c.toDto(new ArrayList<MangakaPageDto>());
  }

  public synchronized MangakaPageDto addPage(
      String proposalId, String chapterId, String authorEmail, MangakaPageCreateRequest r) {
    if (dbMode()) return addPageDb(proposalId, chapterId, authorEmail, r);
    ChapterRecord c = chapterRequired(proposalId, chapterId, authorEmail);
    if (r == null || r.getPageNumber() <= 0 || blank(r.getFileName()))
      throw new IllegalArgumentException("Page data is required");
    PageRecord p = new PageRecord(id(), c.id, r.getPageNumber(), r.getFileName().trim());
    c.pages.put(p.id, p);
    c.status = MangakaChapterStatus.PagesUploaded;
    c.updatedAt = now();
    return p.toDto(new ArrayList<MangakaPageRegionDto>());
  }

  public synchronized MangakaPageRegionDto addRegion(
      String proposalId,
      String chapterId,
      String pageId,
      String authorEmail,
      MangakaPageRegionCreateRequest r) {
    if (dbMode()) return addRegionDb(proposalId, chapterId, pageId, authorEmail, r);
    PageRecord p = pageRequired(proposalId, chapterId, pageId, authorEmail);
    if (r == null
        || blank(r.getRegionType())
        || r.getX() < 0
        || r.getY() < 0
        || r.getWidthPct() <= 0
        || r.getHeightPct() <= 0
        || r.getX() > 100
        || r.getY() > 100
        || r.getWidthPct() > 100
        || r.getHeightPct() > 100
        || r.getX() + r.getWidthPct() > 100
        || r.getY() + r.getHeightPct() > 100)
      throw new IllegalArgumentException("Invalid region bounds");
    RegionRecord g =
        new RegionRecord(
            id(),
            p.id,
            r.getRegionType().trim(),
            r.getX(),
            r.getY(),
            r.getWidthPct(),
            r.getHeightPct(),
            r.getNote());
    p.regions.put(g.id, g);
    p.status = MangakaPageStatus.Segmented;
    return g.toDto(new ArrayList<MangakaProductionTaskDto>());
  }

  public synchronized MangakaProductionTaskDto assignTask(
      String proposalId,
      String chapterId,
      String pageId,
      String regionId,
      String authorEmail,
      MangakaProductionTaskCreateRequest r) {
    if (dbMode()) return assignTaskDb(proposalId, chapterId, pageId, regionId, authorEmail, r);
    RegionRecord g = regionRequired(proposalId, chapterId, pageId, regionId, authorEmail);
    if (r == null || blank(r.getAssistantEmail()) || blank(r.getTaskType()))
      throw new IllegalArgumentException("Task data is required");
    TaskRecord t =
        new TaskRecord(
            id(),
            g.id,
            r.getAssistantEmail().trim(),
            r.getTaskType().trim(),
            r.getInstructions(),
            r.getReferenceFileName());
    g.tasks.put(t.id, t);
    return t.toDto();
  }

  public synchronized List<AssistantTaskDto> listAssistantTasks(String assistantEmail) {
    if (dbMode()) return listAssistantTasksDb(assistantEmail);
    List<AssistantTaskDto> out = new ArrayList<AssistantTaskDto>();
    for (ChapterRecord c : chapters.values())
      for (PageRecord p : c.pages.values())
        for (RegionRecord g : p.regions.values())
          for (TaskRecord t : g.tasks.values())
            if (t.assistantEmail.equalsIgnoreCase(assistantEmail))
              out.add(t.toAssistantDto(c, p, g));
    return out;
  }

  public synchronized AssistantTaskDto startAssistantTask(String taskId, String assistantEmail) {
    if (dbMode()) return startAssistantTaskDb(taskId, assistantEmail);
    TaskRecord t = taskRequired(taskId);
    ensureAssistant(t, assistantEmail);
    if (!(t.status == MangakaTaskStatus.Pending || t.status == MangakaTaskStatus.RedoRequested))
      throw new IllegalArgumentException("Task cannot be started in current status");
    t.status = MangakaTaskStatus.InProgress;
    t.updatedAt = now();
    return memoryTaskContext(t);
  }

  public synchronized AssistantTaskDto submitAssistantTask(
      String taskId, String assistantEmail, String submittedFileName, String submissionNote) {
    if (dbMode())
      return submitAssistantTaskDb(taskId, assistantEmail, submittedFileName, submissionNote);
    TaskRecord t = taskRequired(taskId);
    ensureAssistant(t, assistantEmail);
    if (blank(submittedFileName))
      throw new IllegalArgumentException("Submitted file name is required");
    if (!(t.status == MangakaTaskStatus.Pending
        || t.status == MangakaTaskStatus.InProgress
        || t.status == MangakaTaskStatus.RedoRequested))
      throw new IllegalArgumentException("Task cannot be submitted in current status");
    t.status = MangakaTaskStatus.Submitted;
    t.submittedFileName = submittedFileName.trim();
    t.submissionNote = submissionNote;
    t.submittedAt = now();
    t.updatedAt = t.submittedAt;
    return memoryTaskContext(t);
  }

  public synchronized MangakaProductionTaskDto approveTask(
      String proposalId,
      String chapterId,
      String pageId,
      String regionId,
      String taskId,
      String authorEmail) {
    if (dbMode())
      return approveTaskDb(proposalId, chapterId, pageId, regionId, taskId, authorEmail);
    ensureAllowed(proposalId, authorEmail);
    RegionRecord g = regionRequired(proposalId, chapterId, pageId, regionId, authorEmail);
    if (!g.tasks.containsKey(taskId)) throw new IllegalArgumentException("Task not found");
    TaskRecord t = g.tasks.get(taskId);
    if (t.status != MangakaTaskStatus.Submitted)
      throw new IllegalArgumentException("Task can only be approved after submission");
    t.status = MangakaTaskStatus.Approved;
    t.updatedAt = now();
    return t.toDto();
  }

  public synchronized MangakaProductionTaskDto requestRedoTask(
      String proposalId,
      String chapterId,
      String pageId,
      String regionId,
      String taskId,
      String authorEmail) {
    if (dbMode())
      return requestRedoTaskDb(proposalId, chapterId, pageId, regionId, taskId, authorEmail);
    ensureAllowed(proposalId, authorEmail);
    RegionRecord g = regionRequired(proposalId, chapterId, pageId, regionId, authorEmail);
    if (!g.tasks.containsKey(taskId)) throw new IllegalArgumentException("Task not found");
    TaskRecord t = g.tasks.get(taskId);
    if (t.status != MangakaTaskStatus.Submitted)
      throw new IllegalArgumentException("Task can only be redone after submission");
    t.status = MangakaTaskStatus.RedoRequested;
    t.updatedAt = now();
    return t.toDto();
  }

  private boolean dbMode() {
    return chapterRepository != null
        && pageRepository != null
        && regionRepository != null
        && taskRepository != null;
  }

  private List<MangakaChapterDto> listChaptersDb(String proposalId, String authorEmail) {
    ensureAllowed(proposalId, authorEmail);
    List<MangakaChapterDto> out = new ArrayList<MangakaChapterDto>();
    Long pid = Long.valueOf(proposalId);
    for (MangakaChapterEntity c : chapterRepository.findByProposalId(pid)) out.add(toChapterDto(c));
    return out;
  }

  private MangakaChapterDto createChapterDb(
      String proposalId, String authorEmail, MangakaChapterCreateRequest r) {
    ensureAllowed(proposalId, authorEmail);
    if (r == null || blank(r.getTitle()) || r.getChapterNumber() <= 0)
      throw new IllegalArgumentException("Chapter data is required");
    MangakaChapterEntity e = new MangakaChapterEntity();
    e.setId(nextId(chapterRepository));
    e.setProposalId(Long.valueOf(proposalId));
    e.setTitle(r.getTitle().trim());
    e.setChapterNumber(r.getChapterNumber());
    e.setStatus(MangakaChapterStatus.Draft.name());
    e.setCreatedAt(now());
    e.setUpdatedAt(now());
    return toChapterDto(chapterRepository.save(e));
  }

  private MangakaPageDto addPageDb(
      String proposalId, String chapterId, String authorEmail, MangakaPageCreateRequest r) {
    ensureAllowed(proposalId, authorEmail);
    MangakaChapterEntity c =
        chapterRepository
            .findById(Long.valueOf(chapterId))
            .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
    if (r == null || r.getPageNumber() <= 0 || blank(r.getFileName()))
      throw new IllegalArgumentException("Page data is required");
    MangakaPageEntity p = new MangakaPageEntity();
    p.setId(nextId(pageRepository));
    p.setChapterId(c.getId());
    p.setPageNumber(r.getPageNumber());
    p.setFileName(r.getFileName().trim());
    p.setStatus(MangakaPageStatus.Uploaded.name());
    p.setUploadedAt(now());
    return toPageDto(pageRepository.save(p));
  }

  private MangakaPageRegionDto addRegionDb(
      String proposalId,
      String chapterId,
      String pageId,
      String authorEmail,
      MangakaPageRegionCreateRequest r) {
    ensureAllowed(proposalId, authorEmail);
    if (r == null
        || blank(r.getRegionType())
        || r.getX() < 0
        || r.getY() < 0
        || r.getWidthPct() <= 0
        || r.getHeightPct() <= 0
        || r.getX() + r.getWidthPct() > 100
        || r.getY() + r.getHeightPct() > 100)
      throw new IllegalArgumentException("Invalid region bounds");
    MangakaPageEntity p =
        pageRepository
            .findById(Long.valueOf(pageId))
            .orElseThrow(() -> new IllegalArgumentException("Page not found"));
    MangakaPageRegionEntity g = new MangakaPageRegionEntity();
    g.setId(nextId(regionRepository));
    g.setPageId(p.getId());
    g.setRegionType(r.getRegionType().trim());
    g.setX(r.getX());
    g.setY(r.getY());
    g.setWidthPct(r.getWidthPct());
    g.setHeightPct(r.getHeightPct());
    g.setNote(r.getNote());
    p.setStatus(MangakaPageStatus.Segmented.name());
    pageRepository.save(p);
    return toRegionDto(regionRepository.save(g));
  }

  private MangakaProductionTaskDto assignTaskDb(
      String proposalId,
      String chapterId,
      String pageId,
      String regionId,
      String authorEmail,
      MangakaProductionTaskCreateRequest r) {
    ensureAllowed(proposalId, authorEmail);
    if (r == null || blank(r.getAssistantEmail()) || blank(r.getTaskType()))
      throw new IllegalArgumentException("Task data is required");
    MangakaPageRegionEntity g =
        regionRepository
            .findById(Long.valueOf(regionId))
            .orElseThrow(() -> new IllegalArgumentException("Region not found"));
    MangakaProductionTaskEntity t = new MangakaProductionTaskEntity();
    t.setId(nextId(taskRepository));
    t.setRegionId(g.getId());
    t.setAssistantEmail(r.getAssistantEmail().trim());
    t.setTaskType(r.getTaskType().trim());
    t.setInstructions(r.getInstructions());
    t.setReferenceFileName(r.getReferenceFileName());
    t.setStatus(MangakaTaskStatus.Pending.name());
    t.setCreatedAt(now());
    t.setUpdatedAt(now());
    MangakaChapterEntity c =
        chapterRepository
            .findById(Long.valueOf(chapterId))
            .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
    MangakaPageEntity p =
        pageRepository
            .findById(Long.valueOf(pageId))
            .orElseThrow(() -> new IllegalArgumentException("Page not found"));
    c.setStatus(MangakaChapterStatus.TasksInProgress.name());
    p.setStatus(MangakaPageStatus.TasksAssigned.name());
    chapterRepository.save(c);
    pageRepository.save(p);
    return toTaskDto(taskRepository.save(t));
  }

  private List<AssistantTaskDto> listAssistantTasksDb(String assistantEmail) {
    List<AssistantTaskDto> out = new ArrayList<AssistantTaskDto>();
    for (MangakaProductionTaskEntity t :
        taskRepository.findByAssistantEmailIgnoreCase(assistantEmail))
      out.add(toAssistantTaskDto(t));
    return out;
  }

  private AssistantTaskDto startAssistantTaskDb(String taskId, String assistantEmail) {
    MangakaProductionTaskEntity t =
        taskRepository
            .findById(Long.valueOf(taskId))
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    ensureAssistantDb(t, assistantEmail);
    if (!(MangakaTaskStatus.Pending.name().equals(t.getStatus())
        || MangakaTaskStatus.RedoRequested.name().equals(t.getStatus())))
      throw new IllegalArgumentException("Task cannot be started in current status");
    t.setStatus(MangakaTaskStatus.InProgress.name());
    t.setUpdatedAt(now());
    return toAssistantTaskDto(taskRepository.save(t));
  }

  private AssistantTaskDto submitAssistantTaskDb(
      String taskId, String assistantEmail, String submittedFileName, String submissionNote) {
    MangakaProductionTaskEntity t =
        taskRepository
            .findById(Long.valueOf(taskId))
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    ensureAssistantDb(t, assistantEmail);
    if (blank(submittedFileName))
      throw new IllegalArgumentException("Submitted file name is required");
    if (!(MangakaTaskStatus.Pending.name().equals(t.getStatus())
        || MangakaTaskStatus.InProgress.name().equals(t.getStatus())
        || MangakaTaskStatus.RedoRequested.name().equals(t.getStatus())))
      throw new IllegalArgumentException("Task cannot be submitted in current status");
    t.setStatus(MangakaTaskStatus.Submitted.name());
    t.setSubmittedFileName(submittedFileName.trim());
    t.setSubmissionNote(submissionNote);
    t.setSubmittedAt(now());
    t.setUpdatedAt(t.getSubmittedAt());
    return toAssistantTaskDto(taskRepository.save(t));
  }

  private MangakaProductionTaskDto approveTaskDb(
      String proposalId,
      String chapterId,
      String pageId,
      String regionId,
      String taskId,
      String authorEmail) {
    ensureAllowed(proposalId, authorEmail);
    ensureTaskPathDb(proposalId, chapterId, pageId, regionId);
    MangakaProductionTaskEntity t =
        taskRepository
            .findById(Long.valueOf(taskId))
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    if (!String.valueOf(t.getRegionId()).equals(regionId))
      throw new IllegalArgumentException("Task not found");
    if (!MangakaTaskStatus.Submitted.name().equals(t.getStatus()))
      throw new IllegalArgumentException("Task can only be approved after submission");
    t.setStatus(MangakaTaskStatus.Approved.name());
    t.setUpdatedAt(now());
    return toTaskDto(taskRepository.save(t));
  }

  private MangakaProductionTaskDto requestRedoTaskDb(
      String proposalId,
      String chapterId,
      String pageId,
      String regionId,
      String taskId,
      String authorEmail) {
    ensureAllowed(proposalId, authorEmail);
    ensureTaskPathDb(proposalId, chapterId, pageId, regionId);
    MangakaProductionTaskEntity t =
        taskRepository
            .findById(Long.valueOf(taskId))
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    if (!String.valueOf(t.getRegionId()).equals(regionId))
      throw new IllegalArgumentException("Task not found");
    if (!MangakaTaskStatus.Submitted.name().equals(t.getStatus()))
      throw new IllegalArgumentException("Task can only be redone after submission");
    t.setStatus(MangakaTaskStatus.RedoRequested.name());
    t.setUpdatedAt(now());
    return toTaskDto(taskRepository.save(t));
  }

  private void ensureTaskPathDb(
      String proposalId, String chapterId, String pageId, String regionId) {
    MangakaChapterEntity c =
        chapterRepository
            .findById(Long.valueOf(chapterId))
            .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
    if (!String.valueOf(c.getProposalId()).equals(proposalId))
      throw new IllegalArgumentException("Chapter not found");
    MangakaPageEntity p =
        pageRepository
            .findById(Long.valueOf(pageId))
            .orElseThrow(() -> new IllegalArgumentException("Page not found"));
    if (!String.valueOf(p.getChapterId()).equals(chapterId))
      throw new IllegalArgumentException("Page not found");
    MangakaPageRegionEntity g =
        regionRepository
            .findById(Long.valueOf(regionId))
            .orElseThrow(() -> new IllegalArgumentException("Region not found"));
    if (!String.valueOf(g.getPageId()).equals(pageId))
      throw new IllegalArgumentException("Region not found");
  }

  private void ensureAllowed(String proposalId, String authorEmail) {
    MangaProposalDto p = proposals.getById(proposalId);
    if (p == null) throw new IllegalArgumentException("Proposal not found");
    if (authorEmail == null || !p.getAuthorEmail().equalsIgnoreCase(authorEmail))
      throw new IllegalArgumentException("Proposal does not belong to author");
    if (!(p.getStatus() == MangaProposalStatus.Approved
        || p.getStatus() == MangaProposalStatus.Serializing))
      throw new IllegalArgumentException(
          "Production is only allowed for approved or serializing proposals");
  }

  private void ensureAssistantDb(MangakaProductionTaskEntity t, String assistantEmail) {
    if (assistantEmail == null || !t.getAssistantEmail().equalsIgnoreCase(assistantEmail))
      throw new IllegalArgumentException("Task does not belong to assistant");
  }

  private ChapterRecord chapterRequired(String proposalId, String chapterId, String authorEmail) {
    ensureAllowed(proposalId, authorEmail);
    ChapterRecord c = chapters.get(chapterId);
    if (c == null || !c.proposalId.equals(proposalId))
      throw new IllegalArgumentException("Chapter not found");
    return c;
  }

  private PageRecord pageRequired(
      String proposalId, String chapterId, String pageId, String authorEmail) {
    ChapterRecord c = chapterRequired(proposalId, chapterId, authorEmail);
    PageRecord p = c.pages.get(pageId);
    if (p == null) throw new IllegalArgumentException("Page not found");
    return p;
  }

  private RegionRecord regionRequired(
      String proposalId, String chapterId, String pageId, String regionId, String authorEmail) {
    PageRecord p = pageRequired(proposalId, chapterId, pageId, authorEmail);
    RegionRecord g = p.regions.get(regionId);
    if (g == null) throw new IllegalArgumentException("Region not found");
    return g;
  }

  private ChapterRecord chapterById(String id) {
    return chapters.get(id);
  }

  private PageRecord pageById(String id) {
    for (ChapterRecord c : chapters.values()) if (c.pages.containsKey(id)) return c.pages.get(id);
    return null;
  }

  private RegionRecord regionById(String regionId) {
    for (ChapterRecord c : chapters.values())
      for (PageRecord p : c.pages.values())
        if (p.regions.containsKey(regionId)) return p.regions.get(regionId);
    return null;
  }

  private TaskRecord taskRequired(String taskId) {
    for (ChapterRecord c : chapters.values())
      for (PageRecord p : c.pages.values())
        for (RegionRecord g : p.regions.values())
          if (g.tasks.containsKey(taskId)) return g.tasks.get(taskId);
    throw new IllegalArgumentException("Task not found");
  }

  private void ensureAssistant(TaskRecord t, String assistantEmail) {
    if (assistantEmail == null || !t.assistantEmail.equalsIgnoreCase(assistantEmail))
      throw new IllegalArgumentException("Task does not belong to assistant");
  }

  private void seedAssistantSample() {
    if (!dbMode()) {
      if (chapters.containsKey("600")) return;
      ChapterRecord c =
          new ChapterRecord(
              "600", "4", "Seed Assistant Chapter", 1, MangakaChapterStatus.TasksInProgress);
      PageRecord p = new PageRecord("601", c.id, 1, "seed-page-1.png");
      p.status = MangakaPageStatus.TasksAssigned;
      RegionRecord g =
          new RegionRecord("602", p.id, "SpeechBubble", 10, 10, 30, 20, "Seed assistant task");
      TaskRecord t =
          new TaskRecord(
              "603",
              g.id,
              "assistant@manga.local",
              "Translate",
              "Seed instructions",
              "seed-ref.png");
      g.tasks.put(t.id, t);
      p.regions.put(g.id, g);
      c.pages.put(p.id, p);
      chapters.put(c.id, c);
      return;
    }
    if (chapterRepository.count() > 0 || !repositoryHasProposal4()) return;
    MangakaChapterEntity c = new MangakaChapterEntity();
    c.setId(600L);
    c.setProposalId(4L);
    c.setTitle("Seed Assistant Chapter");
    c.setChapterNumber(1);
    c.setStatus(MangakaChapterStatus.TasksInProgress.name());
    c.setCreatedAt(now());
    c.setUpdatedAt(now());
    chapterRepository.save(c);
    MangakaPageEntity p = new MangakaPageEntity();
    p.setId(601L);
    p.setChapterId(600L);
    p.setPageNumber(1);
    p.setFileName("seed-page-1.png");
    p.setStatus(MangakaPageStatus.TasksAssigned.name());
    p.setUploadedAt(now());
    pageRepository.save(p);
    MangakaPageRegionEntity g = new MangakaPageRegionEntity();
    g.setId(602L);
    g.setPageId(601L);
    g.setRegionType("SpeechBubble");
    g.setX(10);
    g.setY(10);
    g.setWidthPct(30);
    g.setHeightPct(20);
    g.setNote("Seed assistant task");
    regionRepository.save(g);
    MangakaProductionTaskEntity t = new MangakaProductionTaskEntity();
    t.setId(603L);
    t.setRegionId(602L);
    t.setAssistantEmail("assistant@manga.local");
    t.setTaskType("Translate");
    t.setInstructions("Seed instructions");
    t.setReferenceFileName("seed-ref.png");
    t.setStatus(MangakaTaskStatus.Pending.name());
    t.setCreatedAt(now());
    t.setUpdatedAt(now());
    taskRepository.save(t);
  }

  private boolean repositoryHasProposal4() {
    return proposals.getById("4") != null;
  }

  private String id() {
    return String.valueOf(seq.incrementAndGet());
  }

  private String now() {
    return LocalDateTime.now().format(f);
  }

  private boolean blank(String v) {
    return v == null || v.trim().isEmpty();
  }

  private long nextId(org.springframework.data.jpa.repository.JpaRepository<?, Long> repo) {
    long max = 0L;
    for (Object o : repo.findAll()) {
      if (o instanceof MangakaChapterEntity) {
        Long id = ((MangakaChapterEntity) o).getId();
        if (id != null && id.longValue() > max) max = id.longValue();
      } else if (o instanceof MangakaPageEntity) {
        Long id = ((MangakaPageEntity) o).getId();
        if (id != null && id.longValue() > max) max = id.longValue();
      } else if (o instanceof MangakaPageRegionEntity) {
        Long id = ((MangakaPageRegionEntity) o).getId();
        if (id != null && id.longValue() > max) max = id.longValue();
      } else if (o instanceof MangakaProductionTaskEntity) {
        Long id = ((MangakaProductionTaskEntity) o).getId();
        if (id != null && id.longValue() > max) max = id.longValue();
      }
    }
    return max + 1L;
  }

  private AssistantTaskDto memoryTaskContext(TaskRecord t) {
    RegionRecord g = regionById(t.regionId);
    PageRecord p = pageById(g.pageId);
    ChapterRecord c = chapterById(p.chapterId);
    return t.toAssistantDto(c, p, g);
  }

  private List<MangakaPageDto> loadPagesMemory(String chapterId) {
    List<MangakaPageDto> out = new ArrayList<MangakaPageDto>();
    ChapterRecord c = chapters.get(chapterId);
    if (c != null) for (PageRecord p : c.pages.values()) out.add(p.toDto(loadRegionsMemory(p.id)));
    return out;
  }

  private List<MangakaPageRegionDto> loadRegionsMemory(String pageId) {
    List<MangakaPageRegionDto> out = new ArrayList<MangakaPageRegionDto>();
    PageRecord p = pageById(pageId);
    if (p != null)
      for (RegionRecord g : p.regions.values()) out.add(g.toDto(loadTasksMemory(g.id)));
    return out;
  }

  private List<MangakaProductionTaskDto> loadTasksMemory(String regionId) {
    List<MangakaProductionTaskDto> out = new ArrayList<MangakaProductionTaskDto>();
    RegionRecord g = regionById(regionId);
    if (g != null) for (TaskRecord t : g.tasks.values()) out.add(t.toDto());
    return out;
  }

  private MangakaChapterDto toChapterDto(MangakaChapterEntity e) {
    return new MangakaChapterDto(
        String.valueOf(e.getId()),
        String.valueOf(e.getProposalId()),
        e.getTitle(),
        e.getChapterNumber(),
        MangakaChapterStatus.valueOf(e.getStatus()),
        e.getCreatedAt(),
        e.getUpdatedAt(),
        loadPagesDb(e.getId()));
  }

  private List<MangakaPageDto> loadPagesDb(Long chapterId) {
    List<MangakaPageDto> out = new ArrayList<MangakaPageDto>();
    for (MangakaPageEntity p : pageRepository.findByChapterId(chapterId)) out.add(toPageDto(p));
    return out;
  }

  private MangakaPageDto toPageDto(MangakaPageEntity e) {
    return new MangakaPageDto(
        String.valueOf(e.getId()),
        String.valueOf(e.getChapterId()),
        e.getPageNumber(),
        e.getFileName(),
        MangakaPageStatus.valueOf(e.getStatus()),
        e.getUploadedAt(),
        loadRegionsDb(e.getId()));
  }

  private List<MangakaPageRegionDto> loadRegionsDb(Long pageId) {
    List<MangakaPageRegionDto> out = new ArrayList<MangakaPageRegionDto>();
    for (MangakaPageRegionEntity g : regionRepository.findByPageId(pageId)) out.add(toRegionDto(g));
    return out;
  }

  private MangakaPageRegionDto toRegionDto(MangakaPageRegionEntity e) {
    return new MangakaPageRegionDto(
        String.valueOf(e.getId()),
        String.valueOf(e.getPageId()),
        e.getRegionType(),
        e.getX(),
        e.getY(),
        e.getWidthPct(),
        e.getHeightPct(),
        e.getNote(),
        loadTasksDb(e.getId()));
  }

  private List<MangakaProductionTaskDto> loadTasksDb(Long regionId) {
    List<MangakaProductionTaskDto> out = new ArrayList<MangakaProductionTaskDto>();
    for (MangakaProductionTaskEntity t : taskRepository.findByRegionId(regionId))
      out.add(toTaskDto(t));
    return out;
  }

  private MangakaProductionTaskDto toTaskDto(MangakaProductionTaskEntity e) {
    return new MangakaProductionTaskDto(
        String.valueOf(e.getId()),
        String.valueOf(e.getRegionId()),
        e.getAssistantEmail(),
        e.getTaskType(),
        e.getInstructions(),
        e.getReferenceFileName(),
        MangakaTaskStatus.valueOf(e.getStatus()),
        e.getCreatedAt(),
        e.getUpdatedAt(),
        e.getSubmittedFileName(),
        e.getSubmissionNote(),
        e.getSubmittedAt());
  }

  private AssistantTaskDto toAssistantTaskDto(MangakaProductionTaskEntity t) {
    MangakaPageRegionEntity g =
        regionRepository
            .findById(t.getRegionId())
            .orElseThrow(() -> new IllegalArgumentException("Region not found"));
    MangakaPageEntity p =
        pageRepository
            .findById(g.getPageId())
            .orElseThrow(() -> new IllegalArgumentException("Page not found"));
    MangakaChapterEntity c =
        chapterRepository
            .findById(p.getChapterId())
            .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
    return new AssistantTaskDto(
        String.valueOf(t.getId()),
        String.valueOf(c.getProposalId()),
        String.valueOf(c.getId()),
        c.getTitle(),
        String.valueOf(p.getId()),
        p.getPageNumber(),
        p.getFileName(),
        String.valueOf(g.getId()),
        g.getRegionType(),
        g.getNote(),
        t.getAssistantEmail(),
        t.getTaskType(),
        t.getInstructions(),
        t.getReferenceFileName(),
        MangakaTaskStatus.valueOf(t.getStatus()),
        t.getSubmittedFileName(),
        t.getSubmissionNote(),
        t.getCreatedAt(),
        t.getUpdatedAt(),
        t.getSubmittedAt());
  }

  private static class ChapterRecord {
    String id, proposalId, title;
    int chapterNumber;
    MangakaChapterStatus status;
    String createdAt, updatedAt;
    Map<String, PageRecord> pages = new LinkedHashMap<String, PageRecord>();

    ChapterRecord(
        String id,
        String proposalId,
        String title,
        int chapterNumber,
        MangakaChapterStatus status) {
      this.id = id;
      this.proposalId = proposalId;
      this.title = title;
      this.chapterNumber = chapterNumber;
      this.status = status;
      this.createdAt =
          this.updatedAt =
              LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    MangakaChapterDto toDto(List<MangakaPageDto> ps) {
      return new MangakaChapterDto(
          id, proposalId, title, chapterNumber, status, createdAt, updatedAt, ps);
    }
  }

  private static class PageRecord {
    String id, chapterId;
    int pageNumber;
    String fileName;
    MangakaPageStatus status = MangakaPageStatus.Uploaded;
    String uploadedAt;
    Map<String, RegionRecord> regions = new LinkedHashMap<String, RegionRecord>();

    PageRecord(String id, String chapterId, int pageNumber, String fileName) {
      this.id = id;
      this.chapterId = chapterId;
      this.pageNumber = pageNumber;
      this.fileName = fileName;
      this.uploadedAt =
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    MangakaPageDto toDto(List<MangakaPageRegionDto> rs) {
      return new MangakaPageDto(id, chapterId, pageNumber, fileName, status, uploadedAt, rs);
    }
  }

  private static class RegionRecord {
    String id, pageId, regionType, note;
    double x, y, widthPct, heightPct;
    Map<String, TaskRecord> tasks = new LinkedHashMap<String, TaskRecord>();

    RegionRecord(
        String id,
        String pageId,
        String regionType,
        double x,
        double y,
        double widthPct,
        double heightPct,
        String note) {
      this.id = id;
      this.pageId = pageId;
      this.regionType = regionType;
      this.x = x;
      this.y = y;
      this.widthPct = widthPct;
      this.heightPct = heightPct;
      this.note = note;
    }

    MangakaPageRegionDto toDto(List<MangakaProductionTaskDto> ts) {
      return new MangakaPageRegionDto(id, pageId, regionType, x, y, widthPct, heightPct, note, ts);
    }
  }

  private static class TaskRecord {
    String id,
        regionId,
        assistantEmail,
        taskType,
        instructions,
        referenceFileName,
        createdAt,
        updatedAt,
        submittedFileName,
        submissionNote,
        submittedAt;
    MangakaTaskStatus status = MangakaTaskStatus.Pending;

    TaskRecord(
        String id,
        String regionId,
        String assistantEmail,
        String taskType,
        String instructions,
        String referenceFileName) {
      this.id = id;
      this.regionId = regionId;
      this.assistantEmail = assistantEmail;
      this.taskType = taskType;
      this.instructions = instructions;
      this.referenceFileName = referenceFileName;
      this.createdAt =
          this.updatedAt =
              LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    MangakaProductionTaskDto toDto() {
      return new MangakaProductionTaskDto(
          id,
          regionId,
          assistantEmail,
          taskType,
          instructions,
          referenceFileName,
          status,
          createdAt,
          updatedAt,
          submittedFileName,
          submissionNote,
          submittedAt);
    }

    AssistantTaskDto toAssistantDto(ChapterRecord c, PageRecord p, RegionRecord g) {
      return new AssistantTaskDto(
          id,
          c.proposalId,
          c.id,
          c.title,
          p.id,
          p.pageNumber,
          p.fileName,
          g.id,
          g.regionType,
          g.note,
          assistantEmail,
          taskType,
          instructions,
          referenceFileName,
          status,
          submittedFileName,
          submissionNote,
          createdAt,
          updatedAt,
          submittedAt);
    }
  }
}
