package com.mangaworkflow.api.domain.account;

import com.mangaworkflow.api.domain.skill.SkillCategoryDto;

public class AccountDto {
  private String id;
  private String fullName;
  private String email;
  private UserRole role;
  private AccountStatus status;
  private java.util.List<SkillCategoryDto> skills;

  public AccountDto() {}

  public AccountDto(String id, String fullName, String email, UserRole role, AccountStatus status) {
    this.id = id;
    this.fullName = fullName;
    this.email = email;
    this.role = role;
    this.status = status;
    this.skills = new java.util.ArrayList<SkillCategoryDto>();
  }

  public AccountDto(
      String id,
      String fullName,
      String email,
      UserRole role,
      AccountStatus status,
      java.util.List<SkillCategoryDto> skills) {
    this.id = id;
    this.fullName = fullName;
    this.email = email;
    this.role = role;
    this.status = status;
    this.skills = skills;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public AccountStatus getStatus() {
    return status;
  }

  public void setStatus(AccountStatus status) {
    this.status = status;
  }

  public java.util.List<SkillCategoryDto> getSkills() {
    return skills;
  }

  public void setSkills(java.util.List<SkillCategoryDto> skills) {
    this.skills = skills;
  }
}
