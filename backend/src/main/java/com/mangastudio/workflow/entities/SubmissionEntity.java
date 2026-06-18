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
@Table(name = "submissions")
public class SubmissionEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="submission_id") private Long id;
  @ManyToOne @JoinColumn(name="task_id") private TaskEntity task;
  @Column(name="asset_file_path", nullable=false) private String assetFilePath; @Column(name="submitted_at") private LocalDateTime submittedAt;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public TaskEntity getTask(){return task;} public void setTask(TaskEntity task){this.task=task;}
  public String getAssetFilePath(){return assetFilePath;} public void setAssetFilePath(String assetFilePath){this.assetFilePath=assetFilePath;}
  public LocalDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(LocalDateTime submittedAt){this.submittedAt=submittedAt;}
}
