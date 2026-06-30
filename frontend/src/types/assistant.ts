export type AssistantTaskStatus =
  | "Pending"
  | "RedoRequested"
  | "InProgress"
  | "Submitted"
  | "Approved"
  | (string & {});

export interface AssistantTask {
  id: string;
  assistantEmail: string;
  instructions: string;
  status?: AssistantTaskStatus | null;
  /** Series/chapter/page context */
  seriesId?: string | null;
  seriesTitle?: string | null;
  chapterId?: string | null;
  chapterTitle?: string | null;
  pageId?: string | null;
  pageNumber?: number | null;
  pageFileName?: string | null;
  deadline?: string | null;
  /** Submission */
  submittedFileName?: string | null;
  submissionNote?: string | null;
  submittedAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  referenceFileName?: string | null;
  regionId?: string | null;
  regionCoordinates?: string | null;
  payment?: number | null;
  proposalId?: string | null;
  proposalTitle?: string | null;
}

export interface AssistantTaskStartRequest {
  assistantEmail: string;
}

export interface AssistantTaskSubmitRequest {
  assistantEmail: string;
  submittedFileName: string;
  submissionNote: string;
}
