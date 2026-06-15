package com.mangaworkflow.application.proposal;

import com.mangaworkflow.domain.proposal.*;
import com.mangaworkflow.persistence.entity.BoardVoteEntity;
import com.mangaworkflow.persistence.entity.SeriesEntity;
import com.mangaworkflow.persistence.entity.UserEntity;
import com.mangaworkflow.persistence.repository.BoardVoteRepository;
import com.mangaworkflow.persistence.repository.SeriesRepository;
import com.mangaworkflow.persistence.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class InMemoryMangaProposalService {
  private final SeriesRepository seriesRepository;
  private final BoardVoteRepository boardVoteRepository;
  private final UserRepository userRepository;
  private final Map<String, ProposalRecord> proposals = new LinkedHashMap<String, ProposalRecord>();
  private final Map<Long, ProposalMetadata> seriesMetadata = new LinkedHashMap<Long, ProposalMetadata>();
  private final AtomicLong sequence = new AtomicLong(200);
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  private static final String[] BOARD_MEMBERS = {
    "board@manga.local", "board2@manga.local", "board3@manga.local"
  };

  public InMemoryMangaProposalService() {
    this.seriesRepository = null;
    this.boardVoteRepository = null;
    this.userRepository = null;
    seedMemory();
    seedDbIfEmpty();
  }

  @Autowired
  public InMemoryMangaProposalService(
      @Nullable SeriesRepository seriesRepository,
      @Nullable BoardVoteRepository boardVoteRepository,
      @Nullable UserRepository userRepository) {
    this.seriesRepository = seriesRepository;
    this.boardVoteRepository = boardVoteRepository;
    this.userRepository = userRepository;
    seedMemory();
    seedDbIfEmpty();
  }

  public synchronized List<MangaProposalDto> listByAuthorEmail(String authorEmail) {
    return schemaMode()
        ? listByAuthorEmailDb(authorEmail)
        : listByAuthorEmailMemory(authorEmail);
  }

  public synchronized MangaProposalDto getById(String id) {
    return schemaMode() ? getByIdDb(id) : getByIdMemory(id);
  }

  public synchronized MangaProposalDto create(MangaProposalCreateRequest request) {
    return schemaMode() ? createDb(request) : createMemory(request);
  }

  public synchronized MangaProposalDto update(
      String id, String authorEmail, MangaProposalUpdateRequest request) {
    return schemaMode()
        ? updateDb(id, authorEmail, request)
        : updateMemory(id, authorEmail, request);
  }

  public synchronized MangaProposalDto submit(String id, String authorEmail) {
    return schemaMode() ? submitDb(id, authorEmail) : submitMemory(id, authorEmail);
  }

  public synchronized void delete(String id, String authorEmail) {
    if (schemaMode()) deleteDb(id, authorEmail);
    else deleteMemory(id, authorEmail);
  }

  public synchronized MangaProposalDto attachManuscriptMetadata(
      String id, String authorEmail, String fileName, String summary) {
    validateEmail(authorEmail, "authorEmail");
    if (blank(id)) throw new IllegalArgumentException("proposalId is required");
    if (blank(fileName)) throw new IllegalArgumentException("fileName is required");
    if (blank(summary)) throw new IllegalArgumentException("summary is required");
    return schemaMode()
        ? attachManuscriptMetadataDb(id, authorEmail, fileName, summary)
        : attachManuscriptMetadataMemory(id, authorEmail, fileName, summary);
  }

  public synchronized List<MangaProposalDto> listForEditor(String editorEmail) {
    validateEmail(editorEmail, "editorEmail");
    return schemaMode()
        ? listByStatusesDb(
            MangaProposalStatus.SubmittedToEditor,
            MangaProposalStatus.UnderBoardReview,
            MangaProposalStatus.Approved,
            MangaProposalStatus.Rejected,
            MangaProposalStatus.NeedsRevision)
        : listByStatusesMemory(
            MangaProposalStatus.SubmittedToEditor,
            MangaProposalStatus.UnderBoardReview,
            MangaProposalStatus.Approved,
            MangaProposalStatus.Rejected,
            MangaProposalStatus.NeedsRevision);
  }

  public synchronized MangaProposalDto forwardToBoard(String id, String editorEmail, String note) {
    return schemaMode()
        ? forwardToBoardDb(id, editorEmail, note)
        : forwardToBoardMemory(id, editorEmail, note);
  }

  public synchronized MangaProposalDto requestRevisionByEditor(
      String id, String editorEmail, String note) {
    return schemaMode()
        ? requestRevisionByEditorDb(id, editorEmail, note)
        : requestRevisionByEditorMemory(id, editorEmail, note);
  }

  public synchronized MangaProposalDto rejectByEditor(String id, String editorEmail, String note) {
    return schemaMode()
        ? rejectByEditorDb(id, editorEmail, note)
        : rejectByEditorMemory(id, editorEmail, note);
  }

  public synchronized List<MangaProposalDto> listForBoard(String memberEmail) {
    validateEmail(memberEmail, "memberEmail");
    return schemaMode() ? listForBoardDb(memberEmail) : listForBoardMemory(memberEmail);
  }

  public synchronized MangaProposalDto approveByBoard(String id, String memberEmail, String note) {
    return schemaMode()
        ? voteByBoardDb(id, memberEmail, true)
        : voteByBoardMemory(id, memberEmail, true);
  }

  public synchronized MangaProposalDto rejectByBoard(String id, String memberEmail, String note) {
    return schemaMode()
        ? voteByBoardDb(id, memberEmail, false)
        : voteByBoardMemory(id, memberEmail, false);
  }

  private boolean schemaMode() {
    return seriesRepository != null
        && boardVoteRepository != null
        && userRepository != null;
  }

  private void seedMemory() {
    seed("1", "Seed Draft", MangaProposalStatus.Draft);
    seed("2", "Seed Revision", MangaProposalStatus.NeedsRevision);
    seed("3", "Seed Submitted", MangaProposalStatus.SubmittedToEditor);
    seed("4", "Seed Approved", MangaProposalStatus.Approved);
    seed("5", "Seed Serializing", MangaProposalStatus.Serializing);
  }

  private void seedDbIfEmpty() {
    if (schemaMode()) {
      seedSeriesIfEmpty();
    }
  }

  private void seedSeriesIfEmpty() {
    if (seriesRepository == null || seriesRepository.count() > 0) return;
    UserEntity mangaka = userRepository.findByEmailIgnoreCase(normalize("mangaka@manga.local")).orElse(null);
    if (mangaka == null) return;
    saveSeriesSeed(mangaka, "Seed Draft", "DRAFT");
    saveSeriesSeed(mangaka, "Seed Revision", "REVISION_REQUESTED");
    saveSeriesSeed(mangaka, "Seed Submitted", "SUBMITTED_TO_EDITOR");
    saveSeriesSeed(mangaka, "Seed Approved", "APPROVED");
  }

  private void saveSeriesSeed(UserEntity mangaka, String title, String status) {
    SeriesEntity e = new SeriesEntity();
    LocalDateTime now = LocalDateTime.now();
    e.setMangaka(mangaka);
    e.setTitle(title);
    e.setGenre("Action");
    e.setSynopsis("Seed synopsis");
    e.setStatus(status);
    e.setPublishingFrequency("WEEKLY");
    e.setCreatedAt(now);
    e.setUpdatedAt(now);
    seriesRepository.save(e);
  }

  private List<MangaProposalDto> listByAuthorEmailMemory(String authorEmail) {
    List<MangaProposalDto> result = new ArrayList<MangaProposalDto>();
    for (ProposalRecord r : proposals.values())
      if (r.authorEmail.equals(normalize(authorEmail))) result.add(r.toDto());
    sortNewest(result);
    return Collections.unmodifiableList(result);
  }

  private List<MangaProposalDto> listByAuthorEmailDb(String authorEmail) {
    List<MangaProposalDto> result = new ArrayList<MangaProposalDto>();
    for (SeriesEntity e : seriesRepository.findByMangaka_EmailIgnoreCaseOrderByUpdatedAtDesc(normalize(authorEmail)))
      result.add(toDto(e));
    sortNewest(result);
    return Collections.unmodifiableList(result);
  }

  private MangaProposalDto getByIdMemory(String id) {
    ProposalRecord record = proposals.get(id);
    return record == null ? null : record.toDto();
  }

  private MangaProposalDto getByIdDb(String id) {
    return seriesRepository.findById(Long.valueOf(id)).map(this::toDto).orElse(null);
  }

  private MangaProposalDto createMemory(MangaProposalCreateRequest request) {
    validateCreate(request);
    String id = String.valueOf(sequence.incrementAndGet());
    String now = now();
    ProposalRecord record =
        new ProposalRecord(
            id,
            request.getTitle().trim(),
            request.getGenre().trim(),
            request.getTargetAudience().trim(),
            request.getSynopsis().trim(),
            request.getManuscriptTitle().trim(),
            request.getManuscriptSummary().trim(),
            emptyToNull(request.getManuscriptFileName()),
            versionForCreate(request.getManuscriptFileName()),
            emptyToNull(request.getManuscriptFileName()) == null ? null : now,
            normalize(request.getAuthorEmail()),
            MangaProposalStatus.Draft,
            null,
            now);
    proposals.put(id, record);
    return record.toDto();
  }

  private MangaProposalDto createDb(MangaProposalCreateRequest request) {
    validateCreate(request);
    UserEntity mangaka = resolveMangaka(request.getAuthorEmail());
    SeriesEntity e = new SeriesEntity();
    LocalDateTime now = LocalDateTime.now();
    e.setMangaka(mangaka);
    e.setTitle(request.getTitle().trim());
    e.setGenre(request.getGenre().trim());
    e.setSynopsis(request.getSynopsis().trim());
    e.setStatus("DRAFT");
    e.setPublishingFrequency("WEEKLY");
    e.setCreatedAt(now);
    e.setUpdatedAt(now);
    SeriesEntity saved = seriesRepository.save(e);
    saveMetadata(
        saved.getId(),
        request.getTargetAudience(),
        request.getManuscriptTitle(),
        request.getManuscriptSummary(),
        request.getManuscriptFileName(),
        emptyToNull(request.getManuscriptFileName()) == null ? null : format(now));
    return toDto(saved);
  }

  private MangaProposalDto updateMemory(
      String id, String authorEmail, MangaProposalUpdateRequest request) {
    validateUpdate(request);
    ProposalRecord record = getRequired(id);
    ensureOwner(record, authorEmail);
    ensureMutable(record);
    record.title = request.getTitle().trim();
    record.genre = request.getGenre().trim();
    record.targetAudience = request.getTargetAudience().trim();
    record.synopsis = request.getSynopsis().trim();
    record.manuscriptTitle = request.getManuscriptTitle().trim();
    record.manuscriptSummary = request.getManuscriptSummary().trim();
    if (!blank(request.getManuscriptFileName())) {
      if (record.manuscriptFileName == null
          || !record.manuscriptFileName.equals(request.getManuscriptFileName().trim()))
        record.manuscriptVersion = Integer.valueOf(record.manuscriptVersion.intValue() + 1);
      record.manuscriptFileName = request.getManuscriptFileName().trim();
      record.manuscriptUploadedAt = now();
    }
    record.updatedAt = now();
    return record.toDto();
  }

  private MangaProposalDto updateDb(
      String id, String authorEmail, MangaProposalUpdateRequest request) {
    validateUpdate(request);
    SeriesEntity e = getRequiredSeriesForAuthor(id, authorEmail);
    if (!("DRAFT".equals(e.getStatus()) || "REVISION_REQUESTED".equals(e.getStatus())))
      throw new IllegalArgumentException("Proposal cannot be updated in current status");
    e.setTitle(request.getTitle().trim());
    e.setGenre(request.getGenre().trim());
    e.setSynopsis(request.getSynopsis().trim());
    e.setUpdatedAt(LocalDateTime.now());
    saveMetadata(
        e.getId(),
        request.getTargetAudience(),
        request.getManuscriptTitle(),
        request.getManuscriptSummary(),
        request.getManuscriptFileName(),
        !blank(request.getManuscriptFileName()) ? format(e.getUpdatedAt()) : null);
    return toDto(seriesRepository.save(e));
  }

  private MangaProposalDto submitMemory(String id, String authorEmail) {
    ProposalRecord record = getRequired(id);
    ensureOwner(record, authorEmail);
    ensureSubmitable(record);
    if (blank(record.manuscriptFileName))
      throw new IllegalArgumentException("Manuscript file is required before submission");
    record.status = MangaProposalStatus.SubmittedToEditor;
    record.submittedAt = now();
    record.updatedAt = record.submittedAt;
    return record.toDto();
  }

  private MangaProposalDto submitDb(String id, String authorEmail) {
    SeriesEntity e = getRequiredSeriesForAuthor(id, authorEmail);
    if (!("DRAFT".equals(e.getStatus()) || "REVISION_REQUESTED".equals(e.getStatus())))
      throw new IllegalArgumentException("Proposal cannot be submitted in current status");
    e.setStatus("SUBMITTED_TO_EDITOR");
    e.setUpdatedAt(LocalDateTime.now());
    return toDto(seriesRepository.save(e));
  }

  private void deleteMemory(String id, String authorEmail) {
    ProposalRecord record = getRequired(id);
    ensureOwner(record, authorEmail);
    proposals.remove(id);
  }

  private void deleteDb(String id, String authorEmail) {
    SeriesEntity e = getRequiredSeriesForAuthor(id, authorEmail);
    seriesRepository.delete(e);
    seriesMetadata.remove(e.getId());
  }

  private MangaProposalDto attachManuscriptMetadataMemory(
      String id, String authorEmail, String fileName, String summary) {
    ProposalRecord record = getRequired(id);
    ensureOwner(record, authorEmail);
    ensureMutable(record);
    String cleanFileName = fileName.trim();
    if (record.manuscriptFileName == null || !record.manuscriptFileName.equals(cleanFileName)) {
      record.manuscriptVersion =
          record.manuscriptVersion == null
              ? Integer.valueOf(1)
              : Integer.valueOf(record.manuscriptVersion.intValue() + 1);
    }
    record.manuscriptFileName = cleanFileName;
    record.manuscriptSummary = summary.trim();
    record.manuscriptUploadedAt = now();
    record.updatedAt = record.manuscriptUploadedAt;
    return record.toDto();
  }

  private MangaProposalDto attachManuscriptMetadataDb(
      String id, String authorEmail, String fileName, String summary) {
    SeriesEntity e = getRequiredSeriesForAuthor(id, authorEmail);
    if (!("DRAFT".equals(e.getStatus()) || "REVISION_REQUESTED".equals(e.getStatus())))
      throw new IllegalArgumentException("Proposal cannot be updated in current status");
    String cleanFileName = fileName.trim();
    ProposalMetadata metadata = metadataFor(e.getId());
    metadata.manuscriptVersion =
        metadata.manuscriptFileName == null || !metadata.manuscriptFileName.equals(cleanFileName)
            ? Integer.valueOf(metadata.manuscriptVersion == null ? 1 : metadata.manuscriptVersion + 1)
            : metadata.manuscriptVersion;
    metadata.manuscriptFileName = cleanFileName;
    metadata.manuscriptSummary = summary.trim();
    metadata.manuscriptUploadedAt = now();
    e.setUpdatedAt(LocalDateTime.now());
    return toDto(seriesRepository.save(e));
  }

  private List<MangaProposalDto> listByStatusesMemory(MangaProposalStatus... statuses) {
    List<MangaProposalDto> result = new ArrayList<MangaProposalDto>();
    for (ProposalRecord r : proposals.values())
      for (MangaProposalStatus status : statuses)
        if (r.status == status) {
          result.add(r.toDto());
          break;
        }
    sortNewest(result);
    return Collections.unmodifiableList(result);
  }

  private List<MangaProposalDto> listForBoardMemory(String memberEmail) {
    String viewer = normalize(memberEmail);
    List<MangaProposalDto> result = new ArrayList<MangaProposalDto>();
    for (ProposalRecord r : proposals.values())
      if (r.status == MangaProposalStatus.UnderBoardReview
          || r.status == MangaProposalStatus.Approved
          || r.status == MangaProposalStatus.Rejected
          || r.status == MangaProposalStatus.Serializing) result.add(r.toDtoFor(viewer));
    sortNewest(result);
    return Collections.unmodifiableList(result);
  }

  private List<MangaProposalDto> listForBoardDb(String memberEmail) {
    String viewer = normalize(memberEmail);
    List<String> statuses = new ArrayList<String>();
    statuses.add("UNDER_BOARD_REVIEW");
    statuses.add("APPROVED");
    statuses.add("REJECTED");
    List<MangaProposalDto> result = new ArrayList<MangaProposalDto>();
    for (SeriesEntity e : seriesRepository.findByStatusInOrderByUpdatedAtDesc(statuses))
      result.add(toDto(e, viewer));
    sortNewest(result);
    return Collections.unmodifiableList(result);
  }

  private List<MangaProposalDto> listByStatusesDb(MangaProposalStatus... statuses) {
    List<String> ss = new ArrayList<String>();
    for (MangaProposalStatus s : statuses) ss.add(toSeriesStatus(s));
    List<MangaProposalDto> result = new ArrayList<MangaProposalDto>();
    for (SeriesEntity e : seriesRepository.findByStatusInOrderByUpdatedAtDesc(ss)) result.add(toDto(e));
    sortNewest(result);
    return Collections.unmodifiableList(result);
  }

  private MangaProposalDto forwardToBoardMemory(String id, String editorEmail, String note) {
    validateEmail(editorEmail, "editorEmail");
    ProposalRecord record = getRequired(id);
    if (record.status != MangaProposalStatus.SubmittedToEditor)
      throw new IllegalArgumentException("Proposal cannot be forwarded in current status");
    record.status = MangaProposalStatus.UnderBoardReview;
    record.editorEmail = normalize(editorEmail);
    record.editorNote = emptyToNull(note);
    record.editorReviewedAt = now();
    record.updatedAt = record.editorReviewedAt;
    return record.toDto();
  }

  private MangaProposalDto forwardToBoardDb(String id, String editorEmail, String note) {
    validateEmail(editorEmail, "editorEmail");
    SeriesEntity e = getRequiredSeries(id);
    if (!"SUBMITTED_TO_EDITOR".equals(e.getStatus()))
      throw new IllegalArgumentException("Proposal cannot be forwarded in current status");
    e.setStatus("UNDER_BOARD_REVIEW");
    e.setTantouEditor(resolveEditor(editorEmail));
    e.setEditorNotes(emptyToNull(note));
    e.setUpdatedAt(LocalDateTime.now());
    return toDto(seriesRepository.save(e));
  }

  private MangaProposalDto requestRevisionByEditorMemory(
      String id, String editorEmail, String note) {
    validateEmail(editorEmail, "editorEmail");
    ProposalRecord record = getRequired(id);
    if (record.status != MangaProposalStatus.SubmittedToEditor)
      throw new IllegalArgumentException("Proposal cannot be revised in current status");
    record.status = MangaProposalStatus.NeedsRevision;
    record.editorEmail = normalize(editorEmail);
    record.editorNote = emptyToNull(note);
    record.editorReviewedAt = now();
    record.updatedAt = record.editorReviewedAt;
    return record.toDto();
  }

  private MangaProposalDto requestRevisionByEditorDb(String id, String editorEmail, String note) {
    validateEmail(editorEmail, "editorEmail");
    SeriesEntity e = getRequiredSeries(id);
    if (!"SUBMITTED_TO_EDITOR".equals(e.getStatus()))
      throw new IllegalArgumentException("Proposal cannot be revised in current status");
    e.setStatus("REVISION_REQUESTED");
    e.setTantouEditor(resolveEditor(editorEmail));
    e.setEditorNotes(emptyToNull(note));
    e.setUpdatedAt(LocalDateTime.now());
    return toDto(seriesRepository.save(e));
  }

  private MangaProposalDto rejectByEditorMemory(String id, String editorEmail, String note) {
    validateEmail(editorEmail, "editorEmail");
    ProposalRecord record = getRequired(id);
    if (record.status != MangaProposalStatus.SubmittedToEditor)
      throw new IllegalArgumentException("Proposal cannot be rejected in current status");
    record.status = MangaProposalStatus.Rejected;
    record.editorEmail = normalize(editorEmail);
    record.editorNote = emptyToNull(note);
    record.editorReviewedAt = now();
    record.updatedAt = record.editorReviewedAt;
    return record.toDto();
  }

  private MangaProposalDto rejectByEditorDb(String id, String editorEmail, String note) {
    validateEmail(editorEmail, "editorEmail");
    SeriesEntity e = getRequiredSeries(id);
    if (!"SUBMITTED_TO_EDITOR".equals(e.getStatus()))
      throw new IllegalArgumentException("Proposal cannot be rejected in current status");
    e.setStatus("REJECTED");
    e.setTantouEditor(resolveEditor(editorEmail));
    e.setEditorNotes(emptyToNull(note));
    e.setUpdatedAt(LocalDateTime.now());
    return toDto(seriesRepository.save(e));
  }

  private MangaProposalDto voteByBoardMemory(String id, String memberEmail, boolean approve) {
    validateEmail(memberEmail, "memberEmail");
    ProposalRecord record = getRequired(id);
    if (record.status != MangaProposalStatus.UnderBoardReview)
      throw new IllegalArgumentException("Proposal is no longer open for board voting");
    String voter = normalize(memberEmail);
    if (!isBoardMember(voter))
      throw new IllegalArgumentException("Only editorial board members can vote");
    if (record.boardVotes.containsKey(voter))
      throw new IllegalArgumentException("This board member already voted");
    record.boardVotes.put(voter, approve ? "APPROVE" : "REJECT");
    record.boardMemberEmail = voter;
    record.boardReviewedAt = now();
    int approveVotes = countVotes(record, "APPROVE");
    int rejectVotes = countVotes(record, "REJECT");
    if (record.boardVotes.size() >= BOARD_MEMBERS.length) {
      record.status =
          approveVotes > rejectVotes ? MangaProposalStatus.Approved : MangaProposalStatus.Rejected;
      record.boardDecisionNote =
          record.status == MangaProposalStatus.Approved
              ? "Board vote result: approved by majority."
              : "Board vote result: rejected by majority.";
    }
    record.updatedAt = now();
    return record.toDtoFor(voter);
  }

  private MangaProposalDto voteByBoardDb(String id, String memberEmail, boolean approve) {
    validateEmail(memberEmail, "memberEmail");
    SeriesEntity e = getRequiredSeries(id);
    if (!"UNDER_BOARD_REVIEW".equals(e.getStatus()))
      throw new IllegalArgumentException("Proposal is no longer open for board voting");
    String voter = normalize(memberEmail);
    UserEntity boardMember = resolveBoardMember(voter);
    if (boardVoteRepository.existsBySeries_IdAndBoardMember_EmailIgnoreCase(e.getId(), voter))
      throw new IllegalArgumentException("This board member already voted");

    BoardVoteEntity vote = new BoardVoteEntity();
    vote.setSeries(e);
    vote.setBoardMember(boardMember);
    vote.setDecision(approve ? "APPROVE" : "REJECT");
    boardVoteRepository.save(vote);

    List<BoardVoteEntity> votes = boardVoteRepository.findBySeries_Id(e.getId());
    int approveVotes = countVotes(votes, "APPROVE");
    int rejectVotes = countVotes(votes, "REJECT");
    if (votes.size() >= BOARD_MEMBERS.length) {
      e.setStatus(approveVotes > rejectVotes ? "APPROVED" : "REJECTED");
    }
    e.setUpdatedAt(LocalDateTime.now());
    return toDto(seriesRepository.save(e), voter);
  }

  private boolean isBoardMember(String email) {
    for (String member : BOARD_MEMBERS) if (member.equals(email)) return true;
    return false;
  }

  private int countVotes(ProposalRecord record, String vote) {
    int count = 0;
    for (String value : record.boardVotes.values()) if (vote.equals(value)) count++;
    return count;
  }

  private void sortNewest(List<MangaProposalDto> items) {
    Collections.sort(
        items,
        new Comparator<MangaProposalDto>() {
          public int compare(MangaProposalDto left, MangaProposalDto right) {
            return comparableTime(right).compareTo(comparableTime(left));
          }
        });
  }

  private String comparableTime(MangaProposalDto proposal) {
    String value =
        proposal.getSubmittedAt() != null ? proposal.getSubmittedAt() : proposal.getUpdatedAt();
    if (value == null) value = proposal.getManuscriptUploadedAt();
    if (value != null) return value;
    return String.format("%020d", Long.valueOf(proposal.getId()).longValue());
  }

  private ProposalRecord getRequired(String id) {
    ProposalRecord record = proposals.get(id);
    if (record == null) throw new IllegalArgumentException("Proposal not found");
    return record;
  }

  private SeriesEntity getRequiredSeries(String id) {
    return seriesRepository
        .findById(Long.valueOf(id))
        .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
  }

  private SeriesEntity getRequiredSeriesForAuthor(String id, String authorEmail) {
    validateEmail(authorEmail, "authorEmail");
    return seriesRepository
        .findByIdAndMangaka_EmailIgnoreCase(Long.valueOf(id), normalize(authorEmail))
        .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
  }

  private UserEntity resolveMangaka(String authorEmail) {
    validateEmail(authorEmail, "authorEmail");
    UserEntity user =
        userRepository
            .findByEmailIgnoreCase(normalize(authorEmail))
            .orElseThrow(() -> new IllegalArgumentException("Mangaka account not found"));
    if (user.getRole() == null || !"Mangaka".equalsIgnoreCase(user.getRole().getRoleName())) {
      throw new IllegalArgumentException("authorEmail must belong to a Mangaka account");
    }
    if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
      throw new IllegalArgumentException("Mangaka account is inactive");
    }
    return user;
  }

  private UserEntity resolveEditor(String editorEmail) {
    UserEntity user =
        userRepository
            .findByEmailIgnoreCase(normalize(editorEmail))
            .orElseThrow(() -> new IllegalArgumentException("Editor account not found"));
    if (user.getRole() == null || !"Editor".equalsIgnoreCase(user.getRole().getRoleName())) {
      throw new IllegalArgumentException("editorEmail must belong to an Editor account");
    }
    if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
      throw new IllegalArgumentException("Editor account is inactive");
    }
    return user;
  }

  private UserEntity resolveBoardMember(String memberEmail) {
    UserEntity user =
        userRepository
            .findByEmailIgnoreCase(normalize(memberEmail))
            .orElseThrow(() -> new IllegalArgumentException("Board member account not found"));
    if (user.getRole() == null || !"Board".equalsIgnoreCase(user.getRole().getRoleName())) {
      throw new IllegalArgumentException("Only editorial board members can vote");
    }
    if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
      throw new IllegalArgumentException("Board member account is inactive");
    }
    return user;
  }

  private void ensureOwner(ProposalRecord record, String authorEmail) {
    if (authorEmail == null || !record.authorEmail.equals(normalize(authorEmail)))
      throw new IllegalArgumentException("Proposal does not belong to author");
  }

  private void ensureMutable(ProposalRecord record) {
    if (!(record.status == MangaProposalStatus.Draft
        || record.status == MangaProposalStatus.NeedsRevision))
      throw new IllegalArgumentException("Proposal cannot be updated in current status");
  }

  private void ensureSubmitable(ProposalRecord record) {
    if (!(record.status == MangaProposalStatus.Draft
        || record.status == MangaProposalStatus.NeedsRevision))
      throw new IllegalArgumentException("Proposal cannot be submitted in current status");
  }

  private void validateCreate(MangaProposalCreateRequest r) {
    if (r == null
        || blank(r.getAuthorEmail())
        || blank(r.getTitle())
        || blank(r.getGenre())
        || blank(r.getTargetAudience())
        || blank(r.getSynopsis())
        || blank(r.getManuscriptTitle())
        || blank(r.getManuscriptSummary()))
      throw new IllegalArgumentException("All proposal fields are required");
  }

  private void validateUpdate(MangaProposalUpdateRequest r) {
    if (r == null
        || blank(r.getTitle())
        || blank(r.getGenre())
        || blank(r.getTargetAudience())
        || blank(r.getSynopsis())
        || blank(r.getManuscriptTitle())
        || blank(r.getManuscriptSummary()))
      throw new IllegalArgumentException("All proposal fields are required");
  }

  private void validateEmail(String email, String field) {
    if (blank(email)) throw new IllegalArgumentException(field + " is required");
  }

  private String normalize(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private boolean blank(String v) {
    return v == null || v.trim().isEmpty();
  }

  private String now() {
    return LocalDateTime.now().format(formatter);
  }

  private String format(LocalDateTime value) {
    return value == null ? null : value.format(formatter);
  }

  private Integer versionForCreate(String fileName) {
    return blank(fileName) ? null : Integer.valueOf(1);
  }

  private String emptyToNull(String value) {
    return blank(value) ? null : value.trim();
  }

  private int countVotes(Map<String, String> votes, String vote) {
    int count = 0;
    for (String value : votes.values()) if (vote.equals(value)) count++;
    return count;
  }

  private int countVotes(List<BoardVoteEntity> votes, String decision) {
    int count = 0;
    for (BoardVoteEntity vote : votes)
      if (decision.equalsIgnoreCase(vote.getDecision())) count++;
    return count;
  }

  private ProposalMetadata metadataFor(Long seriesId) {
    ProposalMetadata metadata = seriesMetadata.get(seriesId);
    if (metadata == null) {
      metadata = new ProposalMetadata();
      seriesMetadata.put(seriesId, metadata);
    }
    return metadata;
  }

  private void saveMetadata(
      Long seriesId,
      String targetAudience,
      String manuscriptTitle,
      String manuscriptSummary,
      String manuscriptFileName,
      String manuscriptUploadedAt) {
    ProposalMetadata metadata = metadataFor(seriesId);
    if (!blank(targetAudience)) metadata.targetAudience = targetAudience.trim();
    if (!blank(manuscriptTitle)) metadata.manuscriptTitle = manuscriptTitle.trim();
    if (!blank(manuscriptSummary)) metadata.manuscriptSummary = manuscriptSummary.trim();
    if (!blank(manuscriptFileName)) {
      String cleanFileName = manuscriptFileName.trim();
      if (metadata.manuscriptFileName == null || !metadata.manuscriptFileName.equals(cleanFileName)) {
        metadata.manuscriptVersion =
            Integer.valueOf(metadata.manuscriptVersion == null ? 1 : metadata.manuscriptVersion + 1);
      }
      metadata.manuscriptFileName = cleanFileName;
      metadata.manuscriptUploadedAt = manuscriptUploadedAt;
    }
  }

  private MangaProposalDto toDto(SeriesEntity e) {
    ProposalMetadata metadata = metadataFor(e.getId());
    String updatedAt = format(e.getUpdatedAt());
    MangaProposalStatus status = mapSeriesStatus(e.getStatus());
    return new MangaProposalDto(
        String.valueOf(e.getId()),
        e.getTitle(),
        e.getGenre(),
        metadata.targetAudience,
        e.getSynopsis(),
        metadata.manuscriptTitle != null ? metadata.manuscriptTitle : e.getTitle(),
        metadata.manuscriptSummary != null ? metadata.manuscriptSummary : e.getSynopsis(),
        metadata.manuscriptFileName,
        metadata.manuscriptVersion,
        metadata.manuscriptUploadedAt,
        e.getMangaka() == null ? null : e.getMangaka().getEmail(),
        status,
        status == MangaProposalStatus.Draft ? null : updatedAt,
        updatedAt,
        e.getTantouEditor() == null ? null : e.getTantouEditor().getEmail(),
        e.getEditorNotes(),
        null,
        null,
        null,
        null,
        Integer.valueOf(0),
        Integer.valueOf(0),
        Integer.valueOf(BOARD_MEMBERS.length),
        Integer.valueOf(BOARD_MEMBERS.length),
        null);
  }

  private MangaProposalStatus mapSeriesStatus(String status) {
    if ("SUBMITTED_TO_EDITOR".equalsIgnoreCase(status)) return MangaProposalStatus.SubmittedToEditor;
    if ("REVISION_REQUESTED".equalsIgnoreCase(status)) return MangaProposalStatus.NeedsRevision;
    if ("UNDER_BOARD_REVIEW".equalsIgnoreCase(status)) return MangaProposalStatus.UnderBoardReview;
    if ("APPROVED".equalsIgnoreCase(status)) return MangaProposalStatus.Approved;
    if ("REJECTED".equalsIgnoreCase(status)) return MangaProposalStatus.Rejected;
    return MangaProposalStatus.Draft;
  }

  private String toSeriesStatus(MangaProposalStatus status) {
    if (status == MangaProposalStatus.SubmittedToEditor) return "SUBMITTED_TO_EDITOR";
    if (status == MangaProposalStatus.NeedsRevision) return "REVISION_REQUESTED";
    if (status == MangaProposalStatus.UnderBoardReview) return "UNDER_BOARD_REVIEW";
    if (status == MangaProposalStatus.Approved) return "APPROVED";
    if (status == MangaProposalStatus.Rejected) return "REJECTED";
    return "DRAFT";
  }

  private MangaProposalDto toDto(SeriesEntity e, String viewerEmail) {
    List<BoardVoteEntity> votes = boardVoteRepository.findBySeries_Id(e.getId());
    int approveVotes = countVotes(votes, "APPROVE");
    int rejectVotes = countVotes(votes, "REJECT");
    int pendingVotes = Math.max(0, BOARD_MEMBERS.length - votes.size());
    String currentVote = null;
    if (viewerEmail != null) {
      for (BoardVoteEntity vote : votes) {
        if (vote.getBoardMember() != null
            && vote.getBoardMember().getEmail() != null
            && normalize(viewerEmail).equals(normalize(vote.getBoardMember().getEmail()))) {
          currentVote = vote.getDecision();
          break;
        }
      }
    }
    MangaProposalDto dto = toDto(e);
    dto.setBoardApproveVotes(Integer.valueOf(approveVotes));
    dto.setBoardRejectVotes(Integer.valueOf(rejectVotes));
    dto.setBoardPendingVotes(Integer.valueOf(pendingVotes));
    dto.setBoardTotalVotes(Integer.valueOf(BOARD_MEMBERS.length));
    dto.setCurrentMemberVote(currentVote);
    return dto;
  }

  private void seed(String id, String title, MangaProposalStatus status) {
    ProposalRecord record =
        new ProposalRecord(
            id,
            title,
            "Action",
            "Teen",
            "Seed synopsis",
            title + " Manuscript",
            "Seed summary",
            "seed-manuscript-" + id + ".pdf",
            Integer.valueOf(1),
            now(),
            normalize("mangaka@manga.local"),
            status,
            status == MangaProposalStatus.Draft ? null : now(),
            now());
    proposals.put(id, record);
  }

  private class ProposalRecord {
    private final String id;
    private String title;
    private String genre;
    private String targetAudience;
    private String synopsis;
    private String manuscriptTitle;
    private String manuscriptSummary;
    private String manuscriptFileName;
    private Integer manuscriptVersion;
    private String manuscriptUploadedAt;
    private final String authorEmail;
    private MangaProposalStatus status;
    private String submittedAt;
    private String updatedAt;
    private String editorEmail;
    private String editorNote;
    private String editorReviewedAt;
    private String boardMemberEmail;
    private String boardDecisionNote;
    private String boardReviewedAt;
    private final Map<String, String> boardVotes = new LinkedHashMap<String, String>();

    private ProposalRecord(
        String id,
        String title,
        String genre,
        String targetAudience,
        String synopsis,
        String manuscriptTitle,
        String manuscriptSummary,
        String manuscriptFileName,
        Integer manuscriptVersion,
        String manuscriptUploadedAt,
        String authorEmail,
        MangaProposalStatus status,
        String submittedAt,
        String updatedAt) {
      this.id = id;
      this.title = title;
      this.genre = genre;
      this.targetAudience = targetAudience;
      this.synopsis = synopsis;
      this.manuscriptTitle = manuscriptTitle;
      this.manuscriptSummary = manuscriptSummary;
      this.manuscriptFileName = manuscriptFileName;
      this.manuscriptVersion = manuscriptVersion;
      this.manuscriptUploadedAt = manuscriptUploadedAt;
      this.authorEmail = authorEmail;
      this.status = status;
      this.submittedAt = submittedAt;
      this.updatedAt = updatedAt;
    }

    private MangaProposalDto toDto() {
      return toDtoFor(null);
    }

    private MangaProposalDto toDtoFor(String viewerEmail) {
      int approveVotes = countVotes(this, "APPROVE");
      int rejectVotes = countVotes(this, "REJECT");
      int pendingVotes = Math.max(0, BOARD_MEMBERS.length - boardVotes.size());
      String currentVote = viewerEmail == null ? null : boardVotes.get(normalize(viewerEmail));
      return new MangaProposalDto(
          id,
          title,
          genre,
          targetAudience,
          synopsis,
          manuscriptTitle,
          manuscriptSummary,
          manuscriptFileName,
          manuscriptVersion,
          manuscriptUploadedAt,
          authorEmail,
          status,
          submittedAt,
          updatedAt,
          editorEmail,
          editorNote,
          editorReviewedAt,
          boardMemberEmail,
          boardDecisionNote,
          boardReviewedAt,
          Integer.valueOf(approveVotes),
          Integer.valueOf(rejectVotes),
          Integer.valueOf(pendingVotes),
          Integer.valueOf(BOARD_MEMBERS.length),
          currentVote);
    }
  }

  private static class ProposalMetadata {
    private String targetAudience;
    private String manuscriptTitle;
    private String manuscriptSummary;
    private String manuscriptFileName;
    private Integer manuscriptVersion;
    private String manuscriptUploadedAt;
  }
}
