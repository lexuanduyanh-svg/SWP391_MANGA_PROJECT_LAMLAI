package com.mangaworkflow.persistence.entity;

import java.math.BigDecimal;
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
@Table(name = "tasks")
public class TaskEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="task_id") private Long id;
  @ManyToOne @JoinColumn(name="page_id") private PageEntity page;
  @ManyToOne @JoinColumn(name="assistant_id") private UserEntity assistant;
  @ManyToOne @JoinColumn(name="task_type") private SkillEntity taskType;
  @Column(name="region_coordinates") private String regionCoordinates; private BigDecimal payment; private String status;
  @Column(name="feedback_notes") private String feedbackNotes; @Column(name="created_at") private LocalDateTime createdAt; @Column(name="updated_at") private LocalDateTime updatedAt;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public PageEntity getPage(){return page;} public void setPage(PageEntity page){this.page=page;}
  public UserEntity getAssistant(){return assistant;} public void setAssistant(UserEntity assistant){this.assistant=assistant;}
  public SkillEntity getTaskType(){return taskType;} public void setTaskType(SkillEntity taskType){this.taskType=taskType;}
  public String getRegionCoordinates(){return regionCoordinates;} public void setRegionCoordinates(String regionCoordinates){this.regionCoordinates=regionCoordinates;}
  public BigDecimal getPayment(){return payment;} public void setPayment(BigDecimal payment){this.payment=payment;}
  public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
  public String getFeedbackNotes(){return feedbackNotes;} public void setFeedbackNotes(String feedbackNotes){this.feedbackNotes=feedbackNotes;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
  public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime updatedAt){this.updatedAt=updatedAt;}
}
