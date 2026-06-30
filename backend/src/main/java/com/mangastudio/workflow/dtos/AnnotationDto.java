package com.mangastudio.workflow.dtos;

public class AnnotationDto {
  private String id;
  private String pageId;
  private String editorEmail;
  private String spatialCoordinates;
  private String content;
  private String resolved;
  private String createdAt;

  public AnnotationDto() {}
  public AnnotationDto(String id, String pageId, String editorEmail, String spatialCoordinates,
                       String content, String resolved, String createdAt) {
    this.id = id; this.pageId = pageId; this.editorEmail = editorEmail;
    this.spatialCoordinates = spatialCoordinates; this.content = content;
    this.resolved = resolved; this.createdAt = createdAt;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getPageId() { return pageId; }
  public void setPageId(String pageId) { this.pageId = pageId; }
  public String getEditorEmail() { return editorEmail; }
  public void setEditorEmail(String editorEmail) { this.editorEmail = editorEmail; }
  public String getSpatialCoordinates() { return spatialCoordinates; }
  public void setSpatialCoordinates(String spatialCoordinates) { this.spatialCoordinates = spatialCoordinates; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getResolved() { return resolved; }
  public void setResolved(String resolved) { this.resolved = resolved; }
  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
