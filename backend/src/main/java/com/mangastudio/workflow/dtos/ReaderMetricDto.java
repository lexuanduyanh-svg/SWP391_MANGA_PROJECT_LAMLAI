package com.mangastudio.workflow.dtos;

public class ReaderMetricDto {
  private String id;
  private String seriesId;
  private String publicationCycle;
  private String salesFigures;
  private String likesCount;
  private String sharesCount;
  private String votesCount;
  private String createdAt;
  private String updatedAt;

  public ReaderMetricDto() {}
  public ReaderMetricDto(String id, String seriesId, String publicationCycle,
                         String salesFigures, String likesCount, String sharesCount,
                         String votesCount, String createdAt, String updatedAt) {
    this.id = id; this.seriesId = seriesId; this.publicationCycle = publicationCycle;
    this.salesFigures = salesFigures; this.likesCount = likesCount;
    this.sharesCount = sharesCount; this.votesCount = votesCount;
    this.createdAt = createdAt; this.updatedAt = updatedAt;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getSeriesId() { return seriesId; }
  public void setSeriesId(String seriesId) { this.seriesId = seriesId; }
  public String getPublicationCycle() { return publicationCycle; }
  public void setPublicationCycle(String publicationCycle) { this.publicationCycle = publicationCycle; }
  public String getSalesFigures() { return salesFigures; }
  public void setSalesFigures(String salesFigures) { this.salesFigures = salesFigures; }
  public String getLikesCount() { return likesCount; }
  public void setLikesCount(String likesCount) { this.likesCount = likesCount; }
  public String getSharesCount() { return sharesCount; }
  public void setSharesCount(String sharesCount) { this.sharesCount = sharesCount; }
  public String getVotesCount() { return votesCount; }
  public void setVotesCount(String votesCount) { this.votesCount = votesCount; }
  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
  public String getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
