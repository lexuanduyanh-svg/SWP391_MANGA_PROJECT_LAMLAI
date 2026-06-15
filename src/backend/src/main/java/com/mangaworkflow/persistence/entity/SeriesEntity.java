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
@Table(name = "series")
public class SeriesEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="series_id") private Long id;
  @ManyToOne @JoinColumn(name="mangaka_id") private UserEntity mangaka;
  @ManyToOne @JoinColumn(name="tantou_editor_id") private UserEntity tantouEditor;
  @Column(nullable=false) private String title;
  @Lob private String synopsis;
  private String genre; private String status;
  @Column(name="publishing_frequency") private String publishingFrequency;
  @Column(name="editor_notes") private String editorNotes;
  @Column(name="created_at") private LocalDateTime createdAt;
  @Column(name="updated_at") private LocalDateTime updatedAt;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public UserEntity getMangaka(){return mangaka;} public void setMangaka(UserEntity mangaka){this.mangaka=mangaka;}
  public UserEntity getTantouEditor(){return tantouEditor;} public void setTantouEditor(UserEntity tantouEditor){this.tantouEditor=tantouEditor;}
  public String getTitle(){return title;} public void setTitle(String title){this.title=title;}
  public String getSynopsis(){return synopsis;} public void setSynopsis(String synopsis){this.synopsis=synopsis;}
  public String getGenre(){return genre;} public void setGenre(String genre){this.genre=genre;}
  public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
  public String getPublishingFrequency(){return publishingFrequency;} public void setPublishingFrequency(String publishingFrequency){this.publishingFrequency=publishingFrequency;}
  public String getEditorNotes(){return editorNotes;} public void setEditorNotes(String editorNotes){this.editorNotes=editorNotes;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
  public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime updatedAt){this.updatedAt=updatedAt;}
}
