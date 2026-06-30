package com.mangastudio.workflow.dtos;

public class MangakaProductionTaskDto {
  private String id;
  private String pageId;
  private String assistantEmail;
  private String instructions;
  private String deadline;
  private String pageFileName;
  private MangakaTaskStatus status;
  private String createdAt;
  private String updatedAt;
  private String submittedFileName;
  private String submissionNote;
  private String submittedAt;

  public MangakaProductionTaskDto() {}

  public MangakaProductionTaskDto(
      String id,
      String pageId,
      String assistantEmail,
      String instructions,
      String deadline,
      String pageFileName,
      MangakaTaskStatus status,
      String createdAt,
      String updatedAt,
      String submittedFileName,
      String submissionNote,
      String submittedAt) {
    this.id = id;
    this.pageId = pageId;
    this.assistantEmail = assistantEmail;
    this.instructions = instructions;
    this.deadline = deadline;
    this.pageFileName = pageFileName;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.submittedFileName = submittedFileName;
    this.submissionNote = submissionNote;
    this.submittedAt = submittedAt;
  }

  public String getId() { return id; }
  public String getPageId() { return pageId; }
  public String getAssistantEmail() { return assistantEmail; }
  public String getInstructions() { return instructions; }
  public String getDeadline() { return deadline; }
  public String getPageFileName() { return pageFileName; }
  public MangakaTaskStatus getStatus() { return status; }
  public String getCreatedAt() { return createdAt; }
  public String getUpdatedAt() { return updatedAt; }
  public String getSubmittedFileName() { return submittedFileName; }
  public String getSubmissionNote() { return submissionNote; }
  public String getSubmittedAt() { return submittedAt; }
}
