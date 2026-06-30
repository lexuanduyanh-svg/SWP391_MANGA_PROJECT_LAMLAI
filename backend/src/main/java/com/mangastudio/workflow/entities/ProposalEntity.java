package com.mangastudio.workflow.entities;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "proposals")
public class ProposalEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "proposal_id")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "mangaka_id")
  private UserEntity mangaka;

  @ManyToOne
  @JoinColumn(name = "tantou_editor_id")
  private UserEntity tantouEditor;

  @Column(nullable = false)
  private String title;

  private String genre;

  @Column(name = "target_audience")
  private String targetAudience;

  @Column(columnDefinition = "text")
  private String synopsis;

  @Column(name = "manuscript_title")
  private String manuscriptTitle;

  @Column(name = "manuscript_summary")
  private String manuscriptSummary;

  @Column(name = "manuscript_file_name")
  private String manuscriptFileName;

  @Column(name = "manuscript_version")
  private Integer manuscriptVersion;

  @Column(name = "manuscript_uploaded_at")
  private LocalDateTime manuscriptUploadedAt;

  private String status;

  @Column(name = "editor_notes")
  private String editorNotes;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Long getId() { return id; }

  public void setId(Long id) { this.id = id; }

  public UserEntity getMangaka() { return mangaka; }

  public void setMangaka(UserEntity mangaka) { this.mangaka = mangaka; }

  public UserEntity getTantouEditor() { return tantouEditor; }

  public void setTantouEditor(UserEntity tantouEditor) { this.tantouEditor = tantouEditor; }

  public String getTitle() { return title; }

  public void setTitle(String title) { this.title = title; }

  public String getGenre() { return genre; }

  public void setGenre(String genre) { this.genre = genre; }

  public String getTargetAudience() { return targetAudience; }

  public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

  public String getSynopsis() { return synopsis; }

  public void setSynopsis(String synopsis) { this.synopsis = synopsis; }

  public String getManuscriptTitle() { return manuscriptTitle; }

  public void setManuscriptTitle(String manuscriptTitle) { this.manuscriptTitle = manuscriptTitle; }

  public String getManuscriptSummary() { return manuscriptSummary; }

  public void setManuscriptSummary(String manuscriptSummary) { this.manuscriptSummary = manuscriptSummary; }

  public String getManuscriptFileName() { return manuscriptFileName; }

  public void setManuscriptFileName(String manuscriptFileName) { this.manuscriptFileName = manuscriptFileName; }

  public Integer getManuscriptVersion() { return manuscriptVersion; }

  public void setManuscriptVersion(Integer manuscriptVersion) { this.manuscriptVersion = manuscriptVersion; }

  public LocalDateTime getManuscriptUploadedAt() { return manuscriptUploadedAt; }

  public void setManuscriptUploadedAt(LocalDateTime manuscriptUploadedAt) { this.manuscriptUploadedAt = manuscriptUploadedAt; }

  public String getStatus() { return status; }

  public void setStatus(String status) { this.status = status; }

  public String getEditorNotes() { return editorNotes; }

  public void setEditorNotes(String editorNotes) { this.editorNotes = editorNotes; }

  public LocalDateTime getCreatedAt() { return createdAt; }

  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }

  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
