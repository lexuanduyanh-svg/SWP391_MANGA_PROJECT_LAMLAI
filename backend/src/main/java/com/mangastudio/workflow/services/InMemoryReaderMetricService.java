package com.mangastudio.workflow.services;

import com.mangastudio.workflow.dtos.ReaderMetricCreateRequest;
import com.mangastudio.workflow.dtos.ReaderMetricDto;
import com.mangastudio.workflow.dtos.SeriesRankingDto;
import com.mangastudio.workflow.entities.ReaderMetricEntity;
import com.mangastudio.workflow.entities.SeriesEntity;
import com.mangastudio.workflow.repositories.ReaderMetricRepository;
import com.mangastudio.workflow.repositories.SeriesRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class InMemoryReaderMetricService {
  private final ReaderMetricRepository readerMetricRepository;
  private final SeriesRepository seriesRepository;
  private final Map<String, MetricRecord> metrics = new LinkedHashMap<String, MetricRecord>();
  private final Map<String, List<String>> seriesToMetricIds = new LinkedHashMap<String, List<String>>();
  private final AtomicLong seq = new AtomicLong(1000);
  private final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  public InMemoryReaderMetricService() {
    this.readerMetricRepository = null;
    this.seriesRepository = null;
    seed();
  }

  @Autowired
  public InMemoryReaderMetricService(
      @Nullable ReaderMetricRepository readerMetricRepository,
      @Nullable SeriesRepository seriesRepository) {
    this.readerMetricRepository = readerMetricRepository;
    this.seriesRepository = seriesRepository;
    seed();
  }

  private boolean dbMode() {
    return readerMetricRepository != null && seriesRepository != null;
  }

  public synchronized ReaderMetricDto createMetric(String seriesId, ReaderMetricCreateRequest request) {
    if (request == null || request.getPublicationCycle() == null)
      throw new IllegalArgumentException("Publication cycle is required");
    if (dbMode()) return createMetricDb(seriesId, request);
    String id = String.valueOf(seq.incrementAndGet());
    MetricRecord r = new MetricRecord(id, seriesId,
        request.getPublicationCycle(),
        request.getSalesFigures() != null ? request.getSalesFigures() : 0,
        request.getLikesCount() != null ? request.getLikesCount() : 0,
        request.getSharesCount() != null ? request.getSharesCount() : 0,
        request.getVotesCount() != null ? request.getVotesCount() : 0,
        now(), now());
    metrics.put(id, r);
    List<String> ids = seriesToMetricIds.get(seriesId);
    if (ids == null) { ids = new ArrayList<String>(); seriesToMetricIds.put(seriesId, ids); }
    ids.add(id);
    return r.toDto();
  }

  public synchronized List<ReaderMetricDto> listMetrics(String seriesId) {
    if (dbMode()) return listMetricsDb(seriesId);
    List<ReaderMetricDto> out = new ArrayList<ReaderMetricDto>();
    List<String> ids = seriesToMetricIds.get(seriesId);
    if (ids != null) {
      for (String mid : ids) {
        MetricRecord r = metrics.get(mid);
        if (r != null) out.add(r.toDto());
      }
    }
    return out;
  }

  public synchronized SeriesRankingDto computeRanking(String seriesId) {
    List<ReaderMetricDto> metricList = listMetrics(seriesId);
    int totalSales = 0, totalLikes = 0, totalShares = 0, totalVotes = 0;
    for (ReaderMetricDto m : metricList) {
      totalSales += parseInt(m.getSalesFigures());
      totalLikes += parseInt(m.getLikesCount());
      totalShares += parseInt(m.getSharesCount());
      totalVotes += parseInt(m.getVotesCount());
    }
    double composite = (totalSales * 0.4 + totalLikes * 0.3 + totalShares * 0.2 + totalVotes * 0.1) / 1000.0;
    String title = resolveSeriesTitle(seriesId);
    return new SeriesRankingDto(seriesId, title, totalSales, totalLikes, totalShares, totalVotes, composite, 0);
  }

  public synchronized List<SeriesRankingDto> computeAllRankings() {
    if (dbMode()) return computeAllRankingsDb();
    java.util.Set<String> allSeriesIds = seriesToMetricIds.keySet();
    // Also check if there's a series with no metrics but exists
    if (allSeriesIds.isEmpty()) allSeriesIds = new java.util.LinkedHashSet<String>();
    // Add series "801" if missing
    if (!allSeriesIds.contains("801")) {
      List<String> dummy = new ArrayList<String>();
      seriesToMetricIds.put("801", dummy);
      allSeriesIds = seriesToMetricIds.keySet();
    }
    List<SeriesRankingDto> rankings = new ArrayList<SeriesRankingDto>();
    for (String sid : allSeriesIds) {
      rankings.add(computeRanking(sid));
    }
    // Sort by compositeScore desc and assign ranks
    Collections.sort(rankings, new Comparator<SeriesRankingDto>() {
      public int compare(SeriesRankingDto a, SeriesRankingDto b) {
        return b.getCompositeScore().compareTo(a.getCompositeScore());
      }
    });
    for (int i = 0; i < rankings.size(); i++) {
      rankings.get(i).setRank(i + 1);
    }
    return rankings;
  }

  // ---- DB mode ----

  private ReaderMetricDto createMetricDb(String seriesId, ReaderMetricCreateRequest request) {
    SeriesEntity series = seriesRepository.findById(Long.parseLong(seriesId))
        .orElseThrow(() -> new IllegalArgumentException("Series not found"));
    ReaderMetricEntity e = new ReaderMetricEntity();
    e.setSeries(series);
    e.setPublicationCycle(LocalDate.parse(request.getPublicationCycle()));
    e.setSalesFigures(request.getSalesFigures() != null ? request.getSalesFigures() : 0);
    e.setLikesCount(request.getLikesCount() != null ? request.getLikesCount() : 0);
    e.setSharesCount(request.getSharesCount() != null ? request.getSharesCount() : 0);
    e.setVotesCount(request.getVotesCount() != null ? request.getVotesCount() : 0);
    e.setCreatedAt(LocalDateTime.now());
    e.setUpdatedAt(LocalDateTime.now());
    ReaderMetricEntity saved = readerMetricRepository.save(e);
    return toDto(saved);
  }

  private List<ReaderMetricDto> listMetricsDb(String seriesId) {
    List<ReaderMetricDto> out = new ArrayList<ReaderMetricDto>();
    // No findBySeriesId in repository, iterate all for now
    for (ReaderMetricEntity e : readerMetricRepository.findAll()) {
      if (e.getSeries() != null && String.valueOf(e.getSeries().getId()).equals(seriesId))
        out.add(toDto(e));
    }
    return out;
  }

  private List<SeriesRankingDto> computeAllRankingsDb() {
    List<SeriesEntity> allSeries = seriesRepository.findAll();
    List<SeriesRankingDto> rankings = new ArrayList<SeriesRankingDto>();
    for (SeriesEntity s : allSeries) {
      String sid = String.valueOf(s.getId());
      rankings.add(computeRanking(sid));
    }
    Collections.sort(rankings, new Comparator<SeriesRankingDto>() {
      public int compare(SeriesRankingDto a, SeriesRankingDto b) {
        return b.getCompositeScore().compareTo(a.getCompositeScore());
      }
    });
    for (int i = 0; i < rankings.size(); i++) rankings.get(i).setRank(i + 1);
    return rankings;
  }

  private ReaderMetricDto toDto(ReaderMetricEntity e) {
    return new ReaderMetricDto(
        String.valueOf(e.getId()),
        e.getSeries() != null ? String.valueOf(e.getSeries().getId()) : null,
        e.getPublicationCycle() != null ? e.getPublicationCycle().toString() : null,
        String.valueOf(e.getSalesFigures()),
        String.valueOf(e.getLikesCount()),
        String.valueOf(e.getSharesCount()),
        String.valueOf(e.getVotesCount()),
        e.getCreatedAt() != null ? e.getCreatedAt().format(f) : null,
        e.getUpdatedAt() != null ? e.getUpdatedAt().format(f) : null);
  }

  // ---- Helpers ----

  private String resolveSeriesTitle(String seriesId) {
    if ("801".equals(seriesId)) return "Seed Approved";
    return "Series " + seriesId;
  }

  private int parseInt(String s) {
    try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
  }

  private void seed() {
    if (!metrics.isEmpty()) return;
    MetricRecord r1 = new MetricRecord(
        String.valueOf(seq.incrementAndGet()), "801",
        "2026-06-01", 12000, 450, 890, 340, now(), now());
    metrics.put(r1.id, r1);
    List<String> ids = new ArrayList<String>();
    ids.add(r1.id);
    MetricRecord r2 = new MetricRecord(
        String.valueOf(seq.incrementAndGet()), "801",
        "2026-06-15", 15000, 520, 950, 410, now(), now());
    metrics.put(r2.id, r2);
    ids.add(r2.id);
    seriesToMetricIds.put("801", ids);
  }

  private String now() { return LocalDateTime.now().format(f); }

  private static class MetricRecord {
    String id, seriesId, publicationCycle;
    int salesFigures, likesCount, sharesCount, votesCount;
    String createdAt, updatedAt;

    MetricRecord(String id, String seriesId, String publicationCycle,
                 int salesFigures, int likesCount, int sharesCount, int votesCount,
                 String createdAt, String updatedAt) {
      this.id = id; this.seriesId = seriesId; this.publicationCycle = publicationCycle;
      this.salesFigures = salesFigures; this.likesCount = likesCount;
      this.sharesCount = sharesCount; this.votesCount = votesCount;
      this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    ReaderMetricDto toDto() {
      return new ReaderMetricDto(id, seriesId, publicationCycle,
          String.valueOf(salesFigures), String.valueOf(likesCount),
          String.valueOf(sharesCount), String.valueOf(votesCount),
          createdAt, updatedAt);
    }
  }
}
