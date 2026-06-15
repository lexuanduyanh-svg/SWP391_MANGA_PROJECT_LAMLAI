package com.mangaworkflow.persistence.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "user_skills")
public class UserSkillEntity {

  @EmbeddedId
  private UserSkillId id;

  @ManyToOne
  @MapsId("userId")
  @JoinColumn(name = "user_id")
  private UserEntity user;

  @ManyToOne
  @MapsId("skillId")
  @JoinColumn(name = "skill_id")
  private SkillEntity skill;

  @Column(name = "assigned_at")
  private LocalDateTime assignedAt;

  public UserSkillId getId() { return id; }
  public void setId(UserSkillId id) { this.id = id; }
  public UserEntity getUser() { return user; }
  public void setUser(UserEntity user) { this.user = user; }
  public SkillEntity getSkill() { return skill; }
  public void setSkill(SkillEntity skill) { this.skill = skill; }
  public LocalDateTime getAssignedAt() { return assignedAt; }
  public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
