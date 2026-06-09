import { FormEvent, useEffect, useMemo, useState } from "react";
import { logout } from "../services/authService";
import {
  listAssistantTasks,
  startAssistantTask,
  submitAssistantTask,
} from "../services/assistantTaskService";
import type { LoginResponse } from "../types/auth";
import type { AssistantTask, AssistantTaskStatus } from "../types/assistant";

interface AssistantDashboardProps {
  session: LoginResponse;
  onLogout?: () => void;
}

interface SubmitFormState {
  submittedFileName: string;
  submissionNote: string;
}

type TaskBucket = "pending" | "progress" | "submitted";

const EMPTY_SUBMIT_FORM: SubmitFormState = {
  submittedFileName: "",
  submissionNote: "",
};

const BUCKETS: Array<{
  key: TaskBucket;
  label: string;
  statuses: AssistantTaskStatus[];
  description: string;
}> = [
  {
    key: "pending",
    label: "Pending / Redo Requested",
    statuses: ["Pending", "RedoRequested"],
    description: "Tasks waiting for first action or another pass.",
  },
  {
    key: "progress",
    label: "In Progress",
    statuses: ["InProgress"],
    description: "Tasks already started and ready for submission.",
  },
  {
    key: "submitted",
    label: "Submitted / Approved",
    statuses: ["Submitted", "Approved"],
    description: "Completed work and final review states.",
  },
];

function normalizeStatus(status?: string | null): AssistantTaskStatus {
  const value = (status ?? "Pending").toLowerCase().replace(/[\s_-]/g, "");
  if (value.includes("redo")) return "RedoRequested";
  if (
    value.includes("progress") ||
    value.includes("started") ||
    value.includes("inprogress")
  )
    return "InProgress";
  if (value.includes("approved")) return "Approved";
  if (value.includes("submitted") || value.includes("complete"))
    return "Submitted";
  return "Pending";
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

function statusLabel(status?: string | null): string {
  const normalized = normalizeStatus(status);
  if (normalized === "RedoRequested") return "Redo Requested";
  if (normalized === "InProgress") return "In Progress";
  if (normalized === "Submitted") return "Submitted";
  if (normalized === "Approved") return "Approved";
  return "Pending";
}

function statusTone(status?: string | null): string {
  const normalized = normalizeStatus(status);
  if (normalized === "Approved")
    return "assistant-pill assistant-pill--success";
  if (normalized === "Submitted") return "assistant-pill assistant-pill--info";
  if (normalized === "InProgress")
    return "assistant-pill assistant-pill--warning";
  if (normalized === "RedoRequested")
    return "assistant-pill assistant-pill--danger";
  return "assistant-pill assistant-pill--neutral";
}

function taskContext(task: AssistantTask) {
  return {
    proposal:
      task.proposalTitle ??
      task.proposal?.title ??
      valueOrDash(task.proposalId),
    chapter:
      task.chapterTitle ??
      (task.chapterNumber !== null && task.chapterNumber !== undefined
        ? `Chapter ${task.chapterNumber}`
        : (task.chapter?.title ?? valueOrDash(task.chapterId))),
    page:
      task.pageNumber !== null && task.pageNumber !== undefined
        ? `Page ${task.pageNumber}`
        : task.page?.pageNumber !== null && task.page?.pageNumber !== undefined
          ? `Page ${task.page.pageNumber}`
          : (task.page?.fileName ?? valueOrDash(task.pageId)),
    region:
      task.regionType ?? task.region?.regionType ?? valueOrDash(task.regionId),
    referenceFile: task.referenceFileName,
    startedAt: formatDate(task.startedAt),
    submittedAt: formatDate(task.submittedAt),
    reviewedAt: formatDate(task.reviewedAt),
    updatedAt: formatDate(task.updatedAt),
  };
}

export function AssistantDashboard({
  session,
  onLogout,
}: AssistantDashboardProps) {
  const [tasks, setTasks] = useState<AssistantTask[]>([]);
  const [selectedTaskId, setSelectedTaskId] = useState<string | null>(null);
  const [submitForm, setSubmitForm] =
    useState<SubmitFormState>(EMPTY_SUBMIT_FORM);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [activeTaskAction, setActiveTaskAction] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  async function loadTasks() {
    setErrorMessage(null);
    setIsLoading(true);

    try {
      const loaded = await listAssistantTasks(session.user.email);
      setTasks(loaded);
      setSelectedTaskId((current) =>
        current && loaded.some((task) => task.id === current)
          ? current
          : (loaded[0]?.id ?? null),
      );
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Không thể tải danh sách task.",
      );
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }

  useEffect(() => {
    void loadTasks();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session.user.email]);

  const selectedTask = useMemo(
    () => tasks.find((task) => task.id === selectedTaskId) ?? null,
    [selectedTaskId, tasks],
  );
  const groupedTasks = useMemo(
    () =>
      BUCKETS.map((bucket) => ({
        ...bucket,
        tasks: tasks.filter((task) =>
          bucket.statuses.includes(normalizeStatus(task.status)),
        ),
      })),
    [tasks],
  );

  const pendingCount =
    groupedTasks.find((bucket) => bucket.key === "pending")?.tasks.length ?? 0;
  const inProgressCount =
    groupedTasks.find((bucket) => bucket.key === "progress")?.tasks.length ?? 0;
  const submittedCount =
    groupedTasks.find((bucket) => bucket.key === "submitted")?.tasks.length ??
    0;
  const approvedOrCompletedCount = tasks.filter((task) =>
    ["Approved", "Submitted"].includes(normalizeStatus(task.status)),
  ).length;
  const redoRequestedCount = tasks.filter(
    (task) => normalizeStatus(task.status) === "RedoRequested",
  ).length;
  const estimatedEarnings = approvedOrCompletedCount * 12;

  useEffect(() => {
    if (!selectedTask) {
      setSubmitForm(EMPTY_SUBMIT_FORM);
      return;
    }

    setSubmitForm((current) => ({
      submittedFileName:
        current.submittedFileName ||
        selectedTask.submittedFileName ||
        selectedTask.referenceFileName,
      submissionNote:
        current.submissionNote || selectedTask.submissionNote || "",
    }));
  }, [selectedTask]);

  async function handleRefresh() {
    setIsRefreshing(true);
    await loadTasks();
  }

  async function handleStartTask(task: AssistantTask) {
    setActiveTaskAction(task.id);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      await startAssistantTask(task.id, { assistantEmail: session.user.email });
      setSuccessMessage(`Task ${task.id} has been started.`);
      await loadTasks();
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Không thể bắt đầu task.",
      );
    } finally {
      setActiveTaskAction(null);
    }
  }

  async function handleSubmitTask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedTask) return;
    if (
      !submitForm.submittedFileName.trim() ||
      !submitForm.submissionNote.trim()
    ) {
      setErrorMessage("Vui lòng nhập tên file đã nộp và ghi chú nộp.");
      return;
    }

    setActiveTaskAction(selectedTask.id);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      await submitAssistantTask(selectedTask.id, {
        assistantEmail: session.user.email,
        submittedFileName: submitForm.submittedFileName.trim(),
        submissionNote: submitForm.submissionNote.trim(),
      });

      setSuccessMessage(`Task ${selectedTask.id} submitted successfully.`);
      setSubmitForm({ submittedFileName: "", submissionNote: "" });
      await loadTasks();
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Không thể gửi task.",
      );
    } finally {
      setActiveTaskAction(null);
    }
  }

  const summaryCards = [
    {
      label: "Pending",
      value: pendingCount,
      hint: "Ready to start or revise.",
    },
    {
      label: "In progress",
      value: inProgressCount,
      hint: "Work currently in motion.",
    },
    {
      label: "Submitted",
      value: submittedCount,
      hint: "Awaiting or finished review.",
    },
  ];

  const taskInfo = selectedTask ? taskContext(selectedTask) : null;
  const canStart = selectedTask
    ? ["Pending", "RedoRequested"].includes(
        normalizeStatus(selectedTask.status),
      )
    : false;
  const canSubmit = selectedTask
    ? normalizeStatus(selectedTask.status) === "InProgress"
    : false;

  function handleLogoutClick() {
    logout();
    onLogout?.();
  }

  return (
    <main className="board-page board-page--assistant" id="assistant-dashboard">
      <div className="assistant-shell">
        <aside
          className="board-rail assistant-rail"
          aria-label="Assistant sidebar"
        >
          <div className="board-rail__brand">
            <div className="board-rail__badge">A</div>
            <div>
              <span className="eyebrow">Assistant</span>
              <strong>{session.user.fullName}</strong>
            </div>
          </div>

          <div className="board-rail__stats">
            <div className="board-stat">
              <span>Workspace</span>
              <strong>Task execution</strong>
            </div>
            <div className="board-stat">
              <span>Email</span>
              <strong>{session.user.email}</strong>
            </div>
          </div>

          <div className="assistant-rail__summary">
            {summaryCards.map((card) => (
              <article className="board-stat" key={card.label}>
                <span>{card.label}</span>
                <strong>{card.value}</strong>
                <p>{card.hint}</p>
              </article>
            ))}
            <article className="board-stat">
              <span>Estimated earnings</span>
              <strong>${estimatedEarnings}</strong>
              <p>
                {approvedOrCompletedCount} approved/submitted tasks × $12 demo
                rate
              </p>
            </article>
            <article className="board-stat">
              <span>Redo requested</span>
              <strong>{redoRequestedCount}</strong>
              <p>Tasks waiting for another pass.</p>
            </article>
          </div>

          <nav
            className="board-rail__nav"
            aria-label="Assistant workspace navigation"
          >
            <a href="#assistant-board">
              <span>Task board</span>
              <strong>{tasks.length}</strong>
            </a>
            <a href="#assistant-detail">
              <span>Selection</span>
              <strong>
                {selectedTask ? selectedTask.id.slice(0, 8) : "—"}
              </strong>
            </a>
          </nav>

          <div className="assistant-rail__actions">
            <button
              type="button"
              className="button button-secondary"
              onClick={handleRefresh}
              disabled={isRefreshing}
            >
              {isRefreshing ? "Refreshing…" : "Refresh tasks"}
            </button>
            <button
              type="button"
              className="button button-secondary"
              onClick={handleLogoutClick}
            >
              Log out
            </button>
          </div>
        </aside>

        <section className="assistant-main">
          <header className="panel-card assistant-hero">
            <div className="panel-card__header panel-card__header--stacked">
              <div>
                <span className="eyebrow">Assistant workspace</span>
                <h1>
                  Deliver assigned tasks with clear context and fast handoff.
                </h1>
                <p>
                  Review the production context, start work when you are ready,
                  and submit directly from this focused workspace.
                </p>
              </div>
              <div className="assistant-hero__chips">
                <div className="dashboard-chip dashboard-chip--accent">
                  <span>Pending</span>
                  <strong>{pendingCount}</strong>
                </div>
                <div className="dashboard-chip dashboard-chip--accent">
                  <span>In progress</span>
                  <strong>{inProgressCount}</strong>
                </div>
                <div className="dashboard-chip dashboard-chip--accent">
                  <span>Submitted</span>
                  <strong>{submittedCount}</strong>
                </div>
                <div className="dashboard-chip dashboard-chip--accent">
                  <span>Earnings</span>
                  <strong>${estimatedEarnings}</strong>
                </div>
              </div>
            </div>
            <div className="assistant-hero__summary">
              {summaryCards.map((card) => (
                <article className="assistant-summary-card" key={card.label}>
                  <span>{card.label}</span>
                  <strong>{card.value}</strong>
                  <p>{card.hint}</p>
                </article>
              ))}
            </div>
          </header>

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

          <div className="assistant-layout" id="assistant-board">
            <section className="assistant-board">
              {groupedTasks.map((bucket) => (
                <article
                  className="panel-card assistant-column"
                  key={bucket.key}
                >
                  <div className="panel-card__header panel-card__header--stacked">
                    <div>
                      <span className="eyebrow">{bucket.label}</span>
                      <h3>{bucket.tasks.length} tasks</h3>
                      <p>{bucket.description}</p>
                    </div>
                  </div>

                  <div className="assistant-column__list">
                    {isLoading ? (
                      <div className="assistant-empty assistant-empty--loading">
                        Loading tasks…
                      </div>
                    ) : bucket.tasks.length === 0 ? (
                      <div className="assistant-empty">
                        <strong>No tasks</strong>
                        <p>Nothing to do in this lane right now.</p>
                      </div>
                    ) : (
                      bucket.tasks.map((task) => {
                        const isSelected = selectedTaskId === task.id;
                        const context = taskContext(task);
                        return (
                          <button
                            key={task.id}
                            type="button"
                            className={`assistant-task-card ${isSelected ? "is-selected" : ""}`}
                            onClick={() => setSelectedTaskId(task.id)}
                          >
                            <div className="assistant-task-card__header">
                              <div>
                                <span className={statusTone(task.status)}>
                                  {statusLabel(task.status)}
                                </span>
                                <strong>{task.taskType}</strong>
                              </div>
                              <span className="assistant-task-card__id">
                                {task.id.slice(0, 8)}
                              </span>
                            </div>
                            <p>{context.proposal}</p>
                            <div className="assistant-task-card__meta">
                              <span>{context.chapter}</span>
                              <span>{context.page}</span>
                              <span>{context.region}</span>
                            </div>
                          </button>
                        );
                      })
                    )}
                  </div>
                </article>
              ))}
            </section>

            <article
              className="panel-card assistant-detail"
              id="assistant-detail"
            >
              <div className="panel-card__header panel-card__header--stacked">
                <div>
                  <span className="eyebrow">Task detail</span>
                  <h3>
                    {selectedTask ? selectedTask.taskType : "Select a task"}
                  </h3>
                  <p>
                    {selectedTask
                      ? selectedTask.instructions
                      : "Choose a task from any lane to review the full context."}
                  </p>
                </div>
                {selectedTask && (
                  <span className={statusTone(selectedTask.status)}>
                    {statusLabel(selectedTask.status)}
                  </span>
                )}
              </div>

              {selectedTask && taskInfo ? (
                <>
                  <div className="assistant-detail__grid">
                    <div className="assistant-detail__item">
                      <span>Proposal</span>
                      <strong>{taskInfo.proposal}</strong>
                    </div>
                    <div className="assistant-detail__item">
                      <span>Chapter</span>
                      <strong>{taskInfo.chapter}</strong>
                    </div>
                    <div className="assistant-detail__item">
                      <span>Page</span>
                      <strong>{taskInfo.page}</strong>
                    </div>
                    <div className="assistant-detail__item">
                      <span>Region</span>
                      <strong>{taskInfo.region}</strong>
                    </div>
                    <div className="assistant-detail__item">
                      <span>Reference file</span>
                      <strong>{taskInfo.referenceFile}</strong>
                    </div>
                    <div className="assistant-detail__item">
                      <span>Task type</span>
                      <strong>{selectedTask.taskType}</strong>
                    </div>
                    <div className="assistant-detail__item">
                      <span>Started</span>
                      <strong>{taskInfo.startedAt}</strong>
                    </div>
                    <div className="assistant-detail__item">
                      <span>Submitted</span>
                      <strong>{taskInfo.submittedAt}</strong>
                    </div>
                  </div>

                  <div className="assistant-instructions">
                    <span>Instructions</span>
                    <p>{selectedTask.instructions}</p>
                  </div>

                  <div className="assistant-detail__meta-row">
                    <span>Task ID: {selectedTask.id}</span>
                    <span>Updated: {taskInfo.updatedAt}</span>
                    <span>Reviewed: {taskInfo.reviewedAt}</span>
                  </div>

                  <div className="assistant-actions">
                    <button
                      type="button"
                      className="primary-button"
                      onClick={() => void handleStartTask(selectedTask)}
                      disabled={
                        !canStart || activeTaskAction === selectedTask.id
                      }
                    >
                      {activeTaskAction === selectedTask.id
                        ? "Working…"
                        : "Start task"}
                    </button>
                    <div className="assistant-actions__note">
                      {canStart
                        ? "Start the task when you are ready to begin."
                        : "Starting is only available for pending or redo tasks."}
                    </div>
                  </div>

                  <form
                    className="assistant-submit-form"
                    onSubmit={handleSubmitTask}
                  >
                    <div className="panel-card panel-card--inner">
                      <div className="panel-card__header panel-card__header--stacked">
                        <div>
                          <span className="eyebrow">Submit work</span>
                          <h4>Send a completed file back to the team</h4>
                        </div>
                      </div>
                      <div className="form-grid form-grid--two">
                        <label className="admin-field">
                          <span>Submitted file name</span>
                          <input
                            value={submitForm.submittedFileName}
                            onChange={(event) =>
                              setSubmitForm({
                                ...submitForm,
                                submittedFileName: event.target.value,
                              })
                            }
                            placeholder="assistant-output-v1.pdf"
                          />
                        </label>
                        <label className="admin-field">
                          <span>Submission note</span>
                          <input
                            value={submitForm.submissionNote}
                            onChange={(event) =>
                              setSubmitForm({
                                ...submitForm,
                                submissionNote: event.target.value,
                              })
                            }
                            placeholder="Short note about what was completed"
                          />
                        </label>
                      </div>
                      <div className="assistant-submit-form__footer">
                        <p>
                          Include the actual file name you delivered and a short
                          note for reviewers. This stays focused on the handoff
                          metadata.
                        </p>
                        <button
                          type="submit"
                          className="primary-button"
                          disabled={
                            !canSubmit || activeTaskAction === selectedTask.id
                          }
                        >
                          {activeTaskAction === selectedTask.id
                            ? "Submitting…"
                            : "Submit work"}
                        </button>
                      </div>
                    </div>
                  </form>
                </>
              ) : (
                <div className="assistant-empty assistant-empty--detail">
                  <strong>No task selected</strong>
                  <p>
                    Choose any task from the board to see context, start it, or
                    submit your work.
                  </p>
                </div>
              )}
            </article>
          </div>
        </section>
      </div>
    </main>
  );
}
