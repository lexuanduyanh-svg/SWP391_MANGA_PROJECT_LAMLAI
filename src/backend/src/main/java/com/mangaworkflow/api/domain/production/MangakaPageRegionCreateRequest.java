package com.mangaworkflow.api.domain.production;

public class MangakaPageRegionCreateRequest {
  private String regionType;
  private double x;
  private double y;
  private double widthPct;
  private double heightPct;
  private String note;

  public String getRegionType() {
    return regionType;
  }

  public void setRegionType(String regionType) {
    this.regionType = regionType;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getWidthPct() {
    return widthPct;
  }

  public void setWidthPct(double widthPct) {
    this.widthPct = widthPct;
  }

  public double getHeightPct() {
    return heightPct;
  }

  public void setHeightPct(double heightPct) {
    this.heightPct = heightPct;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
