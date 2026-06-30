package com.mangastudio.workflow.dtos;

public class SeriesDecisionRequest {
  private String boardMemberEmail;
  private String decisionType;
  private String reason;
  private String newFrequency;
  private String newFormat;

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
}
