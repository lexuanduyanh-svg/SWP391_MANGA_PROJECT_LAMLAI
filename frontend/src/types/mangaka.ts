export type MangaProposalStatus =
  | "Draft"
  | "SubmittedToEditor"
  | "UnderBoardReview"
  | "NeedsRevision"
  | "Approved"
  | "Rejected";

export interface MangaProposal {
  id: string;
  title: string;
  genre: string;
  targetAudience: string;
  synopsis: string;
  authorEmail: string;
  status: MangaProposalStatus;
  manuscriptFileName?: string | null;
  manuscriptVersion?: string | null;
  manuscriptUploadedAt?: string | null;
  submittedAt?: string | null;
  updatedAt?: string | null;
  editorEmail?: string | null;
  editorNote?: string | null;
  editorReviewedAt?: string | null;
  boardMemberEmail?: string | null;
  boardDecisionNote?: string | null;
  boardReviewedAt?: string | null;
  boardApproveVotes?: number | null;
  boardRejectVotes?: number | null;
  boardPendingVotes?: number | null;
  boardTotalVotes?: number | null;
  currentMemberVote?: "APPROVE" | "REJECT" | null;
  /** Series ID created after board approval — used for production endpoints. */
  seriesId?: string | null;
}

export interface MangaProposalCreateRequest {
  authorEmail: string;
  title: string;
  genre: string;
  targetAudience: string;
  synopsis: string;
  manuscriptFileName: string;
  manuscriptVersion: string;
}

export interface MangaProposalUpdateRequest {
  title: string;
  genre: string;
  targetAudience: string;
  synopsis: string;
  manuscriptFileName: string;
  manuscriptVersion: string;
}

export interface MangaProposalSubmitRequest {
  authorEmail: string;
}

export interface MangaProductionChapter {
  id: string;
  proposalId: string;
  title: string;
  chapterNumber?: number | null;
  summary?: string | null;
  status?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  pages?: MangaProductionPage[] | null;
}

export interface MangaProductionRegion {
  id: string;
  pageId: string;
  regionType: string;
  x: number;
  y: number;
  widthPct: number;
  heightPct: number;
  note: string;
  tasks?: MangaProductionTask[];
}

export interface MangaProductionPage {
  id: string;
  chapterId: string;
  pageNumber: number;
  fileName: string;
  status?: string;
  notes?: string | null;
  uploadedAt?: string | null;
  /** Regions on this page (restored to full scope). */
  regions?: MangaProductionRegion[] | null;
  /** Tasks attached to this page in the production tree (frontend-only). */
  tasks?: MangaProductionTask[] | null;
}

export interface MangaProductionTask {
  id: string;
  pageId: string;
  assistantEmail: string;
  instructions: string;
  deadline?: string | null;
  pageFileName?: string | null;
  status?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  submittedFileName?: string | null;
  submissionNote?: string | null;
  submittedAt?: string | null;
  referenceFileName?: string | null;
  regionCoordinates?: string | null;
  payment?: number | null;
  regionId?: string | null;
}

export interface MangaProductionChapterCreateRequest {
  title: string;
  chapterNumber?: number | null;
  summary?: string;
}

export interface MangaProductionPageCreateRequest {
  pageNumber: number;
  fileName: string;
  notes?: string;
}

export interface MangaProductionTaskCreateRequest {
  assistantEmail: string;
  instructions: string;
  deadline?: string;
}

export type MangakaPageStatus =
  | "Uploaded"
  | "Segmented"
  | "TasksAssigned"
  | "TasksInProgress"
  | "AllTasksApproved"
  | "Finalized";
