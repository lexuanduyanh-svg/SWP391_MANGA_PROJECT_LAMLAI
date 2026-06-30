import { useEffect, useMemo, useState } from "react";
import { logout } from "../services/authService";
import {
  approveProposalForProduction,
  getManuscriptDownloadUrl,
  listEditorialBoardProposals,
  rejectProposalByBoard,
} from "../services/editorialBoardProposalService";
import type { LoginResponse } from "../types/auth";
import type { MangaProposal, MangaProposalStatus } from "../types/mangaka";

interface EditorialBoardDashboardProps {
  session: LoginResponse;
  onLogout?: () => void;
}

type BoardBucket = "review" | "approved" | "rejected";

const BUCKETS: Array<{
  key: BoardBucket;
  label: string;
  statuses: MangaProposalStatus[];
  description: string;
}> = [
  {
    key: "review",
    label: "Under Board Review",
    statuses: ["UnderBoardReview"],
    description: "Editor-approved proposals awaiting board decision.",
  },
  {
    key: "approved",
    label: "Approved",
    statuses: ["Approved"],
    description:
      "Approved for production.",
  },
  {
    key: "rejected",
    label: "Rejected",
    statuses: ["Rejected"],
    description: "Closed items that were not approved by the board.",
  },
];

function normalizeStatus(status?: string | null): MangaProposalStatus | null {
  if (!status) return null;

  const compact = status.toLowerCase().replace(/[\s_-]/g, "");
  if (compact.includes("underboardreview")) return "UnderBoardReview";
  if (compact.includes("approved")) return "Approved";
  if (compact.includes("rejected")) return "Rejected";
  if (compact.includes("needsrevision")) return "NeedsRevision";
  if (compact.includes("submittedtoeditor")) return "SubmittedToEditor";
  if (compact.includes("draft")) return "Draft";

  return null;
}

function formatDate(value?: string | null): string {
  if (!value) return "—";

  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? value
    : new Intl.DateTimeFormat("en", {
        month: "short",
        day: "numeric",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }).format(date);
}

function valueOrDash(value?: string | number | null): string {
  return value === null || value === undefined || value === ""
    ? "—"
    : String(value);
}

function displayFileName(value?: string | null): string {
  if (!value) return "—";
  return value.replace(/^\d+-/, "");
}

function statusLabel(status?: string | null): string {
  const normalized = normalizeStatus(status);
  if (normalized === "UnderBoardReview") return "Under Board Review";
  if (normalized === "Approved") return "Approved";
  if (normalized === "Rejected") return "Rejected";
  return valueOrDash(status);
}

function statusTone(status?: string | null): string {
  const normalized = normalizeStatus(status);
  if (normalized === "UnderBoardReview") return "role-pill role-pill--info";
  if (normalized === "Approved")
    return "role-pill role-pill--success";
  if (normalized === "Rejected") return "role-pill role-pill--danger";
  return "role-pill role-pill--neutral";
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Không thể tải dữ liệu.";
}

function boardVoteDisplay(proposal: MangaProposal) {
  const total = proposal.boardTotalVotes ?? 3;
  const status = normalizeStatus(proposal.status);
  const hasRecordedVotes =
    (proposal.boardApproveVotes ?? 0) + (proposal.boardRejectVotes ?? 0) > 0;
  if (
    (status === "Approved" ||
      status === "Rejected") &&
    !hasRecordedVotes
  ) {
    return {
      approve: 0,
      reject: 0,
      pending: 0,
      completed: true,
      hasRecordedVotes: false,
    };
  }
  return {
    approve: proposal.boardApproveVotes ?? 0,
    reject: proposal.boardRejectVotes ?? 0,
    pending: proposal.boardPendingVotes ?? total,
    completed: status !== "UnderBoardReview",
    hasRecordedVotes,
  };
}

export function EditorialBoardDashboard({
  session,
  onLogout,
}: EditorialBoardDashboardProps) {
  const [proposals, setProposals] = useState<MangaProposal[]>([]);
  const [selectedProposalId, setSelectedProposalId] = useState<string | null>(
    null,
  );
  const [isLoading, setIsLoading] = useState(true);
  const [activeAction, setActiveAction] = useState<"approve" | "reject" | null>(
    null,
  );
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const selectedProposal = useMemo(
    () =>
      proposals.find((proposal) => proposal.id === selectedProposalId) ?? null,
    [proposals, selectedProposalId],
  );

  const groupedProposals = useMemo(
    () =>
      BUCKETS.map((bucket) => ({
        ...bucket,
        proposals: proposals.filter((proposal) => {
          const normalized = normalizeStatus(proposal.status);
          return normalized ? bucket.statuses.includes(normalized) : false;
        }),
      })),
    [proposals],
  );

  const reviewCount =
    groupedProposals.find((bucket) => bucket.key === "review")?.proposals
      .length ?? 0;
  const approvedCount =
    groupedProposals.find((bucket) => bucket.key === "approved")?.proposals
      .length ?? 0;
  const rejectedCount =
    groupedProposals.find((bucket) => bucket.key === "rejected")?.proposals
      .length ?? 0;
  const decisionSummary = {
    quorum: Math.max(3, Math.ceil(proposals.length / 2)),
    votesFor: approvedCount,
    votesAgainst: rejectedCount,
    pending: reviewCount,
  };

  async function loadProposals() {
    setIsLoading(true);
    setErrorMessage(null);

    try {
      const loaded = await listEditorialBoardProposals(session.user.email);
      setProposals(loaded);
      setSelectedProposalId((current) =>
        current && loaded.some((proposal) => proposal.id === current)
          ? current
          : (loaded[0]?.id ?? null),
      );
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadProposals();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session.user.email]);

  function mergeUpdatedProposal(
    updated: MangaProposal | null,
    fallbackStatus: MangaProposalStatus,
  ) {
    if (updated) {
      setProposals((current) =>
        current.map((proposal) =>
          proposal.id === updated.id ? updated : proposal,
        ),
      );
      setSelectedProposalId(updated.id);
      return;
    }

    setProposals((current) =>
      current.map((proposal) =>
        proposal.id === selectedProposal?.id
          ? {
              ...proposal,
              status: fallbackStatus,
              boardMemberEmail: session.user.email,
              boardReviewedAt: new Date().toISOString(),
            }
          : proposal,
      ),
    );
  }

  async function handleAction(
    action: "approve" | "reject",
    request: (
      proposalId: string,
      payload: { memberEmail: string; note: string },
    ) => Promise<MangaProposal | null>,
    successLabel: string,
    fallbackStatus: MangaProposalStatus,
  ) {
    if (!selectedProposal) return;

    setActiveAction(action);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const updated = await request(selectedProposal.id, {
        memberEmail: session.user.email,
        note: "",
      });

      mergeUpdatedProposal(updated, fallbackStatus);
      setSuccessMessage(successLabel);
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setActiveAction(null);
    }
  }

  function handleLogout() {
    logout();
    onLogout?.();
  }

  return (
    <main
      className="board-page board-page--editorial"
      id="editorial-board-dashboard"
    >
      <div className="editor-dashboard editorial-dashboard">
        <section className="dashboard-hero editorial-hero glass-card">
          <div className="dashboard-hero__copy">
            <span className="eyebrow">Editorial Board workspace</span>
            <h1>Vote on board-ready proposals.</h1>
            <p>
              Each board member votes once. When all votes are submitted, the
              system automatically applies the majority decision.
            </p>
          </div>

          <div className="dashboard-hero__meta">
            <div className="dashboard-chip dashboard-chip--accent">
              <span>Board member</span>
              <strong>{session.user.fullName}</strong>
            </div>
            <div className="dashboard-chip dashboard-chip--muted">
              <span>Email</span>
              <strong>{session.user.email}</strong>
            </div>
            <button
              type="button"
              className="button button-secondary dashboard-logout"
              onClick={handleLogout}
            >
              Log out
            </button>
          </div>
        </section>

        <section className="editor-summary-grid" aria-label="Board summary">
          <article className="role-metric-card">
            <span>Under Board Review</span>
            <strong>{reviewCount}</strong>
            <p>Items waiting for a board decision.</p>
          </article>
          <article className="role-metric-card role-metric-card--accent">
            <span>Approved</span>
            <strong>{approvedCount}</strong>
            <p>Approved or already moving through serialization.</p>
          </article>
          <article className="role-metric-card role-metric-card--soft">
            <span>Rejected / Closed</span>
            <strong>{rejectedCount}</strong>
            <p>Proposals that ended their board journey here.</p>
          </article>
        </section>

        <section className="panel-card panel-card--inner editorial-summary-panel">
          <div className="panel-card__header panel-card__header--stacked">
            <div>
              <span className="eyebrow">Decision summary</span>
              <h4>Board voting snapshot</h4>
              <p>Demo-only overview derived from proposal state.</p>
            </div>
          </div>
          <div className="production-summary production-summary--compact">
            <div>
              <span>Quorum</span>
              <strong>{decisionSummary.quorum}</strong>
            </div>
            <div>
              <span>For</span>
              <strong>{decisionSummary.votesFor}</strong>
            </div>
            <div>
              <span>Against</span>
              <strong>{decisionSummary.votesAgainst}</strong>
            </div>
            <div>
              <span>Pending</span>
              <strong>{decisionSummary.pending}</strong>
            </div>
          </div>
        </section>

        {errorMessage && (
          <div className="board-alert board-alert--error" role="alert">
            {errorMessage}
          </div>
        )}
        {successMessage && (
          <div className="board-alert board-alert--success" role="status">
            {successMessage}
          </div>
        )}

        <section className="editor-workflow editorial-workflow">
          <div className="editor-queue panel-card">
            <div className="panel-card__header panel-card__header--stacked">
              <div>
                <span className="eyebrow">Board queue</span>
                <h3>Review proposals awaiting a board decision</h3>
                <p>
                  Focus on the editor’s handoff, manuscript metadata, and the
                  final note before you decide.
                </p>
              </div>
            </div>

            {isLoading ? (
              <div className="board-empty board-empty--compact">
                Loading proposals…
              </div>
            ) : groupedProposals.every(
                (bucket) => bucket.proposals.length === 0,
              ) ? (
              <div className="board-empty board-empty--compact">
                No proposals are waiting for board review.
              </div>
            ) : (
              <div className="editor-columns">
                {groupedProposals.map((bucket) => (
                  <article className="editor-column" key={bucket.key}>
                    <header className="editor-column__header">
                      <div>
                        <span>{bucket.label}</span>
                        <p>{bucket.description}</p>
                      </div>
                      <strong>{bucket.proposals.length}</strong>
                    </header>

                    <div className="editor-column__list">
                      {bucket.proposals.length === 0 ? (
                        <div className="editor-empty-list">No items here.</div>
                      ) : (
                        bucket.proposals.map((proposal) => {
                          const isSelected = proposal.id === selectedProposalId;

                          return (
                            <button
                              key={proposal.id}
                              type="button"
                              className={`editor-proposal-card ${isSelected ? "is-selected" : ""}`}
                              onClick={() => setSelectedProposalId(proposal.id)}
                            >
                              <div className="editor-proposal-card__head">
                                <strong>{proposal.title}</strong>
                                <span className={statusTone(proposal.status)}>
                                  {statusLabel(proposal.status)}
                                </span>
                              </div>
                              <p>
                                {proposal.genre} · {proposal.targetAudience}
                              </p>
                              <div className="editor-proposal-card__meta">
                                <span>{proposal.authorEmail}</span>
                                <span>
                                  {valueOrDash(proposal.manuscriptVersion)}
                                </span>
                              </div>
                            </button>
                          );
                        })
                      )}
                    </div>
                  </article>
                ))}
              </div>
            )}
          </div>

          <article
            className="panel-card editor-detail editorial-detail"
            id="board-detail"
          >
            <div className="panel-card__header panel-card__header--stacked">
              <div>
                <span className="eyebrow">Board decision</span>
                <h3>
                  {selectedProposal
                    ? selectedProposal.title
                    : "Select a proposal"}
                </h3>
                <p>
                  {selectedProposal
                    ? selectedProposal.synopsis
                    : "Choose a proposal from the queue to review the final editor handoff."}
                </p>
              </div>
              {selectedProposal && (
                <span className={statusTone(selectedProposal.status)}>
                  {statusLabel(selectedProposal.status)}
                </span>
              )}
            </div>

            {selectedProposal ? (
              (() => {
                const votes = boardVoteDisplay(selectedProposal);
                const votingOpen =
                  normalizeStatus(selectedProposal.status) ===
                    "UnderBoardReview" &&
                  selectedProposal.currentMemberVote == null;
                return (
                  <>
                    <div className="editor-detail__grid">
                      <div className="editor-detail__item">
                        <span>Author</span>
                        <strong>
                          {valueOrDash(selectedProposal.authorEmail)}
                        </strong>
                      </div>
                      <div className="editor-detail__item">
                        <span>Genre</span>
                        <strong>{valueOrDash(selectedProposal.genre)}</strong>
                      </div>
                      <div className="editor-detail__item">
                        <span>Audience</span>
                        <strong>
                          {valueOrDash(selectedProposal.targetAudience)}
                        </strong>
                      </div>
                      <div className="editor-detail__item editor-detail__item--file">
                        <span>Manuscript file</span>
                        <strong
                          title={
                            selectedProposal.manuscriptFileName ?? undefined
                          }
                        >
                          {displayFileName(selectedProposal.manuscriptFileName)}
                        </strong>
                        {selectedProposal.manuscriptFileName && (
                          <a
                            className="button button-secondary editor-download-link"
                            href={getManuscriptDownloadUrl(
                              selectedProposal.manuscriptFileName,
                            )}
                            download
                          >
                            Download manuscript
                          </a>
                        )}
                      </div>
                      <div className="editor-detail__item">
                        <span>Version</span>
                        <strong>
                          {valueOrDash(selectedProposal.manuscriptVersion)}
                        </strong>
                      </div>
                      <div className="editor-detail__item">
                        <span>Uploaded</span>
                        <strong>
                          {formatDate(selectedProposal.manuscriptUploadedAt)}
                        </strong>
                      </div>
                    </div>

                    <div className="editor-detail__block">
                      <span>Board vote summary</span>
                      <div className="board-vote-summary">
                        <div>
                          <strong>{votes.approve}</strong>
                          <small>Approve</small>
                        </div>
                        <div>
                          <strong>{votes.reject}</strong>
                          <small>Reject</small>
                        </div>
                        <div>
                          <strong>{votes.pending}</strong>
                          <small>Pending</small>
                        </div>
                      </div>
                      <p>
                        {selectedProposal.currentMemberVote
                          ? `Your vote: ${selectedProposal.currentMemberVote === "APPROVE" ? "Approve" : "Reject"}`
                          : votes.completed && !votes.hasRecordedVotes
                            ? "This proposal was seeded as completed, so no vote history is available."
                            : votes.completed
                              ? "Board decision is already completed."
                              : "Your vote is still pending."}
                      </p>
                    </div>

                    {votingOpen ? (
                      <div className="editor-actions">
                        <button
                          type="button"
                          className="button button-primary"
                          onClick={() =>
                            void handleAction(
                              "approve",
                              approveProposalForProduction,
                              "Approve vote submitted.",
                              "UnderBoardReview",
                            )
                          }
                          disabled={activeAction !== null}
                        >
                          {activeAction === "approve"
                            ? "Voting..."
                            : "Vote Approve"}
                        </button>
                        <button
                          type="button"
                          className="button button-danger"
                          onClick={() =>
                            void handleAction(
                              "reject",
                              rejectProposalByBoard,
                              "Reject vote submitted.",
                              "UnderBoardReview",
                            )
                          }
                          disabled={activeAction !== null}
                        >
                          {activeAction === "reject"
                            ? "Voting..."
                            : "Vote Reject"}
                        </button>
                      </div>
                    ) : (
                      <div className="board-alert board-alert--hint">
                        No voting action is available for this proposal.
                      </div>
                    )}
                  </>
                );
              })()
            ) : (
              <div className="board-empty board-empty--detail">
                Select a proposal to review the editor handoff and make the
                final call.
              </div>
            )}
          </article>
        </section>
      </div>
    </main>
  );
}
