export interface ReaderMetric {
  id: string;
  seriesId: string;
  publicationCycle: string;
  salesFigures: string;
  likesCount: string;
  sharesCount: string;
  votesCount: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReaderMetricCreateRequest {
  publicationCycle: string;
  salesFigures: number;
  likesCount: number;
  sharesCount: number;
  votesCount: number;
}

export interface SeriesRanking {
  seriesId: string;
  seriesTitle: string;
  totalSales: number;
  totalLikes: number;
  totalShares: number;
  totalVotes: number;
  compositeScore: number;
  rank: number;
}
