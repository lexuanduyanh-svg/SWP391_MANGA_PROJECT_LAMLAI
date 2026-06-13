package com.mangaworkflow.domain.production;

import java.util.List;

public class MangakaChapterDto {
  private String id;
  private String proposalId;
  private String title;
  private int chapterNumber;
  private MangakaChapterStatus status;
  private String createdAt;
  private String updatedAt;
  private List<MangakaPageDto> pages;

  public MangakaChapterDto() {}

  public MangakaChapterDto(
      String id,
      String proposalId,
      String title,
      int chapterNumber,
      MangakaChapterStatus status,
      String createdAt,
      String updatedAt,
      List<MangakaPageDto> pages) {
    this.id = id;
    this.proposalId = proposalId;
    this.title = title;
    this.chapterNumber = chapterNumber;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.pages = pages;
  }

  public String getId() {
    return id;
  }

  public String getProposalId() {
    return proposalId;
  }

  public String getTitle() {
    return title;
  }

  public int getChapterNumber() {
    return chapterNumber;
  }

  public MangakaChapterStatus getStatus() {
    return status;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public List<MangakaPageDto> getPages() {
    return pages;
  }
}
