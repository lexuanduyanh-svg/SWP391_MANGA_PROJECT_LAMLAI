package com.mangaworkflow.api.domain.proposal;

public class EditorProposalReviewRequest {
  private String editorEmail;
  private String note;

  public String getEditorEmail() {
    return editorEmail;
  }

  public void setEditorEmail(String editorEmail) {
    this.editorEmail = editorEmail;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
