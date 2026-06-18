package com.mangastudio.workflow.dtos;

public class MangakaPageCreateRequest {
  private int pageNumber;
  private String fileName;

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
}
