package com.mangastudio.workflow.dtos;

public class ReaderMetricCreateRequest {
  private String publicationCycle;
  private Integer salesFigures;
  private Integer likesCount;
  private Integer sharesCount;
  private Integer votesCount;

  public String getPublicationCycle() { return publicationCycle; }
  public void setPublicationCycle(String publicationCycle) { this.publicationCycle = publicationCycle; }
  public Integer getSalesFigures() { return salesFigures; }
  public void setSalesFigures(Integer salesFigures) { this.salesFigures = salesFigures; }
  public Integer getLikesCount() { return likesCount; }
  public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }
  public Integer getSharesCount() { return sharesCount; }
  public void setSharesCount(Integer sharesCount) { this.sharesCount = sharesCount; }
  public Integer getVotesCount() { return votesCount; }
  public void setVotesCount(Integer votesCount) { this.votesCount = votesCount; }
}
