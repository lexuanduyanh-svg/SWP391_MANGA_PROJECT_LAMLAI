package com.mangaworkflow.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class UserSkillId implements Serializable {

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "skill_id")
  private Long skillId;

  public UserSkillId() {
  }

  public UserSkillId(Long userId, Long skillId) {
    this.userId = userId;
    this.skillId = skillId;
  }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Long getSkillId() { return skillId; }
  public void setSkillId(Long skillId) { this.skillId = skillId; }
  public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof UserSkillId)) return false; UserSkillId that = (UserSkillId) o; return Objects.equals(userId, that.userId) && Objects.equals(skillId, that.skillId); }
  public int hashCode() { return Objects.hash(userId, skillId); }
}
