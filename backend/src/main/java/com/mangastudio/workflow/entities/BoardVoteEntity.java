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
@Table(name = "board_votes")
public class BoardVoteEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "vote_id")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "proposal_id")
  private ProposalEntity proposal;

  @ManyToOne
  @JoinColumn(name = "board_member_id")
  private UserEntity boardMember;

  private String decision;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public Long getId() { return id; }

  public void setId(Long id) { this.id = id; }

  public ProposalEntity getProposal() { return proposal; }

  public void setProposal(ProposalEntity proposal) { this.proposal = proposal; }

  public UserEntity getBoardMember() { return boardMember; }

  public void setBoardMember(UserEntity boardMember) { this.boardMember = boardMember; }

  public String getDecision() { return decision; }

  public void setDecision(String decision) { this.decision = decision; }

  public LocalDateTime getCreatedAt() { return createdAt; }

  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
