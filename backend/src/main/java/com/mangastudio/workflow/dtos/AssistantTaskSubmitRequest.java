package com.mangastudio.workflow.dtos;

public class AssistantTaskSubmitRequest {
  private String assistantEmail;
  private String submittedFileName;
  private String submissionNote;

  public String getAssistantEmail() {
    return assistantEmail;
  }

  public void setAssistantEmail(String assistantEmail) {
    this.assistantEmail = assistantEmail;
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
}
