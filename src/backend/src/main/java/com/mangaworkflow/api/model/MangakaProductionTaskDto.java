package com.mangaworkflow.api.model;

public class MangakaProductionTaskDto {
  private String id;
  private String regionId;
  private String assistantEmail;
  private String taskType;
  private String instructions;
  private String referenceFileName;
  private MangakaTaskStatus status;
  private String createdAt;
  private String updatedAt;
  private String submittedFileName;
  private String submissionNote;
  private String submittedAt;

  public MangakaProductionTaskDto() {}

  public MangakaProductionTaskDto(
      String id,
      String regionId,
      String assistantEmail,
      String taskType,
      String instructions,
      String referenceFileName,
      MangakaTaskStatus status,
      String createdAt,
      String updatedAt,
      String submittedFileName,
      String submissionNote,
      String submittedAt) {
    this.id = id;
    this.regionId = regionId;
    this.assistantEmail = assistantEmail;
    this.taskType = taskType;
    this.instructions = instructions;
    this.referenceFileName = referenceFileName;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.submittedFileName = submittedFileName;
    this.submissionNote = submissionNote;
    this.submittedAt = submittedAt;
  }

  public String getId() {
    return id;
  }

  public String getRegionId() {
    return regionId;
  }

  public String getAssistantEmail() {
    return assistantEmail;
  }

  public String getTaskType() {
    return taskType;
  }

  public String getInstructions() {
    return instructions;
  }

  public String getReferenceFileName() {
    return referenceFileName;
  }

  public MangakaTaskStatus getStatus() {
    return status;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public String getSubmittedFileName() {
    return submittedFileName;
  }

  public String getSubmissionNote() {
    return submissionNote;
  }

  public String getSubmittedAt() {
    return submittedAt;
  }
}
