export type AssistantTaskStatus =
  | "Pending"
  | "RedoRequested"
  | "InProgress"
  | "Submitted"
  | "Approved"
  | (string & {});

export interface AssistantTaskProposalRef {
  id?: string | null;
  title?: string | null;
}

export interface AssistantTaskChapterRef {
  id?: string | null;
  title?: string | null;
  chapterNumber?: number | null;
}

export interface AssistantTaskPageRef {
  id?: string | null;
  pageNumber?: number | null;
  fileName?: string | null;
}

export interface AssistantTaskRegionRef {
  id?: string | null;
  regionType?: string | null;
  note?: string | null;
}

export interface AssistantTask {
  id: string;
  assistantEmail: string;
  taskType: string;
  instructions: string;
  referenceFileName: string;
  status?: AssistantTaskStatus | null;
  proposalId?: string | null;
  proposalTitle?: string | null;
  chapterId?: string | null;
  chapterTitle?: string | null;
  chapterNumber?: number | null;
  pageId?: string | null;
  pageNumber?: number | null;
  regionId?: string | null;
  regionType?: string | null;
  regionNote?: string | null;
  submittedFileName?: string | null;
  submissionNote?: string | null;
  startedAt?: string | null;
  submittedAt?: string | null;
  reviewedAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  proposal?: AssistantTaskProposalRef | null;
  chapter?: AssistantTaskChapterRef | null;
  page?: AssistantTaskPageRef | null;
  region?: AssistantTaskRegionRef | null;
}

export interface AssistantTaskStartRequest {
  assistantEmail: string;
}

export interface AssistantTaskSubmitRequest {
  assistantEmail: string;
  submittedFileName: string;
  submissionNote: string;
}
