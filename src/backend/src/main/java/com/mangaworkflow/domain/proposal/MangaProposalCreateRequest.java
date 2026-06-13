package com.mangaworkflow.domain.proposal;

public class MangaProposalCreateRequest {
  private String authorEmail;
  private String title;
  private String genre;
  private String targetAudience;
  private String synopsis;
  private String manuscriptTitle;
  private String manuscriptSummary;
  private String manuscriptFileName;

  public String getAuthorEmail() {
    return authorEmail;
  }

  public void setAuthorEmail(String authorEmail) {
    this.authorEmail = authorEmail;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public String getTargetAudience() {
    return targetAudience;
  }

  public void setTargetAudience(String targetAudience) {
    this.targetAudience = targetAudience;
  }

  public String getSynopsis() {
    return synopsis;
  }

  public void setSynopsis(String synopsis) {
    this.synopsis = synopsis;
  }

  public String getManuscriptTitle() {
    return manuscriptTitle;
  }

  public void setManuscriptTitle(String manuscriptTitle) {
    this.manuscriptTitle = manuscriptTitle;
  }

  public String getManuscriptSummary() {
    return manuscriptSummary;
  }

  public void setManuscriptSummary(String manuscriptSummary) {
    this.manuscriptSummary = manuscriptSummary;
  }

  public String getManuscriptFileName() {
    return manuscriptFileName;
  }

  public void setManuscriptFileName(String manuscriptFileName) {
    this.manuscriptFileName = manuscriptFileName;
  }
}
