package com.mangaworkflow.domain.production;

import java.util.List;

public class MangakaPageRegionDto {
  private String id;
  private String pageId;
  private String regionType;
  private double x;
  private double y;
  private double widthPct;
  private double heightPct;
  private String note;
  private List<MangakaProductionTaskDto> tasks;

  public MangakaPageRegionDto() {}

  public MangakaPageRegionDto(
      String id,
      String pageId,
      String regionType,
      double x,
      double y,
      double widthPct,
      double heightPct,
      String note,
      List<MangakaProductionTaskDto> tasks) {
    this.id = id;
    this.pageId = pageId;
    this.regionType = regionType;
    this.x = x;
    this.y = y;
    this.widthPct = widthPct;
    this.heightPct = heightPct;
    this.note = note;
    this.tasks = tasks;
  }

  public String getId() {
    return id;
  }

  public String getPageId() {
    return pageId;
  }

  public String getRegionType() {
    return regionType;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getWidthPct() {
    return widthPct;
  }

  public double getHeightPct() {
    return heightPct;
  }

  public String getNote() {
    return note;
  }

  public List<MangakaProductionTaskDto> getTasks() {
    return tasks;
  }
}
