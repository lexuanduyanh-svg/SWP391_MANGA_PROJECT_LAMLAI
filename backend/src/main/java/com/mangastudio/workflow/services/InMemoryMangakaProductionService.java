package com.mangastudio.workflow.services;

import com.mangastudio.workflow.services.InMemoryMangaProposalService;
import com.mangastudio.workflow.dtos.*;
import com.mangastudio.workflow.dtos.MangaProposalDto;
import com.mangastudio.workflow.dtos.MangaProposalStatus;
import com.mangastudio.workflow.dtos.AssistantTaskDto;
import com.mangastudio.workflow.entities.*;
import com.mangastudio.workflow.repositories.*;
import java.math.BigDecimal;
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
  private final SkillRepository skillRepository;
  private final Map<String, ChapterRecord> chapters = new LinkedHashMap<String, ChapterRecord>();
  private final Map<String, RegionRecord> regions = new LinkedHashMap<String, RegionRecord>();
  private final AtomicLong seq = new AtomicLong(500);
  private final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  public InMemoryMangakaProductionService(InMemoryMangaProposalService proposals) { this(proposals, null, null, null, null, null, null, null); }

  @Autowired
  public InMemoryMangakaProductionService(
      InMemoryMangaProposalService proposals,
      @Nullable SeriesRepository seriesRepository,
      @Nullable ChapterRepository chapterRepository,
      @Nullable PageRepository pageRepository,
      @Nullable TaskRepository taskRepository,
      @Nullable SubmissionRepository submissionRepository,
      @Nullable UserRepository userRepository,
      @Nullable SkillRepository skillRepository) {
    this.proposals = proposals;
    this.seriesRepository = seriesRepository;
    this.chapterRepository = chapterRepository;
    this.pageRepository = pageRepository;
    this.taskRepository = taskRepository;
    this.submissionRepository = submissionRepository;
    this.userRepository = userRepository;
    this.skillRepository = skillRepository;
    seedMemory();
    seedDbIfPossible();
  }

  public synchronized List<MangakaChapterDto> listChapters(String proposalId, String authorEmail) { return dbMode() ? listChaptersDb(proposalId, authorEmail) : listChaptersMemory(proposalId, authorEmail); }
  public synchronized MangakaChapterDto createChapter(String proposalId, String authorEmail, MangakaChapterCreateRequest r) { return dbMode() ? createChapterDb(proposalId, authorEmail, r) : createChapterMemory(proposalId, authorEmail, r); }
  public synchronized MangakaPageDto addPage(String proposalId, String chapterId, String authorEmail, MangakaPageCreateRequest r) { return dbMode() ? addPageDb(proposalId, chapterId, authorEmail, r) : addPageMemory(proposalId, chapterId, authorEmail, r); }
  public synchronized MangakaPageRegionDto addRegion(String proposalId, String chapterId, String pageId, String authorEmail, MangakaPageRegionCreateRequest r) { return dbMode() ? addRegionDb(proposalId, chapterId, pageId, authorEmail, r) : addRegionMemory(proposalId, chapterId, pageId, authorEmail, r); }
  public synchronized MangakaProductionTaskDto assignTask(String proposalId, String chapterId, String pageId, String regionId, String authorEmail, MangakaProductionTaskCreateRequest request) { return dbMode() ? assignTaskDb(proposalId, chapterId, pageId, regionId, authorEmail, request) : assignTaskMemory(proposalId, chapterId, pageId, regionId, authorEmail, request); }
  public synchronized List<AssistantTaskDto> listAssistantTasks(String assistantEmail) { return dbMode() ? listAssistantTasksDb(assistantEmail) : listAssistantTasksMemory(assistantEmail); }
  public synchronized AssistantTaskDto startAssistantTask(String taskId, String assistantEmail) { return dbMode() ? startAssistantTaskDb(taskId, assistantEmail) : startAssistantTaskMemory(taskId, assistantEmail); }
  public synchronized AssistantTaskDto submitAssistantTask(String taskId, String assistantEmail, String submittedFileName, String submissionNote) { return dbMode() ? submitAssistantTaskDb(taskId, assistantEmail, submittedFileName, submissionNote) : submitAssistantTaskMemory(taskId, assistantEmail, submittedFileName, submissionNote); }
  public synchronized MangakaProductionTaskDto approveTask(String proposalId, String chapterId, String pageId, String regionId, String taskId, String authorEmail) { return dbMode() ? approveTaskDb(proposalId, chapterId, pageId, regionId, taskId, authorEmail) : approveTaskMemory(proposalId, chapterId, pageId, regionId, taskId, authorEmail); }
  public synchronized MangakaProductionTaskDto requestRedoTask(String proposalId, String chapterId, String pageId, String regionId, String taskId, String authorEmail) { return dbMode() ? requestRedoTaskDb(proposalId, chapterId, pageId, regionId, taskId, authorEmail) : requestRedoTaskMemory(proposalId, chapterId, pageId, regionId, taskId, authorEmail); }

  private boolean dbMode() { return seriesRepository != null && chapterRepository != null && pageRepository != null && taskRepository != null && submissionRepository != null && userRepository != null && skillRepository != null; }
  private String now() { return LocalDateTime.now().format(f); }
  private String id() { return String.valueOf(seq.incrementAndGet()); }
  private boolean blank(String s) { return s == null || s.trim().isEmpty(); }

  private void seedMemory() {
    if (!chapters.isEmpty()) return;

    ChapterRecord legacyChapter =
        new ChapterRecord("600", "4", "Seed Production Chapter", 1, MangakaChapterStatus.TasksInProgress);
    PageRecord legacyPage =
        new PageRecord("601", "600", 1, "seed-page.png", MangakaPageStatus.TasksAssigned);
    RegionRecord legacyRegion =
        new RegionRecord("602", "601", "Dialogue", 10, 10, 20, 20, "Seed region");
    legacyRegion.task =
        new TaskRecord(
            "603",
            "602",
            "assistant@manga.local",
            "Lettering",
            "Clean and letter this panel",
            "reference.png",
            MangakaTaskStatus.Pending);
    legacyPage.regions.put(legacyRegion.id, legacyRegion);
    legacyChapter.pages.put(legacyPage.id, legacyPage);
    chapters.put(legacyChapter.id, legacyChapter);
    regions.put(legacyRegion.id, legacyRegion);

    String pid = "201";
    String cid = id();
    String pageId = id();
    String regionId = id();
    ChapterRecord c = new ChapterRecord(cid, pid, "Seed Chapter", 1, MangakaChapterStatus.PagesUploaded);
    PageRecord p = new PageRecord(pageId, cid, 1, "seed-page.png", MangakaPageStatus.TasksAssigned);
    RegionRecord r = new RegionRecord(regionId, pageId, "Dialogue", 10, 10, 20, 20, "Seed region");
    c.pages.put(pageId, p);
    p.regions.put(regionId, r);
    chapters.put(cid, c);
    regions.put(regionId, r);
  }
  private void seedDbIfPossible() {}

  private void ensureAllowed(String proposalId, String authorEmail) { MangaProposalDto p = proposals.getById(proposalId); if (p == null || authorEmail == null || !p.getAuthorEmail().equalsIgnoreCase(authorEmail) || !(p.getStatus() == MangaProposalStatus.Approved || p.getStatus() == MangaProposalStatus.Serializing)) throw new IllegalArgumentException("Proposal not allowed"); }

  private List<MangakaChapterDto> listChaptersMemory(String proposalId, String authorEmail) { ensureAllowed(proposalId, authorEmail); List<MangakaChapterDto> out = new ArrayList<MangakaChapterDto>(); for (ChapterRecord c : chapters.values()) if (proposalId.equals(c.proposalId)) out.add(c.toDto(loadPagesMemory(c.id))); return out; }
  private MangakaChapterDto createChapterMemory(String proposalId, String authorEmail, MangakaChapterCreateRequest r) { ensureAllowed(proposalId, authorEmail); if (r == null || blank(r.getTitle()) || r.getChapterNumber() <= 0) throw new IllegalArgumentException("Chapter data is required"); ChapterRecord c = new ChapterRecord(id(), proposalId, r.getTitle().trim(), r.getChapterNumber(), MangakaChapterStatus.Draft); chapters.put(c.id, c); return c.toDto(new ArrayList<MangakaPageDto>()); }
  private MangakaPageDto addPageMemory(String proposalId, String chapterId, String authorEmail, MangakaPageCreateRequest r) { ChapterRecord c = chapterRequiredMemory(proposalId, chapterId, authorEmail); if (r == null || r.getPageNumber() <= 0 || blank(r.getFileName())) throw new IllegalArgumentException("Page data is required"); PageRecord p = new PageRecord(id(), c.id, r.getPageNumber(), r.getFileName().trim(), MangakaPageStatus.Uploaded); c.pages.put(p.id, p); c.status = MangakaChapterStatus.PagesUploaded; c.updatedAt = now(); return p.toDto(new ArrayList<MangakaPageRegionDto>()); }
  private MangakaPageRegionDto addRegionMemory(String proposalId, String chapterId, String pageId, String authorEmail, MangakaPageRegionCreateRequest r) { PageRecord p = pageRequiredMemory(proposalId, chapterId, pageId, authorEmail); RegionRecord g = new RegionRecord(id(), p.id, r.getRegionType().trim(), r.getX(), r.getY(), r.getWidthPct(), r.getHeightPct(), r.getNote()); p.regions.put(g.id, g); regions.put(g.id, g); return g.toDto(); }
  private MangakaProductionTaskDto assignTaskMemory(String proposalId, String chapterId, String pageId, String regionId, String authorEmail, MangakaProductionTaskCreateRequest request) { RegionRecord g = regionRequiredMemory(proposalId, chapterId, pageId, regionId, authorEmail); g.task = new TaskRecord(id(), regionId, request.getAssistantEmail(), request.getTaskType(), request.getInstructions(), request.getReferenceFileName(), MangakaTaskStatus.Pending); return g.task.toDto(); }
  private List<AssistantTaskDto> listAssistantTasksMemory(String assistantEmail) { List<AssistantTaskDto> out = new ArrayList<AssistantTaskDto>(); for (RegionRecord r : regions.values()) if (r.task != null && r.task.assistantEmail.equalsIgnoreCase(assistantEmail)) out.add(r.task.toAssistantDto(findRegionPath(r.id))); return out; }
  private AssistantTaskDto startAssistantTaskMemory(String taskId, String assistantEmail) { TaskRecord t = taskRequiredMemory(taskId, assistantEmail); if (!(t.status == MangakaTaskStatus.Pending || t.status == MangakaTaskStatus.RedoRequested)) throw new IllegalArgumentException("Task cannot be started in current status"); t.status = MangakaTaskStatus.InProgress; t.updatedAt = now(); return t.toAssistantDto(findRegionPath(t.regionId)); }
  private AssistantTaskDto submitAssistantTaskMemory(String taskId, String assistantEmail, String submittedFileName, String submissionNote) { TaskRecord t = taskRequiredMemory(taskId, assistantEmail); t.status = MangakaTaskStatus.Submitted; t.submittedFileName = submittedFileName; t.submissionNote = submissionNote; t.submittedAt = now(); t.updatedAt = t.submittedAt; return t.toAssistantDto(findRegionPath(t.regionId)); }
  private MangakaProductionTaskDto approveTaskMemory(String proposalId, String chapterId, String pageId, String regionId, String taskId, String authorEmail) { ensureAllowed(proposalId, authorEmail); TaskRecord t = taskRequiredMemory(taskId, null); t.status = MangakaTaskStatus.Approved; t.updatedAt = now(); return t.toDto(); }
  private MangakaProductionTaskDto requestRedoTaskMemory(String proposalId, String chapterId, String pageId, String regionId, String taskId, String authorEmail) {
    ensureAllowed(proposalId, authorEmail);
    TaskRecord t = taskRequiredMemory(taskId, null);
    if (t.status == MangakaTaskStatus.Approved) {
      throw new IllegalArgumentException("Approved task cannot be sent back for redo");
    }
    t.status = MangakaTaskStatus.RedoRequested;
    t.updatedAt = now();
    return t.toDto();
  }

  private ChapterRecord chapterRequiredMemory(String proposalId, String chapterId, String authorEmail) { ensureAllowed(proposalId, authorEmail); ChapterRecord c = chapters.get(chapterId); if (c == null || !proposalId.equals(c.proposalId)) throw new IllegalArgumentException("Chapter not found"); return c; }
  private PageRecord pageRequiredMemory(String proposalId, String chapterId, String pageId, String authorEmail) { ChapterRecord c = chapterRequiredMemory(proposalId, chapterId, authorEmail); PageRecord p = c.pages.get(pageId); if (p == null) throw new IllegalArgumentException("Page not found"); return p; }
  private RegionRecord regionRequiredMemory(String proposalId, String chapterId, String pageId, String regionId, String authorEmail) { PageRecord p = pageRequiredMemory(proposalId, chapterId, pageId, authorEmail); RegionRecord r = p.regions.get(regionId); if (r == null) throw new IllegalArgumentException("Region not found"); return r; }
  private TaskRecord taskRequiredMemory(String taskId, String assistantEmail) { for (RegionRecord r : regions.values()) if (r.task != null && r.task.id.equals(taskId) && (assistantEmail == null || r.task.assistantEmail.equalsIgnoreCase(assistantEmail))) return r.task; throw new IllegalArgumentException("Task not found"); }
  private List<MangakaPageDto> loadPagesMemory(String chapterId) { ChapterRecord c = chapters.get(chapterId); List<MangakaPageDto> out = new ArrayList<MangakaPageDto>(); if (c != null) for (PageRecord p : c.pages.values()) out.add(p.toDto(loadRegionsMemory(p.id))); return out; }
  private List<MangakaPageRegionDto> loadRegionsMemory(String pageId) { List<MangakaPageRegionDto> out = new ArrayList<MangakaPageRegionDto>(); for (RegionRecord r : regions.values()) if (pageId.equals(r.pageId)) out.add(r.toDto()); return out; }
  private RegionRecord findRegionPath(String regionId) { RegionRecord r = regions.get(regionId); if (r != null) return r; for (ChapterRecord c : chapters.values()) for (PageRecord p : c.pages.values()) if (p.regions.containsKey(regionId)) return p.regions.get(regionId); throw new IllegalArgumentException("Region not found"); }

  private List<MangakaChapterDto> listChaptersDb(String proposalId, String authorEmail) { return listChaptersMemory(proposalId, authorEmail); }
  private MangakaChapterDto createChapterDb(String proposalId, String authorEmail, MangakaChapterCreateRequest r) { return createChapterMemory(proposalId, authorEmail, r); }
  private MangakaPageDto addPageDb(String proposalId, String chapterId, String authorEmail, MangakaPageCreateRequest r) { return addPageMemory(proposalId, chapterId, authorEmail, r); }
  private MangakaPageRegionDto addRegionDb(String proposalId, String chapterId, String pageId, String authorEmail, MangakaPageRegionCreateRequest r) { return addRegionMemory(proposalId, chapterId, pageId, authorEmail, r); }
  private MangakaProductionTaskDto assignTaskDb(String proposalId, String chapterId, String pageId, String regionId, String authorEmail, MangakaProductionTaskCreateRequest request) { return assignTaskMemory(proposalId, chapterId, pageId, regionId, authorEmail, request); }
  private List<AssistantTaskDto> listAssistantTasksDb(String assistantEmail) { return listAssistantTasksMemory(assistantEmail); }
  private AssistantTaskDto startAssistantTaskDb(String taskId, String assistantEmail) { return startAssistantTaskMemory(taskId, assistantEmail); }
  private AssistantTaskDto submitAssistantTaskDb(String taskId, String assistantEmail, String submittedFileName, String submissionNote) { return submitAssistantTaskMemory(taskId, assistantEmail, submittedFileName, submissionNote); }
  private MangakaProductionTaskDto approveTaskDb(String proposalId, String chapterId, String pageId, String regionId, String taskId, String authorEmail) { return approveTaskMemory(proposalId, chapterId, pageId, regionId, taskId, authorEmail); }
  private MangakaProductionTaskDto requestRedoTaskDb(String proposalId, String chapterId, String pageId, String regionId, String taskId, String authorEmail) { return requestRedoTaskMemory(proposalId, chapterId, pageId, regionId, taskId, authorEmail); }

  private static class ChapterRecord { String id, proposalId, title, createdAt, updatedAt; int chapterNumber; MangakaChapterStatus status; Map<String, PageRecord> pages = new LinkedHashMap<String, PageRecord>(); ChapterRecord(String id, String proposalId, String title, int chapterNumber, MangakaChapterStatus status) { this.id=id; this.proposalId=proposalId; this.title=title; this.chapterNumber=chapterNumber; this.status=status; this.createdAt=this.updatedAt=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")); } MangakaChapterDto toDto(List<MangakaPageDto> pages) { return new MangakaChapterDto(id, proposalId, title, chapterNumber, status, createdAt, updatedAt, pages); } }
  private static class PageRecord { String id, chapterId, fileName, uploadedAt; int pageNumber; MangakaPageStatus status; Map<String, RegionRecord> regions = new LinkedHashMap<String, RegionRecord>(); PageRecord(String id, String chapterId, int pageNumber, String fileName, MangakaPageStatus status) { this.id=id; this.chapterId=chapterId; this.pageNumber=pageNumber; this.fileName=fileName; this.status=status; this.uploadedAt=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")); } MangakaPageDto toDto(List<MangakaPageRegionDto> regions) { return new MangakaPageDto(id, chapterId, pageNumber, fileName, status, uploadedAt, regions); } }
  private static class RegionRecord { String id, pageId, regionType, note; double x, y, widthPct, heightPct; TaskRecord task; RegionRecord(String id, String pageId, String regionType, double x, double y, double widthPct, double heightPct, String note) { this.id=id; this.pageId=pageId; this.regionType=regionType; this.x=x; this.y=y; this.widthPct=widthPct; this.heightPct=heightPct; this.note=note; } MangakaPageRegionDto toDto() { return new MangakaPageRegionDto(id, pageId, regionType, x, y, widthPct, heightPct, note, new ArrayList<MangakaProductionTaskDto>()); } }
  private static class TaskRecord { String id, regionId, assistantEmail, taskType, instructions, referenceFileName, createdAt, updatedAt, submittedFileName, submissionNote, submittedAt; MangakaTaskStatus status; TaskRecord(String id, String regionId, String assistantEmail, String taskType, String instructions, String referenceFileName, MangakaTaskStatus status) { this.id=id; this.regionId=regionId; this.assistantEmail=assistantEmail; this.taskType=taskType; this.instructions=instructions; this.referenceFileName=referenceFileName; this.status=status; this.createdAt=this.updatedAt=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")); } MangakaProductionTaskDto toDto() { return new MangakaProductionTaskDto(id, regionId, assistantEmail, taskType, instructions, referenceFileName, status, createdAt, updatedAt, submittedFileName, submissionNote, submittedAt); } AssistantTaskDto toAssistantDto(RegionRecord r) { return new AssistantTaskDto(id, r == null ? null : r.pageId, null, null, null, 0, null, regionId, r == null ? null : r.regionType, r == null ? null : r.note, assistantEmail, taskType, instructions, referenceFileName, status, submittedFileName, submissionNote, createdAt, updatedAt, submittedAt); } }
}
