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
@Table(name = "series_decisions")
public class SeriesDecisionEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="series_decision_id") private Long id;
  @ManyToOne @JoinColumn(name="series_id") private SeriesEntity series;
  @ManyToOne @JoinColumn(name="board_member_id") private UserEntity boardMember;
  @Column(name="decision_type") private String decisionType;
  @Column(columnDefinition = "TEXT") private String reason;
  @Column(name="new_frequency") private String newFrequency;
  @Column(name="new_format") private String newFormat;
  @Column(name="decided_at") private LocalDateTime decidedAt;

  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public SeriesEntity getSeries(){return series;} public void setSeries(SeriesEntity series){this.series=series;}
  public UserEntity getBoardMember(){return boardMember;} public void setBoardMember(UserEntity boardMember){this.boardMember=boardMember;}
  public String getDecisionType(){return decisionType;} public void setDecisionType(String decisionType){this.decisionType=decisionType;}
  public String getReason(){return reason;} public void setReason(String reason){this.reason=reason;}
  public String getNewFrequency(){return newFrequency;} public void setNewFrequency(String newFrequency){this.newFrequency=newFrequency;}
  public String getNewFormat(){return newFormat;} public void setNewFormat(String newFormat){this.newFormat=newFormat;}
  public LocalDateTime getDecidedAt(){return decidedAt;} public void setDecidedAt(LocalDateTime decidedAt){this.decidedAt=decidedAt;}
}
