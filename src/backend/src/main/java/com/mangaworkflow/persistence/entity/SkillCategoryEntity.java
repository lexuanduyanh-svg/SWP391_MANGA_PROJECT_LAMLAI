package com.mangaworkflow.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "skill_categories", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class SkillCategoryEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Column(length = 1000)
  private String description;

  private boolean active;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
