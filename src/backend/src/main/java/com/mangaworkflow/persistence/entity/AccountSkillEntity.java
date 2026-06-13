package com.mangaworkflow.persistence.entity;

import javax.persistence.*;

@Entity
@Table(
    name = "account_skills",
    uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "skill_id"}))
public class AccountSkillEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "account_id")
  private AccountEntity account;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "skill_id")
  private SkillCategoryEntity skill;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AccountEntity getAccount() {
    return account;
  }

  public void setAccount(AccountEntity account) {
    this.account = account;
  }

  public SkillCategoryEntity getSkill() {
    return skill;
  }

  public void setSkill(SkillCategoryEntity skill) {
    this.skill = skill;
  }
}
