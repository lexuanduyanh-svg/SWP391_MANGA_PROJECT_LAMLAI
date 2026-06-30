import { FormEvent, useEffect, useMemo, useState } from "react";
import { logout } from "../services/authService";
import {
  createMangakaProposal,
  deleteMangakaProposal,
  listMangakaProposals,
  submitMangakaProposal,
  updateMangakaProposal,
  uploadMangakaManuscript,
} from "../services/mangakaProposalService";
import {
  createMangakaChapter,
  createMangakaPage,
  createMangakaTask,
  approveMangakaTask,
  redoMangakaTask,
  listMangakaChapters,
  createMangakaRegion,
  deleteMangakaRegion,
} from "../services/mangakaProductionService";
import type { LoginResponse } from "../types/auth";
import type {
  MangaProductionChapter,
  MangaProductionPage,
  MangaProductionTask,
  MangaProductionRegion,
  MangaProposal,
  MangaProposalStatus,
} from "../types/mangaka";
import VisualCanvas, { type RegionRect } from "./VisualCanvas";

interface MangakaDashboardProps {
  session: LoginResponse;
  onLogout?: () => void;
}

interface ProposalFormState {
  title: string;
  genre: string;
  targetAudience: string;
  synopsis: string;
  manuscriptFileName: string;
  manuscriptVersion: string;
}

interface ChapterFormState {
  title: string;
  chapterNumber: string;
  summary: string;
}

interface PageFormState {
  pageNumber: string;
  fileName: string;
  notes: string;
}

interface TaskFormState {
  assistantEmail: string;
  instructions: string;
  deadline: string;
}

interface RegionFormState {
  regionType: string;
  note: string;
}

type ActiveView =
  | "overall"
  | "proposals"
  | "review"
  | "proposalForm"
  | "submit"
  | "production"
  | "chapter"
  | "page"
  | "region"
  | "task"
  | "inspect";

const EMPTY_PROPOSAL_FORM: ProposalFormState = {
  title: "",
  genre: "",
  targetAudience: "General",
  synopsis: "",
  manuscriptFileName: "",
  manuscriptVersion: "v1",
};

const EMPTY_CHAPTER_FORM: ChapterFormState = {
  title: "",
  chapterNumber: "",
  summary: "",
};
const EMPTY_PAGE_FORM: PageFormState = {
  pageNumber: "",
  fileName: "",
  notes: "",
};
const EMPTY_TASK_FORM: TaskFormState = {
  assistantEmail: "assistant@manga.local",
  instructions: "",
  deadline: "",
};

const EMPTY_REGION_FORM: RegionFormState = {
  regionType: "panel",
  note: "",
};

const DRAFT_LANES: Array<{
  label: string;
  statuses: MangaProposalStatus[];
  emptyHint: string;
}> = [
  {
    label: "Draft",
    statuses: ["Draft"],
    emptyHint: "Use New proposal to create one.",
  },
  {
    label: "Needs revision",
    statuses: ["NeedsRevision"],
    emptyHint: "Revision requests from Tantou Editor will appear here.",
  },
];

const REVIEW_LANES: Array<{
  label: string;
  statuses: MangaProposalStatus[];
  emptyHint: string;
}> = [
  {
    label: "Submitted to Editor",
    statuses: ["SubmittedToEditor"],
    emptyHint: "Submitted proposals wait here for Tantou Editor.",
  },
  {
    label: "Under Board Review",
    statuses: ["UnderBoardReview"],
    emptyHint: "Forwarded proposals wait here for Editorial Board.",
  },
  {
    label: "Rejected",
    statuses: ["Rejected"],
    emptyHint: "Rejected proposals appear here with reviewer notes.",
  },
  {
    label: "Approved",
    statuses: ["Approved"],
    emptyHint: "Approved proposals move into Production.",
  },
];

export function MangakaDashboard({ session, onLogout }: MangakaDashboardProps) {
  const [proposals, setProposals] = useState<MangaProposal[]>([]);
  const [proposalQuery, setProposalQuery] = useState("");
  const [proposalForm, setProposalForm] =
    useState<ProposalFormState>(EMPTY_PROPOSAL_FORM);
  const [editingProposalId, setEditingProposalId] = useState<string | null>(
    null,
  );
  const [selectedBoardProposalId, setSelectedBoardProposalId] = useState<
    string | null
  >(null);
  const [selectedProductionProposalId, setSelectedProductionProposalId] =
    useState<string | null>(null);
  const [productionByProposal, setProductionByProposal] = useState<
    Record<string, MangaProductionChapter[]>
  >({});
  const [selectedChapterId, setSelectedChapterId] = useState<string | null>(
    null,
  );
  const [selectedPageId, setSelectedPageId] = useState<string | null>(null);
  const [selectedRegionId, setSelectedRegionId] = useState<string | null>(null);
  const [drawingMode, setDrawingMode] = useState(false);
  const [regionForm, setRegionForm] = useState<RegionFormState>(EMPTY_REGION_FORM);
  const [regions, setRegions] = useState<MangaProductionRegion[]>([]);
  const [chapterForm, setChapterForm] =
    useState<ChapterFormState>(EMPTY_CHAPTER_FORM);
  const [pageForm, setPageForm] = useState<PageFormState>(EMPTY_PAGE_FORM);
  const [taskForm, setTaskForm] = useState<TaskFormState>(EMPTY_TASK_FORM);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isUploadingManuscript, setIsUploadingManuscript] = useState(false);
  const [pendingManuscriptFile, setPendingManuscriptFile] =
    useState<File | null>(null);
  const [isSubmitting, setIsSubmitting] = useState<string | null>(null);
  const [deletingProposalId, setDeletingProposalId] = useState<string | null>(
    null,
  );
  const [isProductionLoading, setIsProductionLoading] = useState(false);
  const [productionAction, setProductionAction] = useState<
    "chapter" | "page" | "task" | null
  >(null);
  const [activeView, setActiveView] = useState<ActiveView>("overall");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const selectedProposal = useMemo(
    () =>
      proposals.find((proposal) => proposal.id === editingProposalId) ?? null,
    [editingProposalId, proposals],
  );

  const selectedBoardProposal = useMemo(
    () =>
      proposals.find((proposal) => proposal.id === selectedBoardProposalId) ??
      null,
    [selectedBoardProposalId, proposals],
  );

  const eligibleProductionProposals = useMemo(
    () =>
      proposals
        .filter((proposal) => isProductionReady(proposal.status))
        .sort((left, right) =>
          compareDates(
            right.updatedAt ?? right.submittedAt,
            left.updatedAt ?? left.submittedAt,
          ),
        ),
    [proposals],
  );

  const activeProductionProposal = useMemo(
    () =>
      proposals.find(
        (proposal) => proposal.id === selectedProductionProposalId,
      ) ?? null,
    [proposals, selectedProductionProposalId],
  );

  const productionChapters = useMemo(
    () =>
      selectedProductionProposalId
        ? (productionByProposal[selectedProductionProposalId] ?? [])
        : [],
    [productionByProposal, selectedProductionProposalId],
  );

  const selectedChapter = useMemo(
    () =>
      productionChapters.find((chapter) => chapter.id === selectedChapterId) ??
      null,
    [productionChapters, selectedChapterId],
  );

  const selectedPage = useMemo(
    () => findPage(selectedChapter, selectedPageId),
    [selectedChapter, selectedPageId],
  );
  const visibleProposals = useMemo(() => {
    const query = proposalQuery.trim().toLowerCase();
    if (!query) return proposals;

    return proposals.filter((proposal) => {
      const haystack = [
        proposal.title,
        proposal.genre,
        proposal.targetAudience,
        proposal.synopsis,
        formatStatus(proposal.status),
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();
      return haystack.includes(query);
    });
  }, [proposalQuery, proposals]);

  const totalApproved = proposals.filter(
    (proposal) => proposal.status === "Approved",
  ).length;
  const totalSubmitted = proposals.filter(
    (proposal) => proposal.status === "SubmittedToEditor",
  ).length;
  const totalRejected = proposals.filter(
    (proposal) => proposal.status === "Rejected",
  ).length;
  const draftWorkCount = proposals.filter(
    (proposal) =>
      proposal.status === "Draft" || proposal.status === "NeedsRevision",
  ).length;
  const productionReadyCount = totalApproved;
  const visibleDraftCount = visibleProposals.filter(
    (proposal) =>
      proposal.status === "Draft" || proposal.status === "NeedsRevision",
  ).length;
  const recentProposals = useMemo(
    () =>
      [...proposals]
        .sort((left, right) =>
          compareDates(
            right.updatedAt ?? right.submittedAt,
            left.updatedAt ?? left.submittedAt,
          ),
        )
        .slice(0, 5),
    [proposals],
  );

  const activeViewLabel: Record<ActiveView, string> = {
    overall: "Overall dashboard",
    proposals: "Drafts",
    review: "Review Status",
    proposalForm: selectedProposal ? "Edit proposal" : "New proposal",
    submit: "Submit to Editor",
    production: "Production",
    chapter: "Chapter",
    page: "Page",
    region: "Region",
    task: "Task",
    inspect: "View Structure",
  };

  useEffect(() => {
    let mounted = true;

    async function loadProposals() {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const loaded = await listMangakaProposals(session.user.email);
        if (mounted) {
          setProposals(loaded);
        }
      } catch (error) {
        if (mounted) setErrorMessage(getErrorMessage(error));
      } finally {
        if (mounted) setIsLoading(false);
      }
    }

    loadProposals();
    return () => {
      mounted = false;
    };
  }, [session.user.email]);

  useEffect(() => {
    if (selectedProposal) {
      setProposalForm({
        title: selectedProposal.title,
        genre: selectedProposal.genre,
        targetAudience: selectedProposal.targetAudience || "General",
        synopsis: selectedProposal.synopsis,
        manuscriptFileName: selectedProposal.manuscriptFileName ?? "",
        manuscriptVersion: String(selectedProposal.manuscriptVersion ?? "v1"),
      });
      return;
    }
    setProposalForm(EMPTY_PROPOSAL_FORM);
  }, [selectedProposal]);

  useEffect(() => {
    if (eligibleProductionProposals.length === 0) {
      setSelectedProductionProposalId(null);
      setSelectedChapterId(null);
      setSelectedPageId(null);
      return;
    }

    if (
      !selectedProductionProposalId ||
      !eligibleProductionProposals.some(
        (proposal) => proposal.id === selectedProductionProposalId,
      )
    ) {
      setSelectedProductionProposalId(eligibleProductionProposals[0].id);
    }
  }, [eligibleProductionProposals, selectedProductionProposalId]);

  useEffect(() => {
    let mounted = true;

    async function loadProduction() {
      if (!selectedProductionProposalId || !activeProductionProposal) return;

      const seriesId = activeProductionProposal.seriesId || selectedProductionProposalId;
      setIsProductionLoading(true);
      setErrorMessage(null);

      try {
        const loaded = await listMangakaChapters(
          seriesId,
          session.user.email,
        );
        if (!mounted) return;

        setProductionByProposal((current) => ({
          ...current,
          [selectedProductionProposalId]: mergeChapters(
            current[selectedProductionProposalId] ?? [],
            loaded,
          ),
        }));
      } catch (error) {
        if (mounted) setErrorMessage(getErrorMessage(error));
      } finally {
        if (mounted) setIsProductionLoading(false);
      }
    }

    loadProduction();
    return () => {
      mounted = false;
    };
  }, [selectedProductionProposalId, session.user.email]);

  useEffect(() => {
    if (!selectedChapter) {
      setSelectedPageId(null);
      return;
    }

    setSelectedPageId(selectedChapter.pages?.[0]?.id ?? null);
  }, [selectedChapter]);

  async function handleSaveProposal(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const submitter = (event.nativeEvent as SubmitEvent)
      .submitter as HTMLButtonElement | null;
    const shouldSubmitAfterSave = submitter?.dataset.submitAfterSave === "true";

    if (!validateProposalForm(proposalForm, pendingManuscriptFile)) {
      setErrorMessage(
        "Please fill in the proposal title, genre, synopsis, and choose a manuscript file.",
      );
      return;
    }

    setIsSaving(true);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      let manuscriptFileName = proposalForm.manuscriptFileName;
      let uploadedOriginalName: string | null = null;

      if (pendingManuscriptFile) {
        setIsUploadingManuscript(true);
        const uploaded = await uploadMangakaManuscript(pendingManuscriptFile);
        manuscriptFileName = uploaded.fileName;
        uploadedOriginalName = uploaded.originalFileName;
      }

      const payload = { ...proposalForm, manuscriptFileName };
      const saved = selectedProposal
        ? await updateMangakaProposal(
            selectedProposal.id,
            session.user.email,
            payload,
          )
        : await createMangakaProposal({
            authorEmail: session.user.email,
            ...payload,
          });
      const finalProposal =
        shouldSubmitAfterSave && canSubmitProposal(saved.status)
          ? await submitMangakaProposal(saved.id, {
              authorEmail: session.user.email,
            })
          : saved;

      setPendingManuscriptFile(null);
      setProposalForm((current) => ({
        ...current,
        manuscriptFileName:
          finalProposal.manuscriptFileName ?? manuscriptFileName,
      }));
      setProposals((current) => upsertProposal(current, finalProposal));
      setEditingProposalId(finalProposal.id);
      if (isProductionReady(finalProposal.status))
        setSelectedProductionProposalId(finalProposal.id);
      if (shouldSubmitAfterSave) {
        setSelectedBoardProposalId(null);
        notifySuccess(
          uploadedOriginalName
            ? `Uploaded ${uploadedOriginalName}, saved revision, and submitted to Tantou Editor.`
            : "Proposal saved and submitted to Tantou Editor.",
        );
        setActiveView("review");
      } else {
        notifySuccess(
          uploadedOriginalName
            ? `Uploaded ${uploadedOriginalName} and updated proposal.`
            : selectedProposal
              ? "Proposal updated successfully."
              : "Proposal created successfully.",
        );
        setActiveView("proposals");
      }
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsSaving(false);
      setIsUploadingManuscript(false);
    }
  }

  function handleManuscriptUpload(file: File | null) {
    setPendingManuscriptFile(file);
    setErrorMessage(null);
    setSuccessMessage(null);

    if (!file) return;

    setProposalForm((current) => ({
      ...current,
      manuscriptFileName: file.name,
    }));
  }

  async function handleReviewTask(
    action: "approve" | "redo",
    task: MangaProductionTask,
  ) {
    const seriesId = activeProductionProposal?.seriesId || activeProductionProposal?.id;
    if (!seriesId || !selectedChapter || !selectedPage) return;
    setProductionAction(`${action}-${task.id}` as any);
    try {
      const updated =
        action === "approve"
          ? await approveMangakaTask(
              seriesId,
              selectedChapter.id,
              selectedPage.id,
              task.id,
              session.user.email,
            )
          : await redoMangakaTask(
              seriesId,
              selectedChapter.id,
              selectedPage.id,
              task.id,
              session.user.email,
            );
      setProductionByProposal((current) => ({
        ...current,
        [seriesId]: updateTaskInTree(
          current[seriesId] ?? [],
          selectedChapter.id,
          selectedPage.id,
          updated,
        ),
      }));
      setSuccessMessage(
        action === "approve" ? "Task approved." : "Redo requested for task.",
      );
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Không thể cập nhật task.",
      );
    } finally {
      setProductionAction(null);
    }
  }

  async function handleSubmitProposal(proposal: MangaProposal) {
    if (!proposal.manuscriptFileName) {
      setErrorMessage(
        "Add a manuscript file name before submitting to Tantou Editor.",
      );
      return;
    }

    setIsSubmitting(proposal.id);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const submitted = await submitMangakaProposal(proposal.id, {
        authorEmail: session.user.email,
      });
      setProposals((current) => upsertProposal(current, submitted));
      if (isProductionReady(submitted.status))
        setSelectedProductionProposalId(submitted.id);
      setSelectedBoardProposalId(null);
      notifySuccess("Proposal submitted to Tantou Editor.");
      setActiveView("submit");
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsSubmitting(null);
    }
  }

  async function handleDeleteProposal(proposal: MangaProposal) {
    const confirmed = window.confirm(
      `Delete "${proposal.title}"? Other roles will no longer see this proposal.`,
    );
    if (!confirmed) return;

    setDeletingProposalId(proposal.id);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      await deleteMangakaProposal(proposal.id, session.user.email);
      setProposals((current) =>
        current.filter((item) => item.id !== proposal.id),
      );
      if (selectedBoardProposalId === proposal.id)
        setSelectedBoardProposalId(null);
      if (editingProposalId === proposal.id) setEditingProposalId(null);
      if (selectedProductionProposalId === proposal.id)
        setSelectedProductionProposalId(null);
      notifySuccess(
        "Proposal deleted. It has been removed from Mangaka, Tantou Editor, and Board queues.",
      );
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setDeletingProposalId(null);
    }
  }

  async function handleCreateChapter(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!activeProductionProposal) return;
    if (!chapterForm.title.trim()) {
      setErrorMessage("Chapter title is required.");
      return;
    }

    setProductionAction("chapter");
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const created = await createMangakaChapter(
        activeProductionProposal.id,
        session.user.email,
        {
          title: chapterForm.title.trim(),
          chapterNumber: parseOptionalNumber(chapterForm.chapterNumber),
          summary: chapterForm.summary.trim() || undefined,
        },
      );
      setProductionByProposal((current) => ({
        ...current,
        [activeProductionProposal.id]: upsertChapter(
          current[activeProductionProposal.id] ?? [],
          created,
        ),
      }));
      setSelectedChapterId(created.id);
      setChapterForm(EMPTY_CHAPTER_FORM);
      notifySuccess("Chapter created successfully.");
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setProductionAction(null);
    }
  }

  async function handleCreatePage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!activeProductionProposal || !selectedChapter) return;
    if (!pageForm.pageNumber.trim() || !pageForm.fileName.trim()) {
      setErrorMessage("Page number and page file name are required.");
      return;
    }

    setProductionAction("page");
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const created = await createMangakaPage(
        activeProductionProposal.id,
        selectedChapter.id,
        session.user.email,
        {
          pageNumber: Number(pageForm.pageNumber),
          fileName: pageForm.fileName.trim(),
          notes: pageForm.notes.trim() || undefined,
        },
      );
      setProductionByProposal((current) => ({
        ...current,
        [activeProductionProposal.id]: appendPage(
          current[activeProductionProposal.id] ?? [],
          selectedChapter.id,
          created,
        ),
      }));
      setSelectedPageId(created.id);
      setPageForm(EMPTY_PAGE_FORM);
      notifySuccess("Page metadata added successfully.");
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setProductionAction(null);
    }
  }

  async function handleCreateTask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const seriesId = activeProductionProposal?.seriesId || activeProductionProposal?.id;
    if (!seriesId || !selectedChapter || !selectedPage) return;
    if (!taskForm.assistantEmail.trim() || !taskForm.instructions.trim()) {
      setErrorMessage("Assistant email and instructions are required.");
      return;
    }

    setProductionAction("task");
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const created = await createMangakaTask(
        seriesId,
        selectedChapter.id,
        selectedPage.id,
        session.user.email,
        {
          assistantEmail: taskForm.assistantEmail.trim(),
          instructions: taskForm.instructions.trim(),
          deadline: taskForm.deadline.trim() || undefined,
        },
      );
      setProductionByProposal((current) => ({
        ...current,
        [seriesId]: appendTask(
          current[seriesId] ?? [],
          selectedChapter.id,
          selectedPage.id,
          created,
        ),
      }));
      setTaskForm(EMPTY_TASK_FORM);
      setActiveView("page");
      notifySuccess("Assistant task created successfully.");
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setProductionAction(null);
    }
  }

  async function handleCreateRegion(rect: { x: number; y: number; widthPct: number; heightPct: number }) {
    const seriesId = activeProductionProposal?.seriesId || activeProductionProposal?.id;
    if (!seriesId || !selectedChapter || !selectedPage) return;
    setErrorMessage(null);
    try {
      const created = await createMangakaRegion(
        seriesId,
        selectedChapter.id,
        selectedPage.id,
        session.user.email,
        {
          regionType: regionForm.regionType || "panel",
          x: rect.x,
          y: rect.y,
          widthPct: rect.widthPct,
          heightPct: rect.heightPct,
          note: regionForm.note || undefined,
        },
      );
      setRegions((prev) => [...prev, created]);
      setRegionForm({ regionType: "panel", note: "" });
      notifySuccess("Region created.");
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    }
  }

  async function handleDeleteRegion(regionId: string) {
    const seriesId = activeProductionProposal?.seriesId || activeProductionProposal?.id;
    if (!seriesId || !selectedChapter || !selectedPage) return;
    try {
      await deleteMangakaRegion(
        seriesId,
        selectedChapter.id,
        selectedPage.id,
        regionId,
        session.user.email,
      );
      setRegions((prev) => prev.filter((r) => r.id !== regionId));
      notifySuccess("Region deleted.");
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    }
  }

  function handleRegionClick(region: RegionRect) {
    setSelectedRegionId(region.id);
    setTaskForm((prev) => ({ ...prev, assistantEmail: "assistant@manga.local" }));
  }

  function beginEdit(proposal: MangaProposal) {
    setSelectedBoardProposalId(null);
    setEditingProposalId(proposal.id);
    setPendingManuscriptFile(null);
    setActiveView("proposalForm");
    setErrorMessage(null);
  }

  function openNewProposal() {
    setEditingProposalId(null);
    setPendingManuscriptFile(null);
    setProposalForm(EMPTY_PROPOSAL_FORM);
    setActiveView("proposalForm");
    setErrorMessage(null);
  }

  function openSubmitWorkspace(proposal: MangaProposal | null) {
    setSelectedBoardProposalId(null);
    setEditingProposalId(proposal?.id ?? null);
    setActiveView("submit");
    setErrorMessage(null);
  }

  function openBoardProposal(proposal: MangaProposal) {
    setSelectedBoardProposalId(proposal.id);
    setActiveView(canEditProposal(proposal.status) ? "proposals" : "review");
    setErrorMessage(null);
    setSuccessMessage(null);
  }

  function openView(view: ActiveView) {
    if (
      view === "proposals" &&
      selectedBoardProposal &&
      !canEditProposal(selectedBoardProposal.status)
    ) {
      setSelectedBoardProposalId(null);
    }

    if (
      view === "review" &&
      selectedBoardProposal &&
      canEditProposal(selectedBoardProposal.status)
    ) {
      setSelectedBoardProposalId(null);
    }

    setActiveView(view);
    setErrorMessage(null);
    setSuccessMessage(null);
  }

  function openProductionWorkspace(
    view: "chapter" | "page" | "task" | "inspect",
  ) {
    setActiveView(view);
    setErrorMessage(null);
    setSuccessMessage(null);
  }

  function notifySuccess(message: string) {
    setSuccessMessage(message);
    setErrorMessage(null);
  }

  function resetProposalForm() {
    setEditingProposalId(null);
    setPendingManuscriptFile(null);
    setProposalForm(EMPTY_PROPOSAL_FORM);
    setActiveView("proposals");
  }

  function handleSelectProductionProposal(proposalId: string) {
    setSelectedProductionProposalId(proposalId || null);
    setSelectedChapterId(null);
    setSelectedPageId(null);
    setActiveView(
      proposalId
        ? activeView === "inspect"
          ? "inspect"
          : "production"
        : "overall",
    );
  }

  function handleLogout() {
    logout();
    onLogout?.();
  }

  return (
    <main className="board-page board-page--mangaka" id="mangaka-dashboard">
      <div className="mangaka-shell mangaka-shell--compact">
        <aside className="board-rail mangaka-rail" aria-label="Mangaka sidebar">
          <div className="board-rail__brand">
            <div className="board-rail__badge">M</div>
            <div>
              <span className="eyebrow">Mangaka</span>
              <strong>Studio</strong>
            </div>
          </div>

          <nav
            className="board-rail__nav mangaka-rail__actions"
            aria-label="Mangaka workspace navigation"
          >
            <div className="mangaka-rail-group">
              <span className="mangaka-rail-group__title">Workspace</span>
              <button
                type="button"
                className={`mangaka-rail-action ${activeView === "overall" ? "is-active" : ""}`}
                onClick={() => openView("overall")}
              >
                <span>Home</span>
                <strong>Overall</strong>
              </button>
            </div>

            <div className="mangaka-rail-group">
              <span className="mangaka-rail-group__title">Proposal</span>
              <button
                type="button"
                className={`mangaka-rail-action ${activeView === "proposals" ? "is-active" : ""}`}
                onClick={() => openView("proposals")}
              >
                <span>Create / edit</span>
                <strong>Drafts</strong>
              </button>
              <button
                type="button"
                className={`mangaka-rail-action ${activeView === "submit" ? "is-active" : ""}`}
                onClick={() =>
                  openSubmitWorkspace(
                    proposals.find((proposal) =>
                      canSubmitProposal(proposal.status),
                    ) ?? null,
                  )
                }
              >
                <span>Editor queue</span>
                <strong>Submit</strong>
              </button>
              <button
                type="button"
                className={`mangaka-rail-action ${activeView === "review" ? "is-active" : ""}`}
                onClick={() => openView("review")}
              >
                <span>Status / notes</span>
                <strong>Review Status</strong>
              </button>
            </div>

            <div className="mangaka-rail-group">
              <span className="mangaka-rail-group__title">Production</span>
              <button
                type="button"
                className={`mangaka-rail-action ${activeView === "production" ? "is-active" : ""}`}
                onClick={() => openView("production")}
              >
                <span>Overview</span>
                <strong>Production</strong>
              </button>
              <button
                type="button"
                className={`mangaka-rail-action ${activeView === "chapter" ? "is-active" : ""}`}
                onClick={() => openProductionWorkspace("chapter")}
                disabled={eligibleProductionProposals.length === 0}
              >
                <span>Create</span>
                <strong>Chapter</strong>
              </button>
              <button
                type="button"
                className={`mangaka-rail-action ${activeView === "page" ? "is-active" : ""}`}
                onClick={() => openProductionWorkspace("page")}
                disabled={eligibleProductionProposals.length === 0}
              >
                <span>Create</span>
                <strong>Page</strong>
              </button>
              <button
                type="button"
                className={`mangaka-rail-action ${activeView === "task" ? "is-active" : ""}`}
                onClick={() => openProductionWorkspace("task")}
                disabled={eligibleProductionProposals.length === 0}
              >
                <span>Create</span>
                <strong>Task</strong>
              </button>
              <button
                type="button"
                className={`mangaka-rail-action ${activeView === "inspect" ? "is-active" : ""}`}
                onClick={() => openProductionWorkspace("inspect")}
                disabled={eligibleProductionProposals.length === 0}
              >
                <span>Browse</span>
                <strong>View Structure</strong>
              </button>
            </div>
          </nav>

          <div className="board-rail__meta mangaka-rail__profile">
            <div>
              <span>Signed in as</span>
              <strong>{session.user.fullName}</strong>
            </div>
            <div>
              <span>Role</span>
              <strong>{session.user.role}</strong>
            </div>
          </div>
        </aside>

        <section className="mangaka-main">
          <header className="mangaka-topbar glass-card">
            <div className="mangaka-topbar__title">
              <span className="eyebrow">Mangaka workspace</span>
              <h1>{activeViewLabel[activeView]}</h1>
              <p>
                {activeView === "overall"
                  ? "Manage manuscript proposals, review decisions, and production handoff from one workspace."
                  : activeView === "proposals"
                    ? "Only drafts and revision requests live here."
                    : activeView === "review"
                      ? "Track submitted, rejected, and approved proposal decisions here."
                      : activeView === "proposalForm"
                        ? "Work on one proposal form only."
                        : activeView === "submit"
                          ? "Send only eligible drafts to Tantou Editor."
                          : activeView === "production"
                            ? "Pick a series and open the production tools."
                            : "Work on one production module at a time."}
              </p>
            </div>
            <div className="mangaka-topbar__tools">
              <label className="mangaka-search">
                <span className="sr-only">Search proposals</span>
                <input
                  value={proposalQuery}
                  onChange={(event) => setProposalQuery(event.target.value)}
                  placeholder="Search title, genre, or status"
                />
              </label>
              <button
                type="button"
                className="button button-secondary dashboard-logout"
                onClick={handleLogout}
              >
                Logout
              </button>
            </div>
          </header>

          {errorMessage && (
            <div className="board-alert board-alert--error mangaka-alert">
              {errorMessage}
            </div>
          )}
          {successMessage && (
            <div className="board-alert board-alert--success mangaka-alert">
              {successMessage}
            </div>
          )}

          {activeView === "overall" && (
            <section className="mangaka-view-shell">
              <div className="mangaka-overview-grid">
                <article className="panel-card mangaka-overview-card">
                  <span className="eyebrow">Workspace summary</span>
                  <h3>Proposal portfolio</h3>
                  <p>
                    Monitor active drafts, approvals, and submissions before
                    moving into production.
                  </p>
                  <div className="mangaka-stats mangaka-stats--compact">
                    <div>
                      <span>Editable drafts</span>
                      <strong>{draftWorkCount}</strong>
                    </div>
                    <div>
                      <span>Production ready</span>
                      <strong>{productionReadyCount}</strong>
                    </div>
                    <div>
                      <span>In editor review</span>
                      <strong>{totalSubmitted}</strong>
                    </div>
                  </div>
                </article>

                <article className="panel-card mangaka-overview-card">
                  <span className="eyebrow">Workspace modules</span>
                  <h3>Manage the manga workflow</h3>
                  <div className="mangaka-action-grid">
                    <button
                      type="button"
                      className="module-card"
                      onClick={() => openView("proposals")}
                    >
                      <strong>Drafts</strong>
                      <span>Create or revise manuscript proposals.</span>
                    </button>
                    <button
                      type="button"
                      className="module-card"
                      onClick={() =>
                        openSubmitWorkspace(
                          proposals.find((proposal) =>
                            canSubmitProposal(proposal.status),
                          ) ?? null,
                        )
                      }
                    >
                      <strong>Submit</strong>
                      <span>Route completed drafts to editorial review.</span>
                    </button>
                    <button
                      type="button"
                      className="module-card"
                      onClick={() => openView("review")}
                    >
                      <strong>Review Status</strong>
                      <span>Review editor and board outcomes.</span>
                    </button>
                    <button
                      type="button"
                      className="module-card"
                      onClick={() => openView("production")}
                    >
                      <strong>Production</strong>
                      <span>Prepare approved titles for chapter work.</span>
                    </button>
                    <button
                      type="button"
                      className="module-card"
                      onClick={() => openProductionWorkspace("inspect")}
                      disabled={eligibleProductionProposals.length === 0}
                    >
                      <strong>View Structure</strong>
                      <span>Inspect the production hierarchy.</span>
                    </button>
                  </div>
                </article>
              </div>

              <article className="panel-card mangaka-demo-flow-card">
                <div className="panel-card__header panel-card__header--stacked">
                  <div>
                    <span className="eyebrow">Workflow sequence</span>
                    <h3>End-to-end production path</h3>
                    <p>
                      Follow the sequence from proposal drafting to production
                      structure review.
                    </p>
                  </div>
                </div>
                <div className="mangaka-demo-steps">
                  <button type="button" onClick={() => openView("proposals")}>
                    <span>1</span>
                    <strong>Drafts</strong>
                    <small>Prepare manuscript proposal metadata.</small>
                  </button>
                  <button
                    type="button"
                    onClick={() =>
                      openSubmitWorkspace(
                        proposals.find((proposal) =>
                          canSubmitProposal(proposal.status),
                        ) ?? null,
                      )
                    }
                  >
                    <span>2</span>
                    <strong>Submit</strong>
                    <small>Send eligible drafts for editorial review.</small>
                  </button>
                  <button type="button" onClick={() => openView("review")}>
                    <span>3</span>
                    <strong>Review Status</strong>
                    <small>Monitor editor and board decisions.</small>
                  </button>
                  <button type="button" onClick={() => openView("production")}>
                    <span>4</span>
                    <strong>Production</strong>
                    <small>Build approved titles into chapters.</small>
                  </button>
                  <button
                    type="button"
                    onClick={() => openProductionWorkspace("inspect")}
                    disabled={eligibleProductionProposals.length === 0}
                  >
                    <span>5</span>
                    <strong>View Structure</strong>
                    <small>
                      Review chapter, page, and task hierarchy.
                    </small>
                  </button>
                </div>
              </article>

              <div className="mangaka-summary-grid">
                <article className="panel-card">
                  <div className="panel-card__header panel-card__header--stacked">
                    <div>
                      <span className="eyebrow">Recent</span>
                      <h3>Latest activity</h3>
                    </div>
                  </div>
                  <div className="mangaka-recent-list">
                    {recentProposals.length === 0 ? (
                      <div className="admin-empty-state admin-empty-state--compact">
                        <strong>No proposals yet</strong>
                        <p>Create your first draft to get started.</p>
                      </div>
                    ) : (
                      recentProposals.map((proposal) => (
                        <button
                          key={proposal.id}
                          type="button"
                          className="summary-row"
                          onClick={() => openBoardProposal(proposal)}
                        >
                          <div>
                            <strong>{proposal.title}</strong>
                            <span>{proposal.genre || "Untitled genre"}</span>
                          </div>
                          <span
                            className={`status-badge status-badge--${statusClass(proposal.status)}`}
                          >
                            {formatStatus(proposal.status)}
                          </span>
                        </button>
                      ))
                    )}
                  </div>
                </article>

                <article className="panel-card">
                  <div className="panel-card__header panel-card__header--stacked">
                    <div>
                      <span className="eyebrow">Operational queue</span>
                      <h3>Priority actions</h3>
                    </div>
                  </div>
                  <div className="mangaka-next-steps">
                    <button
                      type="button"
                      className="summary-row summary-row--compact"
                      onClick={() => openView("proposals")}
                    >
                      <span>Work on drafts</span>
                      <strong>{draftWorkCount}</strong>
                    </button>
                    <button
                      type="button"
                      className="summary-row summary-row--compact"
                      onClick={() =>
                        openSubmitWorkspace(
                          proposals.find((proposal) =>
                            canSubmitProposal(proposal.status),
                          ) ?? null,
                        )
                      }
                    >
                      <span>Ready to submit</span>
                      <strong>{visibleDraftCount}</strong>
                    </button>
                    <button
                      type="button"
                      className="summary-row summary-row--compact"
                      onClick={() => openView("review")}
                    >
                      <span>Review results</span>
                      <strong>
                        {totalSubmitted + totalRejected + productionReadyCount}
                      </strong>
                    </button>
                    <button
                      type="button"
                      className="summary-row summary-row--compact"
                      onClick={() => openView("production")}
                    >
                      <span>Production-ready</span>
                      <strong>{productionReadyCount}</strong>
                    </button>
                  </div>
                </article>
              </div>
            </section>
          )}

          {activeView === "proposals" && (
            <section className="mangaka-view-shell">
              <div className="panel-card__header mangaka-view-header">
                <div>
                  <span className="eyebrow">Draft workspace</span>
                  <h3>Drafts and revision requests</h3>
                  <p>
                    Only proposals you can create, edit, or resubmit are shown
                    here.
                  </p>
                </div>
                <button
                  type="button"
                  className="primary-button"
                  onClick={openNewProposal}
                >
                  New proposal
                </button>
              </div>

              <div className="mangaka-proposal-board mangaka-proposal-board--drafts panel-card">
                {DRAFT_LANES.map((lane) => {
                  const laneProposals = visibleProposals
                    .filter((proposal) =>
                      lane.statuses.includes(proposal.status),
                    )
                    .sort((left, right) =>
                      compareDates(
                        right.updatedAt ?? right.submittedAt,
                        left.updatedAt ?? left.submittedAt,
                      ),
                    );

                  return (
                    <section className="mangaka-status-column" key={lane.label}>
                      <div className="mangaka-status-column__header">
                        <strong>{lane.label}</strong>
                        <span>{laneProposals.length}</span>
                      </div>
                      <div className="mangaka-status-column__content">
                        {laneProposals.length === 0 ? (
                          <div className="admin-empty-state admin-empty-state--compact">
                            <strong>No proposals</strong>
                            <p>{lane.emptyHint}</p>
                          </div>
                        ) : (
                          laneProposals.map((proposal) => {
                            const isEditable = canEditProposal(proposal.status);
                            const isSubmittable = canSubmitProposal(
                              proposal.status,
                            );
                            const isReadyForProduction = isProductionReady(
                              proposal.status,
                            );

                            return (
                              <article
                                className={`mangaka-card mangaka-card--clickable ${selectedBoardProposalId === proposal.id ? "is-selected" : ""}`}
                                key={proposal.id}
                                role="button"
                                tabIndex={0}
                                onClick={() =>
                                  setSelectedBoardProposalId(proposal.id)
                                }
                                onKeyDown={(event) => {
                                  if (
                                    event.key === "Enter" ||
                                    event.key === " "
                                  ) {
                                    event.preventDefault();
                                    setSelectedBoardProposalId(proposal.id);
                                  }
                                }}
                              >
                                <div className="mangaka-card__top">
                                  <span
                                    className={`status-badge status-badge--${statusClass(proposal.status)}`}
                                  >
                                    {formatStatus(proposal.status)}
                                  </span>
                                  <strong>{proposal.title}</strong>
                                  <p>{proposal.genre || "No genre yet"}</p>
                                </div>
                                <div className="mangaka-card__body">
                                  <p>{proposal.synopsis}</p>
                                </div>
                                {reviewSummary(proposal) && (
                                  <div
                                    className={`mangaka-review-note mangaka-review-note--${statusClass(proposal.status)}`}
                                  >
                                    <span>
                                      {reviewSummary(proposal)?.label}
                                    </span>
                                    <strong>
                                      {reviewSummary(proposal)?.note}
                                    </strong>
                                    <small>
                                      {reviewSummary(proposal)?.reviewer} ·{" "}
                                      {reviewSummary(proposal)?.date}
                                    </small>
                                  </div>
                                )}
                                <div className="mangaka-card__meta">
                                  <span>
                                    {proposal.manuscriptFileName || "No file"}
                                  </span>
                                  <span>{formatDate(proposal.updatedAt)}</span>
                                </div>
                                <div className="mangaka-card__actions">
                                  <button
                                    type="button"
                                    onClick={(event) => {
                                      event.stopPropagation();
                                      setSelectedBoardProposalId(proposal.id);
                                    }}
                                  >
                                    View details
                                  </button>
                                  {isEditable && (
                                    <button
                                      type="button"
                                      onClick={(event) => {
                                        event.stopPropagation();
                                        beginEdit(proposal);
                                      }}
                                    >
                                      Edit
                                    </button>
                                  )}
                                  {isSubmittable && (
                                    <button
                                      type="button"
                                      onClick={(event) => {
                                        event.stopPropagation();
                                        openSubmitWorkspace(proposal);
                                      }}
                                    >
                                      Submit
                                    </button>
                                  )}
                                  {isReadyForProduction && (
                                    <button
                                      type="button"
                                      onClick={(event) => {
                                        event.stopPropagation();
                                        setSelectedProductionProposalId(
                                          proposal.id,
                                        );
                                        openView("production");
                                      }}
                                    >
                                      Production
                                    </button>
                                  )}
                                  <button
                                    type="button"
                                    className="button-danger-text"
                                    onClick={(event) => {
                                      event.stopPropagation();
                                      void handleDeleteProposal(proposal);
                                    }}
                                    disabled={
                                      deletingProposalId === proposal.id
                                    }
                                  >
                                    {deletingProposalId === proposal.id
                                      ? "Deleting..."
                                      : "Delete"}
                                  </button>
                                </div>
                              </article>
                            );
                          })
                        )}
                      </div>
                    </section>
                  );
                })}
              </div>

              <div className="admin-empty-state admin-empty-state--compact mangaka-board-helper">
                <strong>Details open in a popup</strong>
                <p>
                  Use View details on any card to inspect it without losing your
                  place.
                </p>
              </div>
            </section>
          )}

          {activeView === "review" && (
            <section className="mangaka-view-shell">
              <div className="panel-card__header mangaka-view-header">
                <div>
                  <span className="eyebrow">Review status</span>
                  <h3>Submitted, rejected, and approved decisions</h3>
                  <p>
                    This page is only for tracking where a submitted proposal is
                    and why it was accepted or rejected.
                  </p>
                </div>
                <button
                  type="button"
                  className="primary-button"
                  onClick={() => openView("proposals")}
                >
                  Back to drafts
                </button>
              </div>

              <div className="mangaka-proposal-board panel-card">
                {REVIEW_LANES.map((lane) => {
                  const laneProposals = visibleProposals
                    .filter((proposal) =>
                      lane.statuses.includes(proposal.status),
                    )
                    .sort((left, right) =>
                      compareDates(
                        right.updatedAt ?? right.submittedAt,
                        left.updatedAt ?? left.submittedAt,
                      ),
                    );

                  return (
                    <section className="mangaka-status-column" key={lane.label}>
                      <div className="mangaka-status-column__header">
                        <strong>{lane.label}</strong>
                        <span>{laneProposals.length}</span>
                      </div>
                      <div className="mangaka-status-column__content">
                        {laneProposals.length === 0 ? (
                          <div className="admin-empty-state admin-empty-state--compact">
                            <strong>No proposals</strong>
                            <p>{lane.emptyHint}</p>
                          </div>
                        ) : (
                          laneProposals.map((proposal) => {
                            const isReadyForProduction = isProductionReady(
                              proposal.status,
                            );

                            return (
                              <article
                                className={`mangaka-card mangaka-card--clickable ${selectedBoardProposalId === proposal.id ? "is-selected" : ""}`}
                                key={proposal.id}
                                role="button"
                                tabIndex={0}
                                onClick={() =>
                                  setSelectedBoardProposalId(proposal.id)
                                }
                                onKeyDown={(event) => {
                                  if (
                                    event.key === "Enter" ||
                                    event.key === " "
                                  ) {
                                    event.preventDefault();
                                    setSelectedBoardProposalId(proposal.id);
                                  }
                                }}
                              >
                                <div className="mangaka-card__top">
                                  <span
                                    className={`status-badge status-badge--${statusClass(proposal.status)}`}
                                  >
                                    {formatStatus(proposal.status)}
                                  </span>
                                  <strong>{proposal.title}</strong>
                                  <p>{proposal.genre || "No genre yet"}</p>
                                </div>
                                <div className="mangaka-card__body">
                                  <p>{proposal.synopsis}</p>
                                </div>
                                {reviewSummary(proposal) && (
                                  <div
                                    className={`mangaka-review-note mangaka-review-note--${statusClass(proposal.status)}`}
                                  >
                                    <span>
                                      {reviewSummary(proposal)?.label}
                                    </span>
                                    <strong>
                                      {reviewSummary(proposal)?.note}
                                    </strong>
                                    <small>
                                      {reviewSummary(proposal)?.reviewer} ·{" "}
                                      {reviewSummary(proposal)?.date}
                                    </small>
                                  </div>
                                )}
                                <div className="mangaka-card__meta">
                                  <span>
                                    {proposal.manuscriptFileName || "No file"}
                                  </span>
                                  <span>{formatDate(proposal.updatedAt)}</span>
                                </div>
                                <div className="mangaka-card__actions">
                                  <button
                                    type="button"
                                    onClick={(event) => {
                                      event.stopPropagation();
                                      setSelectedBoardProposalId(proposal.id);
                                    }}
                                  >
                                    View details
                                  </button>
                                  {isReadyForProduction && (
                                    <button
                                      type="button"
                                      onClick={(event) => {
                                        event.stopPropagation();
                                        setSelectedProductionProposalId(
                                          proposal.id,
                                        );
                                        openView("production");
                                      }}
                                    >
                                      Production
                                    </button>
                                  )}
                                  <button
                                    type="button"
                                    className="button-danger-text"
                                    onClick={(event) => {
                                      event.stopPropagation();
                                      void handleDeleteProposal(proposal);
                                    }}
                                    disabled={
                                      deletingProposalId === proposal.id
                                    }
                                  >
                                    {deletingProposalId === proposal.id
                                      ? "Deleting..."
                                      : "Delete"}
                                  </button>
                                </div>
                              </article>
                            );
                          })
                        )}
                      </div>
                    </section>
                  );
                })}
              </div>

              <div className="admin-empty-state admin-empty-state--compact mangaka-board-helper">
                <strong>Details open in a popup</strong>
                <p>
                  Use View details on any review card to inspect it without
                  scrolling away from the board.
                </p>
              </div>
            </section>
          )}

          {activeView === "proposalForm" && (
            <section className="mangaka-view-shell">
              <div className="panel-card mangaka-focus-panel">
                <div className="panel-card__header panel-card__header--stacked">
                  <div>
                    <span className="eyebrow">Draft editor</span>
                    <h3>
                      {selectedProposal?.status === "NeedsRevision"
                        ? "Revise manuscript"
                        : selectedProposal
                          ? "Edit proposal"
                          : "New series proposal"}
                    </h3>
                    <p>
                      {selectedProposal?.status === "NeedsRevision"
                        ? "Apply Tantou Editor feedback, choose the revised file, then update or resubmit."
                        : selectedProposal
                          ? "Update proposal metadata before resubmission."
                          : "Prepare manuscript metadata for editorial screening."}
                    </p>
                  </div>
                </div>

                <form
                  className="mangaka-form panel-card panel-card--inner"
                  onSubmit={handleSaveProposal}
                  aria-busy={isSaving}
                >
                  {selectedProposal?.status === "NeedsRevision" &&
                    reviewSummary(selectedProposal) && (
                      <div className="board-alert board-alert--warning">
                        <strong>
                          {reviewSummary(selectedProposal)?.label}
                        </strong>
                        <span>{reviewSummary(selectedProposal)?.note}</span>
                      </div>
                    )}
                  <div className="form-grid form-grid--two">
                    <label className="admin-field">
                      <span>Title</span>
                      <input
                        placeholder="Series title"
                        value={proposalForm.title}
                        onChange={(event) =>
                          setProposalForm({
                            ...proposalForm,
                            title: event.target.value,
                          })
                        }
                      />
                    </label>
                    <label className="admin-field">
                      <span>Genre</span>
                      <input
                        placeholder="Primary genre"
                        value={proposalForm.genre}
                        onChange={(event) =>
                          setProposalForm({
                            ...proposalForm,
                            genre: event.target.value,
                          })
                        }
                      />
                    </label>
                  </div>
                  <label className="admin-field">
                    <span>Synopsis</span>
                    <textarea
                      rows={4}
                      placeholder="Summarize the premise, conflict, and editorial hook."
                      value={proposalForm.synopsis}
                      onChange={(event) =>
                        setProposalForm({
                          ...proposalForm,
                          synopsis: event.target.value,
                        })
                      }
                    />
                  </label>
                  <div className="form-grid form-grid--two">
                    <label className="admin-field">
                      <span>
                        {selectedProposal?.status === "NeedsRevision"
                          ? "Revised file upload"
                          : "File upload"}
                      </span>
                      <input
                        type="file"
                        onChange={(event) =>
                          handleManuscriptUpload(
                            event.target.files?.[0] ?? null,
                          )
                        }
                        disabled={isSaving || isUploadingManuscript}
                      />
                    </label>
                    <label className="admin-field">
                      <span>Version</span>
                      <input
                        placeholder="v1"
                        value={proposalForm.manuscriptVersion}
                        onChange={(event) =>
                          setProposalForm({
                            ...proposalForm,
                            manuscriptVersion: event.target.value,
                          })
                        }
                      />
                    </label>
                  </div>
                  <div className="board-alert board-alert--hint">
                    {isUploadingManuscript
                      ? "Uploading manuscript..."
                      : pendingManuscriptFile
                        ? `Ready to upload on save: ${pendingManuscriptFile.name}`
                        : proposalForm.manuscriptFileName
                          ? `Current manuscript file: ${proposalForm.manuscriptFileName}`
                          : "Choose a manuscript file first."}
                  </div>
                  <div className="admin-form__actions">
                    <button
                      className="primary-button"
                      type="submit"
                      disabled={isSaving || isUploadingManuscript}
                    >
                      {isSaving
                        ? "Saving..."
                        : selectedProposal?.status === "NeedsRevision"
                          ? "Update revision"
                          : selectedProposal
                            ? "Update draft"
                            : "Save draft"}
                    </button>
                    <button
                      className="button button-secondary"
                      type="submit"
                      data-submit-after-save="true"
                      disabled={isSaving || isUploadingManuscript}
                    >
                      {isSaving
                        ? "Submitting..."
                        : selectedProposal?.status === "NeedsRevision"
                          ? "Update & Resubmit to Tantou"
                          : "Save & Submit to Tantou"}
                    </button>
                    <button
                      className="button button-secondary"
                      type="button"
                      onClick={resetProposalForm}
                    >
                      Back to drafts
                    </button>
                  </div>
                </form>
              </div>
            </section>
          )}

          {activeView === "submit" && (
            <section className="mangaka-view-shell">
              <div className="panel-card">
                <div className="panel-card__header panel-card__header--stacked">
                  <div>
                    <span className="eyebrow">Submit</span>
                    <h3>Eligible drafts</h3>
                    <p>Only drafts and revisions can be submitted.</p>
                  </div>
                </div>

                <div className="mangaka-submit-list">
                  {proposals.filter((proposal) =>
                    canSubmitProposal(proposal.status),
                  ).length === 0 ? (
                    <div className="admin-empty-state">
                      <strong>No draft ready</strong>
                      <p>Create or revise one first.</p>
                    </div>
                  ) : (
                    proposals
                      .filter((proposal) => canSubmitProposal(proposal.status))
                      .map((proposal) => (
                        <article
                          className="mangaka-card mangaka-card--compact"
                          key={proposal.id}
                        >
                          <div className="mangaka-card__header">
                            <div>
                              <strong>{proposal.title}</strong>
                              <p>{formatStatus(proposal.status)}</p>
                            </div>
                            <span
                              className={`status-badge status-badge--${statusClass(proposal.status)}`}
                            >
                              {formatStatus(proposal.status)}
                            </span>
                          </div>
                          <p className="production-copy">{proposal.synopsis}</p>
                          <div className="mangaka-card__actions">
                            <button
                              type="button"
                              onClick={() => handleSubmitProposal(proposal)}
                              disabled={isSubmitting === proposal.id}
                            >
                              {isSubmitting === proposal.id
                                ? "Submitting..."
                                : "Submit"}
                            </button>
                            <button
                              type="button"
                              className="button button-secondary"
                              onClick={() => beginEdit(proposal)}
                            >
                              Edit
                            </button>
                            <button
                              type="button"
                              className="button-danger-text"
                              onClick={() =>
                                void handleDeleteProposal(proposal)
                              }
                              disabled={deletingProposalId === proposal.id}
                            >
                              {deletingProposalId === proposal.id
                                ? "Deleting..."
                                : "Delete"}
                            </button>
                          </div>
                        </article>
                      ))
                  )}
                </div>
              </div>
            </section>
          )}

          {activeView === "production" && (
            <section className="mangaka-view-shell">
              <div className="panel-card mangaka-production-hub">
                <div className="panel-card__header panel-card__header--stacked">
                  <div>
                    <span className="eyebrow">Production hub</span>
                    <h3>
                      {activeProductionProposal
                        ? activeProductionProposal.title
                        : "Select a series"}
                    </h3>
                    <p>
                      Choose a ready proposal, then open a production module.
                    </p>
                  </div>
                  <label className="admin-field mangaka-production-selector">
                    <span>Series</span>
                    <select
                      value={selectedProductionProposalId ?? ""}
                      onChange={(event) =>
                        handleSelectProductionProposal(event.target.value)
                      }
                    >
                      <option value="">Choose approved proposal</option>
                      {eligibleProductionProposals.map((proposal) => (
                        <option key={proposal.id} value={proposal.id}>
                          {proposal.title}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>

                <div className="production-summary production-summary--compact">
                  <div>
                    <span>Chapters</span>
                    <strong>{productionChapters.length}</strong>
                  </div>
                  <div>
                    <span>Pages</span>
                    <strong>{countPages(productionChapters)}</strong>
                  </div>
                  <div>
                    <span>Tasks</span>
                    <strong>{countTasks(productionChapters)}</strong>
                  </div>
                </div>

                {!selectedProductionProposalId ? (
                  <div className="admin-empty-state">
                    <strong>No series selected</strong>
                    <p>Pick an approved proposal first.</p>
                  </div>
                ) : (
                  <div className="mangaka-action-grid mangaka-action-grid--production">
                    <button
                      type="button"
                      className="module-card"
                      onClick={() => openProductionWorkspace("chapter")}
                    >
                      <strong>Chapter</strong>
                      <span>Create or review chapter metadata.</span>
                    </button>
                    <button
                      type="button"
                      className="module-card"
                      onClick={() => openProductionWorkspace("page")}
                      disabled={productionChapters.length === 0}
                    >
                      <strong>Page</strong>
                      <span>Add page metadata inside a chapter.</span>
                    </button>
                    <button
                      type="button"
                      className="module-card"
                      onClick={() => openProductionWorkspace("task")}
                      disabled={productionChapters.length === 0}
                    >
                      <strong>Task</strong>
                      <span>Assign assistant work.</span>
                    </button>
                    <button
                      type="button"
                      className="module-card module-card--wide"
                      onClick={() => openProductionWorkspace("inspect")}
                    >
                      <strong>View Structure</strong>
                      <span>Open the full production tree.</span>
                    </button>
                  </div>
                )}
              </div>
            </section>
          )}

          {(activeView === "chapter" ||
            activeView === "page" ||
            activeView === "task") && (
            <section className="mangaka-view-shell mangaka-view-shell--split">
              <div className="panel-card mangaka-module-panel">
                <div className="panel-card__header panel-card__header--stacked">
                  <div>
                    <span className="eyebrow">Production</span>
                    <h3>{activeViewLabel[activeView]}</h3>
                    <p>
                      {activeProductionProposal
                        ? activeProductionProposal.title
                        : "Choose an approved series."}
                    </p>
                  </div>
                  <label className="admin-field mangaka-production-selector">
                    <span>Series</span>
                    <select
                      value={selectedProductionProposalId ?? ""}
                      onChange={(event) =>
                        handleSelectProductionProposal(event.target.value)
                      }
                    >
                      <option value="">Choose approved proposal</option>
                      {eligibleProductionProposals.map((proposal) => (
                        <option key={proposal.id} value={proposal.id}>
                          {proposal.title}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>

                {activeView === "chapter" && (
                  <form
                    className="mangaka-form panel-card panel-card--inner"
                    onSubmit={handleCreateChapter}
                  >
                    <label className="admin-field">
                      <span>Proposal</span>
                      <input
                        value={activeProductionProposal?.title ?? ""}
                        readOnly
                        placeholder="Select an approved proposal"
                      />
                    </label>
                    <div className="form-grid form-grid--two">
                      <label className="admin-field">
                        <span>Title</span>
                        <input
                          placeholder="Chapter 1"
                          value={chapterForm.title}
                          onChange={(event) =>
                            setChapterForm({
                              ...chapterForm,
                              title: event.target.value,
                            })
                          }
                        />
                      </label>
                      <label className="admin-field">
                        <span>No.</span>
                        <input
                          placeholder="1"
                          inputMode="numeric"
                          value={chapterForm.chapterNumber}
                          onChange={(event) =>
                            setChapterForm({
                              ...chapterForm,
                              chapterNumber: event.target.value,
                            })
                          }
                        />
                      </label>
                    </div>
                    <label className="admin-field">
                      <span>Summary</span>
                      <textarea
                        rows={3}
                        placeholder="Optional summary."
                        value={chapterForm.summary}
                        onChange={(event) =>
                          setChapterForm({
                            ...chapterForm,
                            summary: event.target.value,
                          })
                        }
                      />
                    </label>
                    <div className="admin-form__actions">
                      <button
                        className="primary-button"
                        type="submit"
                        disabled={
                          productionAction === "chapter" ||
                          !selectedProductionProposalId
                        }
                      >
                        {productionAction === "chapter"
                          ? "Creating..."
                          : "Create"}
                      </button>
                    </div>
                  </form>
                )}

                {activeView === "page" && (
                  <div className="mangaka-form panel-card panel-card--inner">
                    <div className="form-grid form-grid--two">
                      <div>
                        <h4>Add Page</h4>
                        <form onSubmit={handleCreatePage}>
                          <label className="admin-field">
                            <span>Chapter</span>
                            <select
                              value={selectedChapterId ?? ""}
                              onChange={(event) =>
                                setSelectedChapterId(event.target.value || null)
                              }
                            >
                              <option value="">Choose a chapter</option>
                              {productionChapters.map((chapter) => (
                                <option key={chapter.id} value={chapter.id}>
                                  {chapter.chapterNumber
                                    ? `Chapter ${chapter.chapterNumber}`
                                    : chapter.title}
                                </option>
                              ))}
                            </select>
                          </label>
                          <div className="form-grid form-grid--two">
                            <label className="admin-field">
                              <span>Page</span>
                              <input
                                placeholder="1"
                                inputMode="numeric"
                                value={pageForm.pageNumber}
                                onChange={(event) =>
                                  setPageForm({
                                    ...pageForm,
                                    pageNumber: event.target.value,
                                  })
                                }
                              />
                            </label>
                            <label className="admin-field">
                              <span>File</span>
                              <input
                                placeholder="chapter-01-page-01.png"
                                value={pageForm.fileName}
                                onChange={(event) =>
                                  setPageForm({
                                    ...pageForm,
                                    fileName: event.target.value,
                                  })
                                }
                              />
                            </label>
                          </div>
                          <label className="admin-field">
                            <span>Notes</span>
                            <input
                              placeholder="Metadata only"
                              value={pageForm.notes}
                              onChange={(event) =>
                                setPageForm({
                                  ...pageForm,
                                  notes: event.target.value,
                                })
                              }
                            />
                          </label>
                          <div className="admin-form__actions">
                            <button
                              className="primary-button"
                              type="submit"
                              disabled={
                                productionAction === "page" || !selectedChapterId
                              }
                            >
                              {productionAction === "page" ? "Creating..." : "Add"}
                            </button>
                          </div>
                        </form>
                      </div>

                      <div>
                        <h4>Region Drawing</h4>
                        {selectedPage && (
                          <div>
                            <VisualCanvas
                              imageUrl={`/api/pages/${selectedPage.id}/image`}
                              regions={regions.map(r => ({
                                id: r.id,
                                x: r.x,
                                y: r.y,
                                widthPct: r.widthPct,
                                heightPct: r.heightPct,
                                regionType: r.regionType,
                                note: r.note,
                              }))}
                              onRegionCreate={handleCreateRegion}
                              onRegionClick={handleRegionClick}
                              drawingMode={drawingMode}
                              onToggleDrawing={() => setDrawingMode(!drawingMode)}
                            />
                            {/* Region type form */}
                            {drawingMode && (
                              <div style={{ marginTop: 12, display: "flex", gap: 8, flexWrap: "wrap" }}>
                                <select
                                  value={regionForm.regionType}
                                  onChange={(e) => setRegionForm({ ...regionForm, regionType: e.target.value })}
                                  style={{ padding: "6px 12px", borderRadius: 6, border: "1px solid #d1d5db" }}
                                >
                                  <option value="panel">Panel</option>
                                  <option value="speech_bubble">Speech Bubble</option>
                                  <option value="character">Character</option>
                                  <option value="background">Background</option>
                                  <option value="effect">Effect</option>
                                </select>
                                <input
                                  placeholder="Region note..."
                                  value={regionForm.note}
                                  onChange={(e) => setRegionForm({ ...regionForm, note: e.target.value })}
                                  style={{ padding: "6px 12px", borderRadius: 6, border: "1px solid #d1d5db", flex: 1 }}
                                />
                              </div>
                            )}
                            {/* Region list */}
                            {regions.length > 0 && (
                              <div style={{ marginTop: 12 }}>
                                <h5 style={{ margin: "0 0 8px", fontSize: 14, color: "#374151" }}>Regions</h5>
                                {regions.map((r) => (
                                  <div
                                    key={r.id}
                                    style={{
                                      display: "flex",
                                      alignItems: "center",
                                      justifyContent: "space-between",
                                      padding: "6px 10px",
                                      background: r.id === selectedRegionId ? "#eff6ff" : "#f9fafb",
                                      borderRadius: 6,
                                      marginBottom: 4,
                                      fontSize: 13,
                                      cursor: "pointer",
                                      border: r.id === selectedRegionId ? "1px solid #3b82f6" : "1px solid transparent",
                                    }}
                                    onClick={() => setSelectedRegionId(r.id)}
                                  >
                                    <span>{r.regionType}: {r.note || "No note"} ({Math.round(r.x)},{Math.round(r.y)})</span>
                                    <button
                                      onClick={(e) => { e.stopPropagation(); handleDeleteRegion(r.id); }}
                                      style={{
                                        background: "none",
                                        border: "none",
                                        color: "#ef4444",
                                        cursor: "pointer",
                                        fontSize: 13,
                                      }}
                                    >
                                      Delete
                                    </button>
                                  </div>
                                ))}
                              </div>
                            )}
                          </div>
                        )}
                        {!selectedPage && (
                          <p style={{ color: "#6b7280", fontSize: 14 }}>Select a page from the chapter tree to draw regions.</p>
                        )}
                      </div>
                    </div>
                  </div>
                )}

                {activeView === "task" && selectedPage && (
                  <div className="mangaka-form panel-card panel-card--inner">
                    <div className="form-grid form-grid--two" style={{gap: '24px'}}>
                      <div>
                        <h4>Page Preview</h4>
                        <div
                          style={{
                            border: '1px solid #ddd',
                            borderRadius: '12px',
                            overflow: 'hidden',
                            background: '#fafafa',
                            minHeight: '300px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                          }}
                        >
                          <img
                            src={`/api/pages/${selectedPage.id}/image`}
                            alt={`Page ${selectedPage.pageNumber}`}
                            style={{
                              maxWidth: '100%',
                              maxHeight: '400px',
                              objectFit: 'contain',
                            }}
                            onError={(e) => {
                              (e.currentTarget as HTMLImageElement).style.display = 'none';
                            }}
                          />
                        </div>
                        <p className="production-copy" style={{marginTop: '8px'}}>
                          Page {selectedPage.pageNumber} · {selectedPage.fileName}
                        </p>
                      </div>
                      <form onSubmit={handleCreateTask}>
                        <h4>Assign Task</h4>
                        {selectedRegionId && (
                          <p style={{ fontSize: 13, color: "#3b82f6", marginBottom: 8 }}>
                            Assigning task to selected region
                          </p>
                        )}
                        <label className="admin-field">
                          <span>Assistant email</span>
                          <input
                            placeholder="assistant@manga.local"
                            value={taskForm.assistantEmail}
                            onChange={(event) =>
                              setTaskForm({
                                ...taskForm,
                                assistantEmail: event.target.value,
                              })
                            }
                          />
                        </label>
                        <label className="admin-field">
                          <span>Deadline</span>
                          <input
                            type="date"
                            value={taskForm.deadline}
                            onChange={(event) =>
                              setTaskForm({
                                ...taskForm,
                                deadline: event.target.value,
                              })
                            }
                          />
                        </label>
                        <label className="admin-field">
                          <span>Instructions</span>
                          <textarea
                            rows={3}
                            placeholder="Describe the task..."
                            value={taskForm.instructions}
                            onChange={(event) =>
                              setTaskForm({
                                ...taskForm,
                                instructions: event.target.value,
                              })
                            }
                          />
                        </label>
                        <div className="admin-form__actions">
                          <button
                            className="primary-button"
                            type="submit"
                            disabled={productionAction === "task"}
                          >
                            {productionAction === "task" ? "Creating..." : "Giao Task"}
                          </button>
                        </div>
                      </form>
                    </div>
                  </div>
                )}
              </div>
            </section>
          )}

          {activeView === "inspect" && (
            <section className="mangaka-view-shell">
              <div className="panel-card production-board production-board--inspect">
                <div className="panel-card__header panel-card__header--stacked">
                  <div>
                    <span className="eyebrow">View Structure</span>
                    <h3>Full production tree</h3>
                    <p>
                      {activeProductionProposal
                        ? activeProductionProposal.title
                        : "Select an approved series."}
                    </p>
                  </div>
                  <label className="admin-field mangaka-production-selector">
                    <span>Series</span>
                    <select
                      value={selectedProductionProposalId ?? ""}
                      onChange={(event) =>
                        handleSelectProductionProposal(event.target.value)
                      }
                    >
                      <option value="">Choose approved proposal</option>
                      {eligibleProductionProposals.map((proposal) => (
                        <option key={proposal.id} value={proposal.id}>
                          {proposal.title}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>

                {isProductionLoading ? (
                  <div className="admin-empty-state admin-empty-state--loading">
                    Loading production…
                  </div>
                ) : !selectedProductionProposalId ? (
                  <div className="admin-empty-state">
                    <strong>No series selected</strong>
                    <p>Pick one with production access.</p>
                  </div>
                ) : productionChapters.length === 0 ? (
                  <div className="admin-empty-state">
                    <strong>No chapters yet</strong>
                    <p>Add the first chapter.</p>
                  </div>
                ) : (
                  <div className="production-chapter-list">
                    {productionChapters.map((chapter) => (
                      <article
                        className={`production-chapter-card ${selectedChapterId === chapter.id ? "is-selected" : ""}`}
                        key={chapter.id}
                      >
                        <div className="production-chapter-card__header">
                          <div>
                            <span className="eyebrow">
                              Ch {chapter.chapterNumber ?? "—"}
                            </span>
                            <h4>{chapter.title}</h4>
                          </div>
                          <button
                            type="button"
                            className="button button-secondary production-mini-button"
                            onClick={() => setSelectedChapterId(chapter.id)}
                          >
                            Select
                          </button>
                        </div>

                        {chapter.summary && (
                          <p className="production-copy">{chapter.summary}</p>
                        )}

                        {chapter.pages?.length ? (
                          <div className="production-page-list">
                            {chapter.pages.map((page) => (
                              <article
                                className={`production-page-card ${selectedPageId === page.id ? "is-selected" : ""}`}
                                key={page.id}
                              >
                                <div className="production-page-card__header">
                                  <div>
                                    <strong>Page {page.pageNumber}</strong>
                                    <p>{page.fileName}</p>
                                  </div>
                                  <button
                                    type="button"
                                    className="button button-secondary production-mini-button"
                                    onClick={() => setSelectedPageId(page.id)}
                                  >
                                    Select
                                  </button>
                                </div>
                                <div className="production-meta-row">
                                  <span>
                                    Uploaded {formatDate(page.uploadedAt)}
                                  </span>
                                  {page.notes && <span>{page.notes}</span>}
                                </div>

                                {page.tasks?.length ? (
                                  <div className="production-task-list">
                                    {page.tasks.map((task) => (
                                      <article
                                        className="production-task-card"
                                        key={task.id}
                                      >
                                        <strong>Task</strong>
                                        <p>{task.instructions}</p>
                                        <div className="production-meta-row">
                                          <span>
                                            {task.assistantEmail}
                                          </span>
                                          <span>
                                            {task.status ?? "Pending"}
                                          </span>
                                          <span>
                                            {formatDate(task.createdAt)}
                                          </span>
                                          {task.deadline && (
                                            <span className="task-deadline">
                                              Due: {formatDate(task.deadline)}
                                            </span>
                                          )}
                                        </div>
                                        {(task.submittedFileName ||
                                          task.submissionNote ||
                                          task.submittedAt) && (
                                          <div className="production-submission-note">
                                            <span className="eyebrow">
                                              Assistant submission
                                            </span>
                                            <strong>
                                              {task.submittedFileName ??
                                                "Submitted work"}
                                            </strong>
                                            {task.submissionNote && (
                                              <p>{task.submissionNote}</p>
                                            )}
                                            <small>
                                              {formatDate(task.submittedAt)}
                                            </small>
                                          </div>
                                        )}
                                        {normalizeTaskStatus(
                                          task.status,
                                        ) === "Submitted" && (
                                          <div className="production-task-actions">
                                            <button
                                              type="button"
                                              className="button button-secondary production-mini-button"
                                              onClick={() =>
                                                void handleReviewTask(
                                                  "approve",
                                                  task,
                                                )
                                              }
                                              disabled={
                                                productionAction ===
                                                `approve-${task.id}`
                                              }
                                            >
                                              {productionAction ===
                                              `approve-${task.id}`
                                                ? "Approving…"
                                                : "Approve"}
                                            </button>
                                            <button
                                              type="button"
                                              className="button button-secondary production-mini-button"
                                              onClick={() =>
                                                void handleReviewTask(
                                                  "redo",
                                                  task,
                                                )
                                              }
                                              disabled={
                                                productionAction ===
                                                `redo-${task.id}`
                                              }
                                            >
                                              {productionAction ===
                                              `redo-${task.id}`
                                                ? "Sending…"
                                                : "Request redo"}
                                            </button>
                                          </div>
                                        )}
                                      </article>
                                    ))}
                                  </div>
                                ) : (
                                  <div className="admin-empty-state admin-empty-state--compact">
                                    <strong>No tasks</strong>
                                    <p>Create a task in the task view.</p>
                                  </div>
                                )}
                              </article>
                            ))}
                          </div>
                        ) : (
                          <div className="admin-empty-state admin-empty-state--compact">
                            <strong>No pages</strong>
                            <p>Add the first page.</p>
                          </div>
                        )}
                      </article>
                    ))}
                  </div>
                )}
              </div>
            </section>
          )}
        </section>
      </div>

      {selectedBoardProposal && (
        <div
          className="mangaka-detail-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="mangaka-detail-title"
          onClick={() => setSelectedBoardProposalId(null)}
        >
          <article
            className={`mangaka-detail-dialog mangaka-proposal-detail--${statusClass(selectedBoardProposal.status)}`}
            onClick={(event) => event.stopPropagation()}
          >
            <div className="mangaka-detail-dialog__header">
              <div>
                <span className="eyebrow">Proposal detail</span>
                <h3 id="mangaka-detail-title">{selectedBoardProposal.title}</h3>
                <p>
                  Status, manuscript file, reviewer notes, and available next
                  steps.
                </p>
              </div>
              <button
                type="button"
                className="button button-secondary"
                onClick={() => setSelectedBoardProposalId(null)}
              >
                Close
              </button>
            </div>

            <div className="mangaka-detail-dialog__status">
              <span
                className={`status-badge status-badge--${statusClass(selectedBoardProposal.status)}`}
              >
                {formatStatus(selectedBoardProposal.status)}
              </span>
            </div>

            <div className="mangaka-detail-grid mangaka-detail-grid--modal">
              <div>
                <span>Genre</span>
                <strong>{selectedBoardProposal.genre || "—"}</strong>
              </div>
              <div>
                <span>Manuscript file</span>
                <strong>
                  {selectedBoardProposal.manuscriptFileName || "—"}
                </strong>
              </div>
              <div>
                <span>Submitted</span>
                <strong>{formatDate(selectedBoardProposal.submittedAt)}</strong>
              </div>
              <div>
                <span>Updated</span>
                <strong>{formatDate(selectedBoardProposal.updatedAt)}</strong>
              </div>
            </div>

            <div className="mangaka-detail-copy">
              <span>Synopsis</span>
              <p>{selectedBoardProposal.synopsis}</p>
            </div>

            {reviewSummary(selectedBoardProposal) ? (
              <div
                className={`mangaka-review-note mangaka-review-note--large mangaka-review-note--${statusClass(selectedBoardProposal.status)}`}
              >
                <span>{reviewSummary(selectedBoardProposal)?.label}</span>
                <strong>{reviewSummary(selectedBoardProposal)?.note}</strong>
                <small>
                  {reviewSummary(selectedBoardProposal)?.reviewer} ·{" "}
                  {reviewSummary(selectedBoardProposal)?.date}
                </small>
              </div>
            ) : (
              <div className="board-alert board-alert--hint">
                No reviewer note yet. This proposal is still waiting in the
                workflow.
              </div>
            )}

            <div className="admin-form__actions">
              {canEditProposal(selectedBoardProposal.status) && (
                <button
                  className="primary-button"
                  type="button"
                  onClick={() => beginEdit(selectedBoardProposal)}
                >
                  Edit proposal
                </button>
              )}
              {canSubmitProposal(selectedBoardProposal.status) && (
                <button
                  className="button button-secondary"
                  type="button"
                  onClick={() => openSubmitWorkspace(selectedBoardProposal)}
                >
                  Submit to editor
                </button>
              )}
              {isProductionReady(selectedBoardProposal.status) && (
                <button
                  className="primary-button"
                  type="button"
                  onClick={() => {
                    setSelectedBoardProposalId(null);
                    setSelectedProductionProposalId(selectedBoardProposal.id);
                    openView("production");
                  }}
                >
                  Open production
                </button>
              )}
              <button
                className="button-danger-text"
                type="button"
                onClick={() => void handleDeleteProposal(selectedBoardProposal)}
                disabled={deletingProposalId === selectedBoardProposal.id}
              >
                {deletingProposalId === selectedBoardProposal.id
                  ? "Deleting..."
                  : "Delete proposal"}
              </button>
            </div>
          </article>
        </div>
      )}
    </main>
  );
}

function validateProposalForm(
  form: ProposalFormState,
  pendingFile: File | null,
) {
  return (
    [form.title, form.genre, form.synopsis, form.manuscriptVersion].every(
      (value) => value.trim().length > 0,
    ) &&
    (form.manuscriptFileName.trim().length > 0 || pendingFile !== null)
  );
}

function parseOptionalNumber(value: string) {
  const normalized = value.trim();
  if (!normalized) return null;
  const parsed = Number(normalized);
  return Number.isFinite(parsed) ? parsed : null;
}

function reviewSummary(proposal: MangaProposal) {
  if (proposal.status === "Rejected" && proposal.boardDecisionNote) {
    return {
      label: "Board rejected",
      note: proposal.boardDecisionNote,
      reviewer: proposal.boardMemberEmail ?? "Editorial Board",
      date: formatDate(proposal.boardReviewedAt),
    };
  }

  if (proposal.status === "Rejected" && proposal.editorNote) {
    return {
      label: "Editor rejected",
      note: proposal.editorNote,
      reviewer: proposal.editorEmail ?? "Tantou Editor",
      date: formatDate(proposal.editorReviewedAt),
    };
  }

  if (proposal.status === "NeedsRevision" && proposal.editorNote) {
    return {
      label: "Revision requested",
      note: proposal.editorNote,
      reviewer: proposal.editorEmail ?? "Tantou Editor",
      date: formatDate(proposal.editorReviewedAt),
    };
  }

  if (proposal.status === "UnderBoardReview" && proposal.editorNote) {
    return {
      label: "Forwarded to board",
      note: proposal.editorNote,
      reviewer: proposal.editorEmail ?? "Tantou Editor",
      date: formatDate(proposal.editorReviewedAt),
    };
  }

  if (
    proposal.status === "Approved" &&
    proposal.boardDecisionNote
  ) {
    return {
      label: "Board approved",
      note: proposal.boardDecisionNote,
      reviewer: proposal.boardMemberEmail ?? "Editorial Board",
      date: formatDate(proposal.boardReviewedAt),
    };
  }

  return null;
}

function isProductionReady(status: MangaProposalStatus) {
  return status === "Approved";
}

function normalizeTaskStatus(status?: string | null) {
  const compact = (status ?? "Pending").toLowerCase().replace(/[\s_-]/g, "");
  if (compact.includes("redo")) return "RedoRequested";
  if (compact.includes("approved")) return "Approved";
  if (compact.includes("submitted")) return "Submitted";
  if (compact.includes("progress")) return "InProgress";
  return "Pending";
}

function canEditProposal(status: MangaProposalStatus) {
  return status === "Draft" || status === "NeedsRevision";
}

function canSubmitProposal(status: MangaProposalStatus) {
  return status === "Draft" || status === "NeedsRevision";
}

function formatStatus(status: MangaProposalStatus) {
  switch (status) {
    case "Draft":
      return "Draft";
    case "SubmittedToEditor":
      return "Submitted to Tantou Editor";
    case "UnderBoardReview":
      return "Under board review";
    case "NeedsRevision":
      return "Needs revision";
    case "Approved":
      return "Approved";
    case "Rejected":
      return "Rejected";
  }
}

function statusClass(status: MangaProposalStatus) {
  switch (status) {
    case "Draft":
      return "draft";
    case "SubmittedToEditor":
      return "submitted";
    case "UnderBoardReview":
      return "submitted";
    case "NeedsRevision":
      return "revision";
    case "Approved":
      return "approved";
    case "Rejected":
      return "rejected";
  }
}

function formatDate(value?: string | null) {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "—";
  return new Intl.DateTimeFormat("en", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

function compareDates(left?: string | null, right?: string | null) {
  return toTime(left) - toTime(right);
}

function toTime(value?: string | null) {
  if (!value) return 0;
  const time = new Date(value).getTime();
  return Number.isNaN(time) ? 0 : time;
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Không thể lưu dữ liệu.";
}

function upsertProposal(current: MangaProposal[], proposal: MangaProposal) {
  return [proposal, ...current.filter((item) => item.id !== proposal.id)];
}

function upsertChapter(
  current: MangaProductionChapter[],
  chapter: MangaProductionChapter,
) {
  const merged = mergeChapter(
    current.find((item) => item.id === chapter.id),
    chapter,
  );
  return [merged, ...current.filter((item) => item.id !== chapter.id)].sort(
    (left, right) =>
      (left.chapterNumber ?? Number.MAX_SAFE_INTEGER) -
      (right.chapterNumber ?? Number.MAX_SAFE_INTEGER),
  );
}

function mergeChapters(
  existing: MangaProductionChapter[],
  incoming: MangaProductionChapter[],
) {
  return incoming
    .map((chapter) =>
      mergeChapter(
        existing.find((item) => item.id === chapter.id),
        chapter,
      ),
    )
    .sort(
      (left, right) =>
        (left.chapterNumber ?? Number.MAX_SAFE_INTEGER) -
        (right.chapterNumber ?? Number.MAX_SAFE_INTEGER),
    );
}

function mergeChapter(
  existing: MangaProductionChapter | undefined,
  incoming: MangaProductionChapter,
): MangaProductionChapter {
  return {
    ...existing,
    ...incoming,
    pages: mergePages(existing?.pages ?? [], incoming.pages ?? []),
  };
}

function mergePages(
  existing: MangaProductionPage[],
  incoming: MangaProductionPage[],
) {
  const map = new Map(existing.map((page) => [page.id, page]));
  return incoming.length > 0
    ? incoming
        .map((page) => mergePage(map.get(page.id), page))
        .sort((left, right) => left.pageNumber - right.pageNumber)
    : existing.sort((left, right) => left.pageNumber - right.pageNumber);
}

function mergePage(
  existing: MangaProductionPage | undefined,
  incoming: MangaProductionPage,
): MangaProductionPage {
  return {
    ...existing,
    ...incoming,
  };
}

function mergeTasks(
  existing: MangaProductionTask[],
  incoming: MangaProductionTask[],
) {
  const map = new Map(existing.map((task) => [task.id, task]));
  return incoming.length > 0
    ? incoming.map((task) => ({ ...map.get(task.id), ...task }))
    : existing;
}

function appendPage(
  chapters: MangaProductionChapter[],
  chapterId: string,
  page: MangaProductionPage,
) {
  return chapters.map((chapter) => {
    if (chapter.id !== chapterId) return chapter;
    const pages = chapter.pages
      ? [...chapter.pages.filter((item) => item.id !== page.id), page]
      : [page];
    return {
      ...chapter,
      pages: pages.sort((left, right) => left.pageNumber - right.pageNumber),
    };
  });
}

function appendTask(
  chapters: MangaProductionChapter[],
  chapterId: string,
  pageId: string,
  task: MangaProductionTask,
) {
  return chapters.map((chapter) => {
    if (chapter.id !== chapterId || !chapter.pages) return chapter;
    return {
      ...chapter,
      pages: chapter.pages.map((page) => {
        if (page.id !== pageId) return page;
        const tasks = page.tasks
          ? [...page.tasks.filter((t) => t.id !== task.id), task]
          : [task];
        return { ...page, tasks };
      }),
    };
  });
}

function updateTaskInTree(
  chapters: MangaProductionChapter[],
  chapterId: string,
  pageId: string,
  task: MangaProductionTask,
) {
  return chapters.map((chapter) => {
    if (chapter.id !== chapterId || !chapter.pages) return chapter;
    return {
      ...chapter,
      pages: chapter.pages.map((page) => {
        if (page.id !== pageId || !page.tasks) return page;
        return {
          ...page,
          tasks: page.tasks.map((t) => (t.id === task.id ? task : t)),
        };
      }),
    };
  });
}

function countPages(chapters: MangaProductionChapter[]) {
  return chapters.reduce(
    (total, chapter) => total + (chapter.pages?.length ?? 0),
    0,
  );
}

function countTasks(chapters: MangaProductionChapter[]) {
  return chapters.reduce(
    (chapterTotal, chapter) =>
      chapterTotal +
      (chapter.pages?.reduce(
        (pageTotal, page) => pageTotal + (page.tasks?.length ?? 0),
        0,
      ) ?? 0),
    0,
  );
}

function findPage(
  chapter: MangaProductionChapter | null,
  pageId: string | null,
) {
  if (!chapter || !pageId) return null;
  return chapter.pages?.find((page) => page.id === pageId) ?? null;
}


