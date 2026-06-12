package com.mangaworkflow.api.domain.task;

import com.mangaworkflow.api.domain.production.MangakaTaskStatus;

public class AssistantTaskDto {
  private String id;
  private String proposalId;
  private String chapterId;
  private String chapterTitle;
  private String pageId;
  private int pageNumber;
  private String pageFileName;
  private String regionId;
  private String regionType;
  private String regionNote;
  private String assistantEmail;
  private String taskType;
  private String instructions;
  private String referenceFileName;
  private MangakaTaskStatus status;
  private String submittedFileName;
  private String submissionNote;
  private String createdAt;
  private String updatedAt;
  private String submittedAt;

  public AssistantTaskDto() {}

  public AssistantTaskDto(
      String id,
      String proposalId,
      String chapterId,
      String chapterTitle,
      String pageId,
      int pageNumber,
      String pageFileName,
      String regionId,
      String regionType,
      String regionNote,
      String assistantEmail,
      String taskType,
      String instructions,
      String referenceFileName,
      MangakaTaskStatus status,
      String submittedFileName,
      String submissionNote,
      String createdAt,
      String updatedAt,
      String submittedAt) {
    this.id = id;
    this.proposalId = proposalId;
    this.chapterId = chapterId;
    this.chapterTitle = chapterTitle;
    this.pageId = pageId;
    this.pageNumber = pageNumber;
    this.pageFileName = pageFileName;
    this.regionId = regionId;
    this.regionType = regionType;
    this.regionNote = regionNote;
    this.assistantEmail = assistantEmail;
    this.taskType = taskType;
    this.instructions = instructions;
    this.referenceFileName = referenceFileName;
    this.status = status;
    this.submittedFileName = submittedFileName;
    this.submissionNote = submissionNote;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.submittedAt = submittedAt;
  }

  public String getId() {
    return id;
  }

  public String getProposalId() {
    return proposalId;
  }

  public String getChapterId() {
    return chapterId;
  }

  public String getChapterTitle() {
    return chapterTitle;
  }

  public String getPageId() {
    return pageId;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public String getPageFileName() {
    return pageFileName;
  }

  public String getRegionId() {
    return regionId;
  }

  public String getRegionType() {
    return regionType;
  }

  public String getRegionNote() {
    return regionNote;
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

  public String getSubmittedFileName() {
    return submittedFileName;
  }

  public String getSubmissionNote() {
    return submissionNote;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public String getSubmittedAt() {
    return submittedAt;
  }
}
