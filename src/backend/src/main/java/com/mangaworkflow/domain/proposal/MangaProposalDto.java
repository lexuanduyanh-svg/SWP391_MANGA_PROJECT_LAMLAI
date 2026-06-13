package com.mangaworkflow.domain.proposal;

public class MangaProposalDto {
  private String id;
  private String title;
  private String genre;
  private String targetAudience;
  private String synopsis;
  private String manuscriptTitle;
  private String manuscriptSummary;
  private String manuscriptFileName;
  private Integer manuscriptVersion;
  private String manuscriptUploadedAt;
  private String authorEmail;
  private MangaProposalStatus status;
  private String submittedAt;
  private String updatedAt;
  private String editorEmail;
  private String editorNote;
  private String editorReviewedAt;
  private String boardMemberEmail;
  private String boardDecisionNote;
  private String boardReviewedAt;
  private Integer boardApproveVotes;
  private Integer boardRejectVotes;
  private Integer boardPendingVotes;
  private Integer boardTotalVotes;
  private String currentMemberVote;

  public MangaProposalDto() {}

  public MangaProposalDto(
      String id,
      String title,
      String genre,
      String targetAudience,
      String synopsis,
      String manuscriptTitle,
      String manuscriptSummary,
      String manuscriptFileName,
      Integer manuscriptVersion,
      String manuscriptUploadedAt,
      String authorEmail,
      MangaProposalStatus status,
      String submittedAt,
      String updatedAt,
      String editorEmail,
      String editorNote,
      String editorReviewedAt,
      String boardMemberEmail,
      String boardDecisionNote,
      String boardReviewedAt,
      Integer boardApproveVotes,
      Integer boardRejectVotes,
      Integer boardPendingVotes,
      Integer boardTotalVotes,
      String currentMemberVote) {
    this.id = id;
    this.title = title;
    this.genre = genre;
    this.targetAudience = targetAudience;
    this.synopsis = synopsis;
    this.manuscriptTitle = manuscriptTitle;
    this.manuscriptSummary = manuscriptSummary;
    this.manuscriptFileName = manuscriptFileName;
    this.manuscriptVersion = manuscriptVersion;
    this.manuscriptUploadedAt = manuscriptUploadedAt;
    this.authorEmail = authorEmail;
    this.status = status;
    this.submittedAt = submittedAt;
    this.updatedAt = updatedAt;
    this.editorEmail = editorEmail;
    this.editorNote = editorNote;
    this.editorReviewedAt = editorReviewedAt;
    this.boardMemberEmail = boardMemberEmail;
    this.boardDecisionNote = boardDecisionNote;
    this.boardReviewedAt = boardReviewedAt;
    this.boardApproveVotes = boardApproveVotes;
    this.boardRejectVotes = boardRejectVotes;
    this.boardPendingVotes = boardPendingVotes;
    this.boardTotalVotes = boardTotalVotes;
    this.currentMemberVote = currentMemberVote;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public String getTargetAudience() {
    return targetAudience;
  }

  public void setTargetAudience(String targetAudience) {
    this.targetAudience = targetAudience;
  }

  public String getSynopsis() {
    return synopsis;
  }

  public void setSynopsis(String synopsis) {
    this.synopsis = synopsis;
  }

  public String getManuscriptTitle() {
    return manuscriptTitle;
  }

  public void setManuscriptTitle(String manuscriptTitle) {
    this.manuscriptTitle = manuscriptTitle;
  }

  public String getManuscriptSummary() {
    return manuscriptSummary;
  }

  public void setManuscriptSummary(String manuscriptSummary) {
    this.manuscriptSummary = manuscriptSummary;
  }

  public String getManuscriptFileName() {
    return manuscriptFileName;
  }

  public void setManuscriptFileName(String manuscriptFileName) {
    this.manuscriptFileName = manuscriptFileName;
  }

  public Integer getManuscriptVersion() {
    return manuscriptVersion;
  }

  public void setManuscriptVersion(Integer manuscriptVersion) {
    this.manuscriptVersion = manuscriptVersion;
  }

  public String getManuscriptUploadedAt() {
    return manuscriptUploadedAt;
  }

  public void setManuscriptUploadedAt(String manuscriptUploadedAt) {
    this.manuscriptUploadedAt = manuscriptUploadedAt;
  }

  public String getAuthorEmail() {
    return authorEmail;
  }

  public void setAuthorEmail(String authorEmail) {
    this.authorEmail = authorEmail;
  }

  public MangaProposalStatus getStatus() {
    return status;
  }

  public void setStatus(MangaProposalStatus status) {
    this.status = status;
  }

  public String getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(String submittedAt) {
    this.submittedAt = submittedAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getEditorEmail() {
    return editorEmail;
  }

  public void setEditorEmail(String editorEmail) {
    this.editorEmail = editorEmail;
  }

  public String getEditorNote() {
    return editorNote;
  }

  public void setEditorNote(String editorNote) {
    this.editorNote = editorNote;
  }

  public String getEditorReviewedAt() {
    return editorReviewedAt;
  }

  public void setEditorReviewedAt(String editorReviewedAt) {
    this.editorReviewedAt = editorReviewedAt;
  }

  public String getBoardMemberEmail() {
    return boardMemberEmail;
  }

  public void setBoardMemberEmail(String boardMemberEmail) {
    this.boardMemberEmail = boardMemberEmail;
  }

  public String getBoardDecisionNote() {
    return boardDecisionNote;
  }

  public void setBoardDecisionNote(String boardDecisionNote) {
    this.boardDecisionNote = boardDecisionNote;
  }

  public String getBoardReviewedAt() {
    return boardReviewedAt;
  }

  public void setBoardReviewedAt(String boardReviewedAt) {
    this.boardReviewedAt = boardReviewedAt;
  }

  public Integer getBoardApproveVotes() {
    return boardApproveVotes;
  }

  public void setBoardApproveVotes(Integer boardApproveVotes) {
    this.boardApproveVotes = boardApproveVotes;
  }

  public Integer getBoardRejectVotes() {
    return boardRejectVotes;
  }

  public void setBoardRejectVotes(Integer boardRejectVotes) {
    this.boardRejectVotes = boardRejectVotes;
  }

  public Integer getBoardPendingVotes() {
    return boardPendingVotes;
  }

  public void setBoardPendingVotes(Integer boardPendingVotes) {
    this.boardPendingVotes = boardPendingVotes;
  }

  public Integer getBoardTotalVotes() {
    return boardTotalVotes;
  }

  public void setBoardTotalVotes(Integer boardTotalVotes) {
    this.boardTotalVotes = boardTotalVotes;
  }

  public String getCurrentMemberVote() {
    return currentMemberVote;
  }

  public void setCurrentMemberVote(String currentMemberVote) {
    this.currentMemberVote = currentMemberVote;
  }
}
