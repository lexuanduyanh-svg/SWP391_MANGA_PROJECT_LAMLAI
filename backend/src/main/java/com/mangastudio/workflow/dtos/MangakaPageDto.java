package com.mangastudio.workflow.dtos;

/**
 * Page DTO (V2 scope — no regions).
 * 
 * <p>Tasks are assigned at the page level. The {@code regions} list has been removed.
 */
public class MangakaPageDto {
  private String id;
  private String chapterId;
  private int pageNumber;
  private String fileName;
  private MangakaPageStatus status;
  private String uploadedAt;

  public MangakaPageDto() {}

  public MangakaPageDto(
      String id,
      String chapterId,
      int pageNumber,
      String fileName,
      MangakaPageStatus status,
      String uploadedAt) {
    this.id = id;
    this.chapterId = chapterId;
    this.pageNumber = pageNumber;
    this.fileName = fileName;
    this.status = status;
    this.uploadedAt = uploadedAt;
  }

  public String getId() {
    return id;
  }

  public String getChapterId() {
    return chapterId;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public String getFileName() {
    return fileName;
  }

  public MangakaPageStatus getStatus() {
    return status;
  }

  public String getUploadedAt() {
    return uploadedAt;
  }
}
