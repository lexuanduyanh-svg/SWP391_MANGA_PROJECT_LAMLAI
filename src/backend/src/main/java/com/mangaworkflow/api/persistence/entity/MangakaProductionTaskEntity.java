package com.mangaworkflow.api.persistence.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mangaka_production_tasks")
public class MangakaProductionTaskEntity {
  @Id private Long id;
  private Long regionId;
  private String assistantEmail;
  private String taskType;
  private String instructions;
  private String referenceFileName;
  private String status;
  private String createdAt;
  private String updatedAt;
  private String submittedFileName;
  private String submissionNote;
  private String submittedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getRegionId() {
    return regionId;
  }

  public void setRegionId(Long regionId) {
    this.regionId = regionId;
  }

  public String getAssistantEmail() {
    return assistantEmail;
  }

  public void setAssistantEmail(String assistantEmail) {
    this.assistantEmail = assistantEmail;
  }

  public String getTaskType() {
    return taskType;
  }

  public void setTaskType(String taskType) {
    this.taskType = taskType;
  }

  public String getInstructions() {
    return instructions;
  }

  public void setInstructions(String instructions) {
    this.instructions = instructions;
  }

  public String getReferenceFileName() {
    return referenceFileName;
  }

  public void setReferenceFileName(String referenceFileName) {
    this.referenceFileName = referenceFileName;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getSubmittedFileName() {
    return submittedFileName;
  }

  public void setSubmittedFileName(String submittedFileName) {
    this.submittedFileName = submittedFileName;
  }

  public String getSubmissionNote() {
    return submissionNote;
  }

  public void setSubmissionNote(String submissionNote) {
    this.submissionNote = submissionNote;
  }

  public String getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(String submittedAt) {
    this.submittedAt = submittedAt;
  }
}
