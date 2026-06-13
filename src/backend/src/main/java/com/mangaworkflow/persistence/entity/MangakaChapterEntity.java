package com.mangaworkflow.persistence.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mangaka_chapters")
public class MangakaChapterEntity {
  @Id private Long id;
  private Long proposalId;
  private String title;
  private int chapterNumber;
  private String status;
  private String createdAt;
  private String updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getProposalId() {
    return proposalId;
  }

  public void setProposalId(Long proposalId) {
    this.proposalId = proposalId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getChapterNumber() {
    return chapterNumber;
  }

  public void setChapterNumber(int chapterNumber) {
    this.chapterNumber = chapterNumber;
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
}
