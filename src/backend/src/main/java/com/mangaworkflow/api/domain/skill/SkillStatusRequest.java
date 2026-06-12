package com.mangaworkflow.api.domain.skill;

import javax.validation.constraints.NotNull;

public class SkillStatusRequest {
  @NotNull private Boolean active;

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }
}
