package com.mangastudio.workflow.dtos;

public class MangakaProductionTaskCreateRequest {
  private String assistantEmail;
  private String taskType;
  private String instructions;
  private String referenceFileName;

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
}
