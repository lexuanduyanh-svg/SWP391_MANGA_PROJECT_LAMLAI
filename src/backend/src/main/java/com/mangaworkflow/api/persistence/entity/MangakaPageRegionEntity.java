package com.mangaworkflow.api.persistence.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mangaka_page_regions")
public class MangakaPageRegionEntity {
  @Id private Long id;
  private Long pageId;
  private String regionType;
  private double x;
  private double y;
  private double widthPct;
  private double heightPct;
  private String note;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getPageId() {
    return pageId;
  }

  public void setPageId(Long pageId) {
    this.pageId = pageId;
  }

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
