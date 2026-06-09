package com.mangaworkflow.api.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "manga_proposals")
public class MangaProposalEntity {
  @Id private Long id;
  private String title;
  private String genre;
  private String targetAudience;

  @Column(length = 4000)
  private String synopsis;

  private String manuscriptTitle;

  @Column(length = 4000)
  private String manuscriptSummary;

  private String manuscriptFileName;
  private Integer manuscriptVersion;
  private String manuscriptUploadedAt;
  private String authorEmail;
  private String status;
  private String submittedAt;
  private String updatedAt;
  private String editorEmail;

  @Column(length = 4000)
  private String editorNote;

  private String editorReviewedAt;
  private String boardMemberEmail;

  @Column(length = 4000)
  private String boardDecisionNote;

  private String boardReviewedAt;

  @Column(length = 4000)
  private String boardVotes;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
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

  public String getBoardVotes() {
    return boardVotes;
  }

  public void setBoardVotes(String boardVotes) {
    this.boardVotes = boardVotes;
  }
}
