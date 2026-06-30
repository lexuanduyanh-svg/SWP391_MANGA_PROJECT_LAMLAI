package com.mangastudio.workflow.dtos;

public class SeriesRankingDto {
  private String seriesId;
  private String seriesTitle;
  private Integer totalSales;
  private Integer totalLikes;
  private Integer totalShares;
  private Integer totalVotes;
  private Double compositeScore;
  private Integer rank;

  public SeriesRankingDto() {}
  public SeriesRankingDto(String seriesId, String seriesTitle, Integer totalSales,
                          Integer totalLikes, Integer totalShares, Integer totalVotes,
                          Double compositeScore, Integer rank) {
    this.seriesId = seriesId; this.seriesTitle = seriesTitle;
    this.totalSales = totalSales; this.totalLikes = totalLikes;
    this.totalShares = totalShares; this.totalVotes = totalVotes;
    this.compositeScore = compositeScore; this.rank = rank;
  }

  public String getSeriesId() { return seriesId; }
  public void setSeriesId(String seriesId) { this.seriesId = seriesId; }
  public String getSeriesTitle() { return seriesTitle; }
  public void setSeriesTitle(String seriesTitle) { this.seriesTitle = seriesTitle; }
  public Integer getTotalSales() { return totalSales; }
  public void setTotalSales(Integer totalSales) { this.totalSales = totalSales; }
  public Integer getTotalLikes() { return totalLikes; }
  public void setTotalLikes(Integer totalLikes) { this.totalLikes = totalLikes; }
  public Integer getTotalShares() { return totalShares; }
  public void setTotalShares(Integer totalShares) { this.totalShares = totalShares; }
  public Integer getTotalVotes() { return totalVotes; }
  public void setTotalVotes(Integer totalVotes) { this.totalVotes = totalVotes; }
  public Double getCompositeScore() { return compositeScore; }
  public void setCompositeScore(Double compositeScore) { this.compositeScore = compositeScore; }
  public Integer getRank() { return rank; }
  public void setRank(Integer rank) { this.rank = rank; }
}
