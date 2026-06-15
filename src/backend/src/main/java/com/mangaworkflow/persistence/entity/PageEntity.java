package com.mangaworkflow.persistence.entity;

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
@Table(name = "pages")
public class PageEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="page_id") private Long id;
  @ManyToOne @JoinColumn(name="chapter_id") private ChapterEntity chapter;
  @Column(name="page_number", nullable=false) private Integer pageNumber; @Column(name="manuscript_file_path") private String manuscriptFilePath; private String status;
  @Column(name="created_at") private LocalDateTime createdAt; @Column(name="updated_at") private LocalDateTime updatedAt;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public ChapterEntity getChapter(){return chapter;} public void setChapter(ChapterEntity chapter){this.chapter=chapter;}
  public Integer getPageNumber(){return pageNumber;} public void setPageNumber(Integer pageNumber){this.pageNumber=pageNumber;}
  public String getManuscriptFilePath(){return manuscriptFilePath;} public void setManuscriptFilePath(String manuscriptFilePath){this.manuscriptFilePath=manuscriptFilePath;}
  public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
  public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime updatedAt){this.updatedAt=updatedAt;}
}
