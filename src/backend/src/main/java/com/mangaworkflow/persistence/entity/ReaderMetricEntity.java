package com.mangaworkflow.persistence.entity;

import java.time.LocalDate;
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
@Table(name = "reader_metrics")
public class ReaderMetricEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="metric_id") private Long id;
  @ManyToOne @JoinColumn(name="series_id") private SeriesEntity series;
  @Column(name="publication_cycle", nullable=false) private LocalDate publicationCycle; @Column(name="sales_figures") private Integer salesFigures; @Column(name="likes_count") private Integer likesCount; @Column(name="shares_count") private Integer sharesCount; @Column(name="votes_count") private Integer votesCount; @Column(name="created_at") private LocalDateTime createdAt; @Column(name="updated_at") private LocalDateTime updatedAt;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public SeriesEntity getSeries(){return series;} public void setSeries(SeriesEntity series){this.series=series;}
  public LocalDate getPublicationCycle(){return publicationCycle;} public void setPublicationCycle(LocalDate publicationCycle){this.publicationCycle=publicationCycle;}
  public Integer getSalesFigures(){return salesFigures;} public void setSalesFigures(Integer salesFigures){this.salesFigures=salesFigures;}
  public Integer getLikesCount(){return likesCount;} public void setLikesCount(Integer likesCount){this.likesCount=likesCount;}
  public Integer getSharesCount(){return sharesCount;} public void setSharesCount(Integer sharesCount){this.sharesCount=sharesCount;}
  public Integer getVotesCount(){return votesCount;} public void setVotesCount(Integer votesCount){this.votesCount=votesCount;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
  public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime updatedAt){this.updatedAt=updatedAt;}
}
