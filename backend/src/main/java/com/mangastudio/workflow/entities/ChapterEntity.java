package com.mangastudio.workflow.entities;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "chapters")
public class ChapterEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="chapter_id") private Long id;
  @ManyToOne @JoinColumn(name="series_id") private SeriesEntity series;
  @Column(name="chapter_number", nullable=false) private Integer chapterNumber; private String title; private String status;
  @Column(name="print_deadline") private LocalDateTime printDeadline;
  @Column(name="created_at") private LocalDateTime createdAt;
  @Column(name="updated_at") private LocalDateTime updatedAt;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public SeriesEntity getSeries(){return series;} public void setSeries(SeriesEntity series){this.series=series;}
  public Integer getChapterNumber(){return chapterNumber;} public void setChapterNumber(Integer chapterNumber){this.chapterNumber=chapterNumber;}
  public String getTitle(){return title;} public void setTitle(String title){this.title=title;}
  public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
  public LocalDateTime getPrintDeadline(){return printDeadline;} public void setPrintDeadline(LocalDateTime printDeadline){this.printDeadline=printDeadline;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
  public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime updatedAt){this.updatedAt=updatedAt;}
}
