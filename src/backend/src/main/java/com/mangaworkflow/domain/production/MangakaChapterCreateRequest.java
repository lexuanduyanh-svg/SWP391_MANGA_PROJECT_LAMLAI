package com.mangaworkflow.domain.production;

public class MangakaChapterCreateRequest {
  private String title;
  private int chapterNumber;

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
}
