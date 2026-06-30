package com.mangastudio.workflow.services;

import com.mangastudio.workflow.dtos.SeriesDecisionDto;
import com.mangastudio.workflow.dtos.SeriesDecisionRequest;
import com.mangastudio.workflow.entities.SeriesDecisionEntity;
import com.mangastudio.workflow.entities.SeriesEntity;
import com.mangastudio.workflow.entities.UserEntity;
import com.mangastudio.workflow.repositories.SeriesDecisionRepository;
import com.mangastudio.workflow.repositories.SeriesRepository;
import com.mangastudio.workflow.repositories.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class InMemorySeriesDecisionService {
  private final SeriesDecisionRepository seriesDecisionRepository;
  private final SeriesRepository seriesRepository;
  private final UserRepository userRepository;
  private final InMemoryMangaProposalService proposals;
  private final Map<String, DecisionRecord> decisions = new LinkedHashMap<String, DecisionRecord>();
  private final AtomicLong seq = new AtomicLong(1100);
  private final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  private static final List<String> BOARD_MEMBERS = Arrays.asList(
      "board@manga.local", "board2@manga.local", "board3@manga.local");
  private static final List<String> VALID_TYPES = Arrays.asList(
      "MAINTAIN", "RESCHEDULE", "CANCEL", "CHANGE_FORMAT");

  public InMemorySeriesDecisionService(InMemoryMangaProposalService proposals) {
    this.seriesDecisionRepository = null;
    this.seriesRepository = null;
    this.userRepository = null;
    this.proposals = proposals;
    seed();
  }

  @Autowired
  public InMemorySeriesDecisionService(
      @Nullable SeriesDecisionRepository seriesDecisionRepository,
      @Nullable SeriesRepository seriesRepository,
      @Nullable UserRepository userRepository,
      InMemoryMangaProposalService proposals) {
    this.seriesDecisionRepository = seriesDecisionRepository;
    this.seriesRepository = seriesRepository;
    this.userRepository = userRepository;
    this.proposals = proposals;
    seed();
  }

  private boolean dbMode() {
    return seriesDecisionRepository != null && seriesRepository != null && userRepository != null;
  }

  public synchronized SeriesDecisionDto makeDecision(String seriesId, SeriesDecisionRequest request) {
    if (request == null || request.getBoardMemberEmail() == null)
      throw new IllegalArgumentException("Board member email is required");
    if (request.getDecisionType() == null)
      throw new IllegalArgumentException("Decision type is required");
    if (!VALID_TYPES.contains(request.getDecisionType()))
      throw new IllegalArgumentException("Decision type must be one of: " + VALID_TYPES);
    if (!BOARD_MEMBERS.contains(normalize(request.getBoardMemberEmail())))
      throw new IllegalArgumentException("Only editorial board members can make series decisions");

    // Verify series exists
    if (dbMode()) {
      seriesRepository.findById(Long.parseLong(seriesId))
          .orElseThrow(() -> new IllegalArgumentException("Series not found"));
    } else {
      if (proposals.getSeriesRecord(seriesId) == null && !"801".equals(seriesId))
        throw new IllegalArgumentException("Series not found");
    }

    if (dbMode()) return makeDecisionDb(seriesId, request);

    String id = String.valueOf(seq.incrementAndGet());
    String title = resolveSeriesTitle(seriesId);
    DecisionRecord r = new DecisionRecord(id, seriesId, title,
        normalize(request.getBoardMemberEmail()),
        request.getDecisionType(), request.getReason(),
        request.getNewFrequency(), request.getNewFormat(), now());
    decisions.put(id, r);
    return r.toDto();
  }

  public synchronized List<SeriesDecisionDto> listDecisions(String seriesId) {
    if (dbMode()) return listDecisionsDb(seriesId);
    List<SeriesDecisionDto> out = new ArrayList<SeriesDecisionDto>();
    for (DecisionRecord r : decisions.values())
      if (seriesId.equals(r.seriesId)) out.add(r.toDto());
    return out;
  }

  public synchronized List<SeriesDecisionDto> listAllDecisions() {
    if (dbMode()) return listAllDecisionsDb();
    List<SeriesDecisionDto> out = new ArrayList<SeriesDecisionDto>();
    for (DecisionRecord r : decisions.values()) out.add(r.toDto());
    return out;
  }

  // ---- DB mode ----

  private SeriesDecisionDto makeDecisionDb(String seriesId, SeriesDecisionRequest request) {
    SeriesEntity series = seriesRepository.findById(Long.parseLong(seriesId))
        .orElseThrow(() -> new IllegalArgumentException("Series not found"));
    UserEntity member = userRepository.findByEmailIgnoreCase(normalize(request.getBoardMemberEmail()))
        .orElseThrow(() -> new IllegalArgumentException("Board member not found"));
    SeriesDecisionEntity e = new SeriesDecisionEntity();
    e.setSeries(series);
    e.setBoardMember(member);
    e.setDecisionType(request.getDecisionType());
    e.setReason(request.getReason());
    e.setNewFrequency(request.getNewFrequency());
    e.setNewFormat(request.getNewFormat());
    e.setDecidedAt(LocalDateTime.now());
    return toDto(seriesDecisionRepository.save(e));
  }

  private List<SeriesDecisionDto> listDecisionsDb(String seriesId) {
    List<SeriesDecisionDto> out = new ArrayList<SeriesDecisionDto>();
    for (SeriesDecisionEntity e :
        seriesDecisionRepository.findBySeries_IdOrderByDecidedAtDesc(Long.parseLong(seriesId)))
      out.add(toDto(e));
    return out;
  }

  private List<SeriesDecisionDto> listAllDecisionsDb() {
    List<SeriesDecisionDto> out = new ArrayList<SeriesDecisionDto>();
    for (SeriesDecisionEntity e : seriesDecisionRepository.findAllByOrderByDecidedAtDesc())
      out.add(toDto(e));
    return out;
  }

  private SeriesDecisionDto toDto(SeriesDecisionEntity e) {
    String title = e.getSeries() != null ? e.getSeries().getTitle() : null;
    return new SeriesDecisionDto(
        String.valueOf(e.getId()),
        e.getSeries() != null ? String.valueOf(e.getSeries().getId()) : null,
        title,
        e.getBoardMember() != null ? e.getBoardMember().getEmail() : null,
        e.getDecisionType(), e.getReason(),
        e.getNewFrequency(), e.getNewFormat(),
        e.getDecidedAt() != null ? e.getDecidedAt().format(f) : null);
  }

  // ---- Helpers ----

  private String resolveSeriesTitle(String seriesId) {
    com.mangastudio.workflow.services.InMemoryMangaProposalService.SeriesRecord sr = proposals.getSeriesRecord(seriesId);
    if (sr != null) return sr.title;
    if ("801".equals(seriesId)) return "Seed Approved";
    return "Series " + seriesId;
  }

  private String normalize(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private void seed() {
    if (!decisions.isEmpty()) return;
    DecisionRecord r = new DecisionRecord(
        String.valueOf(seq.incrementAndGet()), "801", "Seed Approved",
        "board2@manga.local", "MAINTAIN",
        "Series is performing well. Keep current schedule.",
        null, null, now());
    decisions.put(r.id, r);
  }

  private String now() { return LocalDateTime.now().format(f); }

  private static class DecisionRecord {
    String id, seriesId, seriesTitle, boardMemberEmail, decisionType, reason,
           newFrequency, newFormat, decidedAt;

    DecisionRecord(String id, String seriesId, String seriesTitle, String boardMemberEmail,
                   String decisionType, String reason, String newFrequency,
                   String newFormat, String decidedAt) {
      this.id = id; this.seriesId = seriesId; this.seriesTitle = seriesTitle;
      this.boardMemberEmail = boardMemberEmail; this.decisionType = decisionType;
      this.reason = reason; this.newFrequency = newFrequency;
      this.newFormat = newFormat; this.decidedAt = decidedAt;
    }

    SeriesDecisionDto toDto() {
      return new SeriesDecisionDto(id, seriesId, seriesTitle, boardMemberEmail,
          decisionType, reason, newFrequency, newFormat, decidedAt);
    }
  }
}
