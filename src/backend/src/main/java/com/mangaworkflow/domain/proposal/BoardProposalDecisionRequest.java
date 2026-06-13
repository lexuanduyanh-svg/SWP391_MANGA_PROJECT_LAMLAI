package com.mangaworkflow.domain.proposal;

public class BoardProposalDecisionRequest {
  private String memberEmail;
  private String note;

  public String getMemberEmail() {
    return memberEmail;
  }

  public void setMemberEmail(String memberEmail) {
    this.memberEmail = memberEmail;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
