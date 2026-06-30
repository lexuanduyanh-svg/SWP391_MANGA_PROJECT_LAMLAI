export type SeriesDecisionType =
  | "MAINTAIN"
  | "RESCHEDULE"
  | "CANCEL"
  | "CHANGE_FORMAT";

export interface SeriesDecision {
  id: string;
  seriesId: string;
  seriesTitle: string;
  boardMemberEmail: string;
  decisionType: SeriesDecisionType;
  reason: string | null;
  newFrequency: string | null;
  newFormat: string | null;
  decidedAt: string;
}

export interface SeriesDecisionRequest {
  boardMemberEmail: string;
  decisionType: SeriesDecisionType;
  reason: string;
  newFrequency?: string;
  newFormat?: string;
}
