package com.mangastudio.workflow.dtos;

public class MangakaProductionTaskCreateRequest {
  private String assistantEmail;
  private String instructions;
  private String deadline;

  public String getAssistantEmail() {
    return assistantEmail;
  }

  public void setAssistantEmail(String assistantEmail) {
    this.assistantEmail = assistantEmail;
  }

  public String getInstructions() {
    return instructions;
  }

  public void setInstructions(String instructions) {
    this.instructions = instructions;
  }

  public String getDeadline() {
    return deadline;
  }

  public void setDeadline(String deadline) {
    this.deadline = deadline;
  }
}
