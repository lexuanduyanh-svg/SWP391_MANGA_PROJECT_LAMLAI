package com.mangastudio.workflow.dtos;

public class SeriesDecisionDto {
  private String id;
  private String seriesId;
  private String seriesTitle;
  private String boardMemberEmail;
  private String decisionType;
  private String reason;
  private String newFrequency;
  private String newFormat;
  private String decidedAt;

  public SeriesDecisionDto() {}
  public SeriesDecisionDto(String id, String seriesId, String seriesTitle, String boardMemberEmail,
                          String decisionType, String reason, String newFrequency,
                          String newFormat, String decidedAt) {
    this.id = id; this.seriesId = seriesId; this.seriesTitle = seriesTitle;
    this.boardMemberEmail = boardMemberEmail; this.decisionType = decisionType;
    this.reason = reason; this.newFrequency = newFrequency;
    this.newFormat = newFormat; this.decidedAt = decidedAt;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getSeriesId() { return seriesId; }
  public void setSeriesId(String seriesId) { this.seriesId = seriesId; }
  public String getSeriesTitle() { return seriesTitle; }
  public void setSeriesTitle(String seriesTitle) { this.seriesTitle = seriesTitle; }
  public String getBoardMemberEmail() { return boardMemberEmail; }
  public void setBoardMemberEmail(String boardMemberEmail) { this.boardMemberEmail = boardMemberEmail; }
  public String getDecisionType() { return decisionType; }
  public void setDecisionType(String decisionType) { this.decisionType = decisionType; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
  public String getNewFrequency() { return newFrequency; }
  public void setNewFrequency(String newFrequency) { this.newFrequency = newFrequency; }
  public String getNewFormat() { return newFormat; }
  public void setNewFormat(String newFormat) { this.newFormat = newFormat; }
  public String getDecidedAt() { return decidedAt; }
  public void setDecidedAt(String decidedAt) { this.decidedAt = decidedAt; }
}
