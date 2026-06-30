import { useEffect, useMemo, useState } from "react";
import { logout } from "../services/authService";
import {
  forwardProposalToBoard,
  getManuscriptDownloadUrl,
  listTantouEditorProposals,
  requestProposalRevision,
} from "../services/tantouEditorProposalService";
import type { LoginResponse } from "../types/auth";
import type { MangaProposal, MangaProposalStatus } from "../types/mangaka";
import PageAnnotations from "./PageAnnotations";

interface TantouEditorDashboardProps {
  session: LoginResponse;
  onLogout?: () => void;
}

type EditorSection = "proposals" | "production";

type ProposalBucket = "submitted" | "board" | "needsWork";

const BUCKETS: Array<{
  key: ProposalBucket;
  label: string;
  statuses: MangaProposalStatus[];
  description: string;
}> = [
  {
    key: "submitted",
    label: "Submitted",
    statuses: ["SubmittedToEditor"],
    description: "New proposals waiting for the first editorial pass.",
  },
  {
    key: "board",
    label: "Forwarded to Board",
    statuses: ["UnderBoardReview"],
    description: "Approved by the editor and waiting on the board.",
  },
  {
    key: "needsWork",
    label: "Revision / Completed",
    statuses: ["NeedsRevision", "Approved"],
    description:
      "Sent back for author revision or already completed beyond editor review.",
  },
];

function normalizeStatus(status?: string | null): MangaProposalStatus | null {
  if (!status) return null;

  const compact = status.toLowerCase().replace(/[\s_-]/g, "");
  if (compact.includes("underboardreview")) return "UnderBoardReview";
  if (compact.includes("submittedtoeditor")) return "SubmittedToEditor";
  if (compact.includes("needsrevision")) return "NeedsRevision";
  if (compact.includes("approved")) return "Approved";
  if (compact.includes("rejected")) return "Rejected";
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
  if (normalized === "UnderBoardReview") return "Forwarded to Board";
  if (normalized === "NeedsRevision") return "Needs Revision";
  if (normalized === "Approved") return "Approved";
  if (normalized === "Rejected") return "Rejected";
  if (normalized === "SubmittedToEditor") return "Submitted";
  if (normalized === "Draft") return "Draft";
  return valueOrDash(status);
}

function statusTone(status?: string | null): string {
  const normalized = normalizeStatus(status);
  if (normalized === "UnderBoardReview") return "role-pill role-pill--info";
  if (normalized === "Approved")
    return "role-pill role-pill--success";
  if (normalized === "NeedsRevision") return "role-pill role-pill--warning";
  if (normalized === "Rejected") return "role-pill role-pill--danger";
  if (normalized === "SubmittedToEditor") return "role-pill role-pill--neutral";
  return "role-pill role-pill--neutral";
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Không thể tải dữ liệu.";
}

function compareNewestProposal(left: MangaProposal, right: MangaProposal) {
  return proposalTime(right) - proposalTime(left);
}

function proposalTime(proposal: MangaProposal) {
  const candidate =
    proposal.submittedAt ?? proposal.updatedAt ?? proposal.manuscriptUploadedAt;
  const parsed = candidate ? new Date(candidate).getTime() : Number.NaN;
  if (!Number.isNaN(parsed)) return parsed;
  const numericId = Number(proposal.id);
  return Number.isNaN(numericId) ? 0 : numericId;
}

export function TantouEditorDashboard({
  session,
  onLogout,
}: TantouEditorDashboardProps) {
  const [proposals, setProposals] = useState<MangaProposal[]>([]);
  const [selectedProposalId, setSelectedProposalId] = useState<string | null>(
    null,
  );
  const [note, setNote] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [activeSection, setActiveSection] = useState<EditorSection>("proposals");
  const [activeAction, setActiveAction] = useState<
    "forward" | "revision" | null
  >(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const sortedProposals = useMemo(
    () => [...proposals].sort(compareNewestProposal),
    [proposals],
  );

  const selectedProposal = useMemo(
    () =>
      sortedProposals.find((proposal) => proposal.id === selectedProposalId) ??
      null,
    [sortedProposals, selectedProposalId],
  );

  const groupedProposals = useMemo(
    () =>
      BUCKETS.map((bucket) => ({
        ...bucket,
        proposals: sortedProposals.filter((proposal) => {
          const normalized = normalizeStatus(proposal.status);
          return normalized ? bucket.statuses.includes(normalized) : false;
        }),
      })),
    [sortedProposals],
  );

  const submittedCount =
    groupedProposals.find((bucket) => bucket.key === "submitted")?.proposals
      .length ?? 0;
  const boardCount =
    groupedProposals.find((bucket) => bucket.key === "board")?.proposals
      .length ?? 0;
  const needsWorkCount =
    groupedProposals.find((bucket) => bucket.key === "needsWork")?.proposals
      .length ?? 0;

  async function loadProposals(options: { silent?: boolean } = {}) {
    const silent = options.silent === true;
    if (!silent) {
      setIsLoading(true);
      setErrorMessage(null);
    }

    try {
      const loaded = (await listTantouEditorProposals(session.user.email)).sort(
        compareNewestProposal,
      );
      setProposals(loaded);
      setSelectedProposalId((current) =>
        current && loaded.some((proposal) => proposal.id === current)
          ? current
          : (loaded[0]?.id ?? null),
      );
    } catch (error) {
      if (!silent) setErrorMessage(getErrorMessage(error));
    } finally {
      if (!silent) setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadProposals();
    const refreshId = window.setInterval(
      () => void loadProposals({ silent: true }),
      5000,
    );
    return () => window.clearInterval(refreshId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session.user.email]);

  useEffect(() => {
    setNote("");
  }, [selectedProposalId]);

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
              editorEmail: session.user.email,
              editorNote: note.trim() || null,
              editorReviewedAt: new Date().toISOString(),
            }
          : proposal,
      ),
    );
  }

  async function handleAction(
    action: "forward" | "revision",
    request: (
      proposalId: string,
      payload: { editorEmail: string; note: string },
    ) => Promise<MangaProposal | null>,
    successLabel: string,
    fallbackStatus: MangaProposalStatus,
  ) {
    if (!selectedProposal) return;
    if (action === "revision" && note.trim().length === 0) {
      setErrorMessage("Please write a revision note for the mangaka.");
      return;
    }

    setActiveAction(action);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const updated = await request(selectedProposal.id, {
        editorEmail: session.user.email,
        note: action === "revision" ? note.trim() : "",
      });

      mergeUpdatedProposal(updated, fallbackStatus);
      setSuccessMessage(successLabel);
      setNote("");
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
      className="board-page board-page--editor"
      id="tantou-editor-dashboard"
    >
      <div className="editor-dashboard">
        <section className="dashboard-hero editor-hero glass-card">
          <div className="dashboard-hero__copy">
            <span className="eyebrow">Tantou Editor workspace</span>
            <h1>Screen proposals before they reach the board.</h1>
            <p>
              Review the first pass, either request author revision with a note
              or forward the proposal to the Editorial Board.
            </p>
          </div>

          <div className="dashboard-hero__meta">
            <div className="dashboard-chip dashboard-chip--accent">
              <span>Editor</span>
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

        <section className="editor-summary-grid" aria-label="Proposal summary">
          <article className="role-metric-card">
            <span>Submitted</span>
            <strong>{submittedCount}</strong>
            <p>New proposals waiting for your editorial review.</p>
          </article>
          <article className="role-metric-card role-metric-card--accent">
            <span>Forwarded to Board</span>
            <strong>{boardCount}</strong>
            <p>Proposals already approved by the editor and in board review.</p>
          </article>
          <article className="role-metric-card role-metric-card--soft">
            <span>Revision / Completed</span>
            <strong>{needsWorkCount}</strong>
            <p>Items that need another pass or have been closed out.</p>
          </article>
        </section>

        {/* Section tabs */}
        <div
          style={{
            display: "flex",
            gap: "4px",
            marginBottom: "16px",
            background: "rgba(255,255,255,0.04)",
            borderRadius: "12px",
            padding: "4px",
            border: "1px solid rgba(148,163,184,0.16)",
          }}
        >
          {(["proposals", "production"] as EditorSection[]).map(
            (section) => (
              <button
                key={section}
                type="button"
                onClick={() => setActiveSection(section)}
                style={{
                  flex: 1,
                  padding: "8px 16px",
                  border: "none",
                  borderRadius: "8px",
                  background:
                    activeSection === section
                      ? "rgba(59,130,246,0.2)"
                      : "transparent",
                  color:
                    activeSection === section ? "#60a5fa" : "#94a3b8",
                  fontWeight: activeSection === section ? 600 : 400,
                  fontSize: "13px",
                  cursor: "pointer",
                  transition: "all 0.15s ease",
                }}
              >
                {section === "proposals" ? "Proposals" : "Production Review"}
              </button>
            ),
          )}
        </div>

        {activeSection === "production" && (
          <section className="panel-card" style={{ padding: "20px" }}>
            <div className="panel-card__header panel-card__header--stacked" style={{ marginBottom: "16px" }}>
              <div>
                <span className="eyebrow">Page Annotations</span>
                <h3>Review production pages</h3>
                <p>View pages from approved series and add markup annotations for the mangaka.</p>
              </div>
            </div>
            <PageAnnotations
              pageId="601"
              pageImageUrl="/api/pages/601/image"
              editorEmail={session.user.email}
            />
          </section>
        )}

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

        {activeSection === "proposals" && (
        <section className="editor-workflow">
          <div className="editor-queue panel-card">
            <div className="panel-card__header panel-card__header--stacked">
              <div>
                <span className="eyebrow">Review queue</span>
                <h3>Pick a proposal to inspect</h3>
                <p>
                  One queue, three editorial outcomes. Keep the pass clean and
                  decisive.
                </p>
              </div>
              <button
                type="button"
                className="button button-secondary"
                onClick={() => void loadProposals()}
                disabled={isLoading}
              >
                {isLoading ? "Refreshing..." : "Refresh queue"}
              </button>
            </div>

            {isLoading ? (
              <div className="board-empty board-empty--compact">
                Loading proposals…
              </div>
            ) : groupedProposals.every(
                (bucket) => bucket.proposals.length === 0,
              ) ? (
              <div className="board-empty board-empty--compact">
                No proposals are waiting for editor review.
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

          <article className="panel-card editor-detail" id="editorial-detail">
            <div className="panel-card__header panel-card__header--stacked">
              <div>
                <span className="eyebrow">Proposal detail</span>
                <h3>
                  {selectedProposal
                    ? selectedProposal.title
                    : "Select a proposal"}
                </h3>
                <p>
                  {selectedProposal
                    ? selectedProposal.synopsis
                    : "Choose a proposal from the queue to review the full pitch."}
                </p>
              </div>
              {selectedProposal && (
                <span className={statusTone(selectedProposal.status)}>
                  {statusLabel(selectedProposal.status)}
                </span>
              )}
            </div>

            {selectedProposal ? (
              <>
                <div className="editor-detail__grid">
                  <div className="editor-detail__item">
                    <span>Genre</span>
                    <strong>{valueOrDash(selectedProposal.genre)}</strong>
                  </div>
                  <div className="editor-detail__item">
                    <span>Author</span>
                    <strong>{valueOrDash(selectedProposal.authorEmail)}</strong>
                  </div>
                  <div className="editor-detail__item editor-detail__item--file">
                    <span>Manuscript file</span>
                    <strong
                      title={selectedProposal.manuscriptFileName ?? undefined}
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
                  <div className="editor-detail__item">
                    <span>Submitted</span>
                    <strong>{formatDate(selectedProposal.submittedAt)}</strong>
                  </div>
                  <div className="editor-detail__item">
                    <span>Updated</span>
                    <strong>{formatDate(selectedProposal.updatedAt)}</strong>
                  </div>
                </div>

                <div className="editor-detail__block">
                  <span>Synopsis</span>
                  <p>{selectedProposal.synopsis}</p>
                </div>

                <label className="editor-note-field">
                  <span>Revision note for Mangaka</span>
                  <textarea
                    rows={5}
                    value={note}
                    onChange={(event) => setNote(event.target.value)}
                    placeholder="Required only when requesting a revision from the mangaka. Forwarding to the board does not send this note."
                  />
                </label>

                <div className="editor-actions">
                  <button
                    type="button"
                    className="button button-primary"
                    onClick={() =>
                      void handleAction(
                        "forward",
                        forwardProposalToBoard,
                        "Proposal forwarded to the board.",
                        "UnderBoardReview",
                      )
                    }
                    disabled={activeAction !== null}
                  >
                    {activeAction === "forward"
                      ? "Forwarding..."
                      : "Forward to Board"}
                  </button>
                  <button
                    type="button"
                    className="button button-revision"
                    onClick={() =>
                      void handleAction(
                        "revision",
                        requestProposalRevision,
                        "Revision note sent to the mangaka.",
                        "NeedsRevision",
                      )
                    }
                    disabled={activeAction !== null}
                  >
                    {activeAction === "revision"
                      ? "Sending..."
                      : "Request revision from author"}
                  </button>
                </div>
              </>
            ) : (
              <div className="board-empty board-empty--detail">
                Select a proposal to inspect and decide its next step.
              </div>
            )}
          </article>
        </section>
        )}
      </div>
    </main>
  );
}
