import { useCallback, useEffect, useState } from "react";
import { logout } from "../services/authService";
import {
  createMetric,
  getAllRankings,
  listMetrics,
} from "../services/rankingService";
import type {
  ReaderMetric,
  ReaderMetricCreateRequest,
  SeriesRanking,
} from "../types/ranking";

interface RankingsDashboardProps {
  session: { email: string };
  onLogout?: () => void;
}

const SERIES_OPTIONS = [
  { id: "801", title: "Seed Approved" },
  { id: "802", title: "Test Series" },
];

const MEDAL_EMOJI: Record<number, string> = {
  1: "\uD83E\uDD47",
  2: "\uD83E\uDD48",
  3: "\uD83E\uDD49",
};

const tabStyle = (
  active: boolean
): React.CSSProperties => ({
  padding: "8px 20px",
  background: active ? "#3b82f6" : "transparent",
  color: active ? "#fff" : "#94a3b8",
  border: active ? "1px solid #3b82f6" : "1px solid #334155",
  borderRadius: "6px",
  cursor: "pointer",
  fontSize: "14px",
  fontWeight: active ? 600 : 400,
});

export default function RankingsDashboard({
  session,
  onLogout,
}: RankingsDashboardProps) {
  const [activeTab, setActiveTab] = useState<"metrics" | "rankings">("metrics");
  const [selectedSeries, setSelectedSeries] = useState("801");
  const [publicationCycle, setPublicationCycle] = useState("");
  const [salesFigures, setSalesFigures] = useState("");
  const [likesCount, setLikesCount] = useState("");
  const [sharesCount, setSharesCount] = useState("");
  const [votesCount, setVotesCount] = useState("");
  const [recentMetrics, setRecentMetrics] = useState<ReaderMetric[]>([]);
  const [rankings, setRankings] = useState<SeriesRanking[]>([]);
  const [submitMsg, setSubmitMsg] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loadingRankings, setLoadingRankings] = useState(false);

  const loadRecentMetrics = useCallback(async () => {
    try {
      const data = await listMetrics(selectedSeries);
      setRecentMetrics(data.slice(-5));
    } catch {
      // silently fail
    }
  }, [selectedSeries]);

  useEffect(() => {
    loadRecentMetrics();
  }, [loadRecentMetrics]);

  const handleSubmitMetric = useCallback(async () => {
    setSubmitMsg(null);
    setError(null);
    if (!publicationCycle) {
      setError("Publication cycle date is required");
      return;
    }
    try {
      const request: ReaderMetricCreateRequest = {
        publicationCycle,
        salesFigures: parseInt(salesFigures) || 0,
        likesCount: parseInt(likesCount) || 0,
        sharesCount: parseInt(sharesCount) || 0,
        votesCount: parseInt(votesCount) || 0,
      };
      await createMetric(selectedSeries, request);
      setSubmitMsg("Metric recorded successfully!");
      setPublicationCycle("");
      setSalesFigures("");
      setLikesCount("");
      setSharesCount("");
      setVotesCount("");
      loadRecentMetrics();
    } catch (e: unknown) {
      setError(
        e instanceof Error ? e.message : "Failed to record metric"
      );
    }
  }, [
    publicationCycle,
    salesFigures,
    likesCount,
    sharesCount,
    votesCount,
    selectedSeries,
    loadRecentMetrics,
  ]);

  const loadRankings = useCallback(async () => {
    setLoadingRankings(true);
    try {
      const data = await getAllRankings();
      setRankings(data);
    } catch (e: unknown) {
      setError(
        e instanceof Error ? e.message : "Failed to load rankings"
      );
    } finally {
      setLoadingRankings(false);
    }
  }, []);

  useEffect(() => {
    if (activeTab === "rankings") {
      loadRankings();
    }
  }, [activeTab, loadRankings]);

  const handleLogout = () => {
    logout();
    onLogout?.();
  };

  const inputStyle: React.CSSProperties = {
    width: "100%",
    padding: "8px 10px",
    background: "#0f172a",
    color: "#e0e0e0",
    border: "1px solid #334155",
    borderRadius: "4px",
    fontSize: "13px",
    boxSizing: "border-box",
  };

  const labelStyle: React.CSSProperties = {
    fontSize: "12px",
    color: "#94a3b8",
    marginBottom: "4px",
    display: "block",
  };

  return (
    <div
      className="rankings-dashboard"
      style={{
        padding: "24px",
        maxWidth: "1000px",
        margin: "0 auto",
        fontFamily: "system-ui, sans-serif",
        color: "#e0e0e0",
      }}
    >
      {/* Header */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "24px",
        }}
      >
        <h2 style={{ margin: 0, fontSize: "20px", fontWeight: 700 }}>
          Reader Metrics & Rankings
        </h2>
        <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
          <span style={{ fontSize: "13px", color: "#94a3b8" }}>
            {session.email}
          </span>
          <button
            onClick={handleLogout}
            style={{
              padding: "6px 14px",
              background: "transparent",
              color: "#ef4444",
              border: "1px solid #ef4444",
              borderRadius: "4px",
              cursor: "pointer",
              fontSize: "13px",
            }}
          >
            Logout
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div
        className="rankings-tabs"
        style={{ display: "flex", gap: "8px", marginBottom: "20px" }}
      >
        <button
          className={`rankings-tab ${activeTab === "metrics" ? "active" : ""}`}
          style={tabStyle(activeTab === "metrics")}
          onClick={() => setActiveTab("metrics")}
        >
          Metrics Input
        </button>
        <button
          className={`rankings-tab ${activeTab === "rankings" ? "active" : ""}`}
          style={tabStyle(activeTab === "rankings")}
          onClick={() => setActiveTab("rankings")}
        >
          Rankings
        </button>
      </div>

      {/* Error / Success */}
      {error && (
        <div
          style={{
            padding: "10px 14px",
            background: "#7f1d1d33",
            color: "#ef4444",
            borderRadius: "6px",
            marginBottom: "16px",
            fontSize: "13px",
          }}
        >
          {error}
        </div>
      )}
      {submitMsg && (
        <div
          style={{
            padding: "10px 14px",
            background: "#064e3b33",
            color: "#22c55e",
            borderRadius: "6px",
            marginBottom: "16px",
            fontSize: "13px",
          }}
        >
          {submitMsg}
        </div>
      )}

      {/* Tab: Metrics Input */}
      {activeTab === "metrics" && (
        <div>
          {/* Series selector */}
          <div style={{ marginBottom: "16px" }}>
            <label style={labelStyle}>Series</label>
            <select
              value={selectedSeries}
              onChange={(e) => setSelectedSeries(e.target.value)}
              style={inputStyle}
            >
              {SERIES_OPTIONS.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.id} - {s.title}
                </option>
              ))}
            </select>
          </div>

          {/* Metric form */}
          <div
            className="metrics-form"
            style={{
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: "12px",
              marginBottom: "16px",
            }}
          >
            <div>
              <label style={labelStyle}>Publication Cycle (date)</label>
              <input
                type="date"
                value={publicationCycle}
                onChange={(e) => setPublicationCycle(e.target.value)}
                style={inputStyle}
              />
            </div>
            <div>
              <label style={labelStyle}>Sales Figures</label>
              <input
                type="number"
                min="0"
                value={salesFigures}
                onChange={(e) => setSalesFigures(e.target.value)}
                style={inputStyle}
              />
            </div>
            <div>
              <label style={labelStyle}>Likes Count</label>
              <input
                type="number"
                min="0"
                value={likesCount}
                onChange={(e) => setLikesCount(e.target.value)}
                style={inputStyle}
              />
            </div>
            <div>
              <label style={labelStyle}>Shares Count</label>
              <input
                type="number"
                min="0"
                value={sharesCount}
                onChange={(e) => setSharesCount(e.target.value)}
                style={inputStyle}
              />
            </div>
            <div>
              <label style={labelStyle}>Votes Count</label>
              <input
                type="number"
                min="0"
                value={votesCount}
                onChange={(e) => setVotesCount(e.target.value)}
                style={inputStyle}
              />
            </div>
          </div>

          <button
            onClick={handleSubmitMetric}
            style={{
              padding: "8px 20px",
              background: "#3b82f6",
              color: "#fff",
              border: "none",
              borderRadius: "6px",
              cursor: "pointer",
              fontSize: "14px",
              marginBottom: "24px",
            }}
          >
            Record Metric
          </button>

          {/* Recent metrics table */}
          {recentMetrics.length > 0 && (
            <div>
              <div
                style={{
                  fontSize: "14px",
                  fontWeight: 600,
                  marginBottom: "8px",
                }}
              >
                Recent Metrics
              </div>
              <table
                className="metrics-table"
                style={{
                  width: "100%",
                  borderCollapse: "collapse",
                  fontSize: "13px",
                }}
              >
                <thead>
                  <tr style={{ borderBottom: "1px solid #334155" }}>
                    <th style={{ padding: "8px", textAlign: "left", color: "#94a3b8" }}>Date</th>
                    <th style={{ padding: "8px", textAlign: "right", color: "#94a3b8" }}>Sales</th>
                    <th style={{ padding: "8px", textAlign: "right", color: "#94a3b8" }}>Likes</th>
                    <th style={{ padding: "8px", textAlign: "right", color: "#94a3b8" }}>Shares</th>
                    <th style={{ padding: "8px", textAlign: "right", color: "#94a3b8" }}>Votes</th>
                  </tr>
                </thead>
                <tbody>
                  {recentMetrics.map((m) => (
                    <tr key={m.id} style={{ borderBottom: "1px solid #1e293b" }}>
                      <td style={{ padding: "6px 8px" }}>{m.publicationCycle}</td>
                      <td style={{ padding: "6px 8px", textAlign: "right" }}>{m.salesFigures}</td>
                      <td style={{ padding: "6px 8px", textAlign: "right" }}>{m.likesCount}</td>
                      <td style={{ padding: "6px 8px", textAlign: "right" }}>{m.sharesCount}</td>
                      <td style={{ padding: "6px 8px", textAlign: "right" }}>{m.votesCount}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Tab: Rankings */}
      {activeTab === "rankings" && (
        <div>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: "12px",
            }}
          >
            <div
              style={{ fontSize: "14px", fontWeight: 600 }}
            >
              Series Rankings
            </div>
            <button
              onClick={loadRankings}
              style={{
                padding: "6px 14px",
                background: "transparent",
                color: "#3b82f6",
                border: "1px solid #3b82f6",
                borderRadius: "4px",
                cursor: "pointer",
                fontSize: "13px",
              }}
            >
              {loadingRankings ? "Loading..." : "Refresh"}
            </button>
          </div>

          {loadingRankings ? (
            <div style={{ color: "#94a3b8", fontSize: "13px" }}>
              Loading rankings...
            </div>
          ) : rankings.length === 0 ? (
            <div
              style={{
                color: "#64748b",
                fontSize: "13px",
                padding: "24px 0",
                textAlign: "center",
              }}
            >
              No rankings data yet. Add metrics first.
            </div>
          ) : (
            <table
              className="rankings-table"
              style={{
                width: "100%",
                borderCollapse: "collapse",
                fontSize: "13px",
              }}
            >
              <thead>
                <tr style={{ borderBottom: "1px solid #334155" }}>
                  <th style={{ padding: "8px", textAlign: "left", color: "#94a3b8", width: "60px" }}>Rank</th>
                  <th style={{ padding: "8px", textAlign: "left", color: "#94a3b8" }}>Series</th>
                  <th style={{ padding: "8px", textAlign: "right", color: "#94a3b8" }}>Sales</th>
                  <th style={{ padding: "8px", textAlign: "right", color: "#94a3b8" }}>Likes</th>
                  <th style={{ padding: "8px", textAlign: "right", color: "#94a3b8" }}>Shares</th>
                  <th style={{ padding: "8px", textAlign: "right", color: "#94a3b8" }}>Votes</th>
                  <th style={{ padding: "8px", textAlign: "right", color: "#94a3b8" }}>Score</th>
                </tr>
              </thead>
              <tbody>
                {rankings.map((r) => {
                  const medal = MEDAL_EMOJI[r.rank];
                  const isTop3 = r.rank >= 1 && r.rank <= 3;
                  return (
                    <tr
                      key={r.seriesId}
                      className={`${isTop3 ? "rank-highlight" : ""}`}
                      style={{
                        borderBottom: "1px solid #1e293b",
                        background: isTop3
                          ? r.rank === 1
                            ? "#f59e0b11"
                            : r.rank === 2
                            ? "#94a3b811"
                            : "#cd7f3211"
                          : "transparent",
                      }}
                    >
                      <td
                        style={{
                          padding: "8px",
                          fontWeight: isTop3 ? 700 : 400,
                          fontSize: "16px",
                        }}
                      >
                        {medal ?? `#${r.rank}`}
                      </td>
                      <td style={{ padding: "8px", fontWeight: 500 }}>
                        {r.seriesTitle}
                      </td>
                      <td style={{ padding: "8px", textAlign: "right" }}>
                        {r.totalSales.toLocaleString()}
                      </td>
                      <td style={{ padding: "8px", textAlign: "right" }}>
                        {r.totalLikes.toLocaleString()}
                      </td>
                      <td style={{ padding: "8px", textAlign: "right" }}>
                        {r.totalShares.toLocaleString()}
                      </td>
                      <td style={{ padding: "8px", textAlign: "right" }}>
                        {r.totalVotes.toLocaleString()}
                      </td>
                      <td
                        style={{
                          padding: "8px",
                          textAlign: "right",
                          fontWeight: 700,
                          color: "#3b82f6",
                        }}
                      >
                        {r.compositeScore.toFixed(2)}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}
