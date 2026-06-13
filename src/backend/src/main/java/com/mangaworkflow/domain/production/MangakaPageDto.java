package com.mangaworkflow.domain.production;

import java.util.List;

public class MangakaPageDto {
  private String id;
  private String chapterId;
  private int pageNumber;
  private String fileName;
  private MangakaPageStatus status;
  private String uploadedAt;
  private List<MangakaPageRegionDto> regions;

  public MangakaPageDto() {}

  public MangakaPageDto(
      String id,
      String chapterId,
      int pageNumber,
      String fileName,
      MangakaPageStatus status,
      String uploadedAt,
      List<MangakaPageRegionDto> regions) {
    this.id = id;
    this.chapterId = chapterId;
    this.pageNumber = pageNumber;
    this.fileName = fileName;
    this.status = status;
    this.uploadedAt = uploadedAt;
    this.regions = regions;
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

  public List<MangakaPageRegionDto> getRegions() {
    return regions;
  }
}
