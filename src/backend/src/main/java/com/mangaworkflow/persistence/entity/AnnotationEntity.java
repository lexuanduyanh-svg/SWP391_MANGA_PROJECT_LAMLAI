package com.mangaworkflow.persistence.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "annotations")
public class AnnotationEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="markup_id") private Long id;
  @ManyToOne @JoinColumn(name="page_id") private PageEntity page; @ManyToOne @JoinColumn(name="editor_id") private UserEntity editor;
  @Column(name="spatial_coordinates") private String spatialCoordinates; @Lob private String content; private Boolean resolved; @Column(name="created_at") private LocalDateTime createdAt;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public PageEntity getPage(){return page;} public void setPage(PageEntity page){this.page=page;}
  public UserEntity getEditor(){return editor;} public void setEditor(UserEntity editor){this.editor=editor;}
  public String getSpatialCoordinates(){return spatialCoordinates;} public void setSpatialCoordinates(String spatialCoordinates){this.spatialCoordinates=spatialCoordinates;}
  public String getContent(){return content;} public void setContent(String content){this.content=content;}
  public Boolean getResolved(){return resolved;} public void setResolved(Boolean resolved){this.resolved=resolved;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}
