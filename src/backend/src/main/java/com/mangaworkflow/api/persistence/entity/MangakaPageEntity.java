package com.mangaworkflow.api.persistence.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mangaka_pages")
public class MangakaPageEntity {
  @Id private Long id;
  private Long chapterId;
  private int pageNumber;
  private String fileName;
  private String status;
  private String uploadedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getChapterId() {
    return chapterId;
  }

  public void setChapterId(Long chapterId) {
    this.chapterId = chapterId;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(String uploadedAt) {
    this.uploadedAt = uploadedAt;
  }
}
