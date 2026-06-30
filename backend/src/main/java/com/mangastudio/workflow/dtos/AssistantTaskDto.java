package com.mangastudio.workflow.dtos;

public class AssistantTaskDto {
  private String id;
  private String seriesId;
  private String seriesTitle;
  private String chapterId;
  private String chapterTitle;
  private String pageId;
  private int pageNumber;
  private String pageFileName;
  private String assistantEmail;
  private String instructions;
  private String deadline;
  private MangakaTaskStatus status;
  private String submittedFileName;
  private String submissionNote;
  private String createdAt;
  private String updatedAt;
  private String submittedAt;
  private String regionId;
  private String regionType;
  private String regionNote;
  private String proposalId;
  private String proposalTitle;

  public AssistantTaskDto() {}

  public AssistantTaskDto(
      String id,
      String seriesId,
      String seriesTitle,
      String chapterId,
      String chapterTitle,
      String pageId,
      int pageNumber,
      String pageFileName,
      String assistantEmail,
      String instructions,
      String deadline,
      MangakaTaskStatus status,
      String submittedFileName,
      String submissionNote,
      String createdAt,
      String updatedAt,
      String submittedAt) {
    this.id = id;
    this.seriesId = seriesId;
    this.seriesTitle = seriesTitle;
    this.chapterId = chapterId;
    this.chapterTitle = chapterTitle;
    this.pageId = pageId;
    this.pageNumber = pageNumber;
    this.pageFileName = pageFileName;
    this.assistantEmail = assistantEmail;
    this.instructions = instructions;
    this.deadline = deadline;
    this.status = status;
    this.submittedFileName = submittedFileName;
    this.submissionNote = submissionNote;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.submittedAt = submittedAt;
  }

  public String getId() { return id; }
  public String getSeriesId() { return seriesId; }
  public String getSeriesTitle() { return seriesTitle; }
  public String getChapterId() { return chapterId; }
  public String getChapterTitle() { return chapterTitle; }
  public String getPageId() { return pageId; }
  public int getPageNumber() { return pageNumber; }
  public String getPageFileName() { return pageFileName; }
  public String getAssistantEmail() { return assistantEmail; }
  public String getInstructions() { return instructions; }
  public String getDeadline() { return deadline; }
  public MangakaTaskStatus getStatus() { return status; }
  public String getSubmittedFileName() { return submittedFileName; }
  public String getSubmissionNote() { return submissionNote; }
  public String getCreatedAt() { return createdAt; }
  public String getUpdatedAt() { return updatedAt; }
  public String getSubmittedAt() { return submittedAt; }
  public String getRegionId() { return regionId; }
  public void setRegionId(String regionId) { this.regionId = regionId; }
  public String getRegionType() { return regionType; }
  public void setRegionType(String regionType) { this.regionType = regionType; }
  public String getRegionNote() { return regionNote; }
  public void setRegionNote(String regionNote) { this.regionNote = regionNote; }
  public String getProposalId() { return proposalId; }
  public void setProposalId(String proposalId) { this.proposalId = proposalId; }
  public String getProposalTitle() { return proposalTitle; }
  public void setProposalTitle(String proposalTitle) { this.proposalTitle = proposalTitle; }
}
