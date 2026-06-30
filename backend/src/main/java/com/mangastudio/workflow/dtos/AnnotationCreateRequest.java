package com.mangastudio.workflow.dtos;

public class AnnotationCreateRequest {
  private String editorEmail;
  private String spatialCoordinates;
  private String content;

  public String getEditorEmail() { return editorEmail; }
  public void setEditorEmail(String editorEmail) { this.editorEmail = editorEmail; }
  public String getSpatialCoordinates() { return spatialCoordinates; }
  public void setSpatialCoordinates(String spatialCoordinates) { this.spatialCoordinates = spatialCoordinates; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
}
