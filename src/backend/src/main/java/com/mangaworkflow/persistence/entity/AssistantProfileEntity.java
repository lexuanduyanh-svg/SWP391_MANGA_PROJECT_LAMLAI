package com.mangaworkflow.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "assistant_profiles")
public class AssistantProfileEntity {
  @Id
  @Column(name = "user_id")
  private Long userId;

  @OneToOne
  @JoinColumn(name = "user_id")
  private UserEntity user;

  @Column(name = "monthly_earnings", precision = 10, scale = 2)
  private BigDecimal monthlyEarnings;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public UserEntity getUser() { return user; }
  public void setUser(UserEntity user) { this.user = user; }
  public BigDecimal getMonthlyEarnings() { return monthlyEarnings; }
  public void setMonthlyEarnings(BigDecimal monthlyEarnings) { this.monthlyEarnings = monthlyEarnings; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
