import { useCallback, useEffect, useState } from "react";
import { logout } from "../services/authService";
import { listAllDecisions } from "../services/seriesDecisionService";
import type { SeriesDecision } from "../types/seriesDecision";
import SeriesDecisionsPanel from "./SeriesDecisionsPanel";

interface SeriesDecisionsDashboardProps {
  session: { email: string };
  onLogout?: () => void;
}

const DECISION_LABELS: Record<string, string> = {
  MAINTAIN: "Maintain",
  RESCHEDULE: "Reschedule",
  CANCEL: "Cancel",
  CHANGE_FORMAT: "Change Format",
};

const DECISION_COLORS: Record<string, string> = {
  MAINTAIN: "#3b82f6",
  RESCHEDULE: "#f59e0b",
  CANCEL: "#ef4444",
  CHANGE_FORMAT: "#a855f7",
};

export default function SeriesDecisionsDashboard({
  session,
  onLogout,
}: SeriesDecisionsDashboardProps) {
  const [allDecisions, setAllDecisions] = useState<SeriesDecision[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [selectedSeriesId, setSelectedSeriesId] = useState<string | null>(null);
  const [selectedSeriesTitle, setSelectedSeriesTitle] = useState("");

  const loadAllDecisions = useCallback(async () => {
    setLoading(true);
    try {
      const data = await listAllDecisions();
      setAllDecisions(data);
    } catch {
      // silently fail
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAllDecisions();
  }, [loadAllDecisions]);

  // Group decisions by series
  const grouped = new Map<string, { title: string; decisions: SeriesDecision[] }>();
  for (const d of allDecisions) {
    const key = d.seriesId;
    if (!grouped.has(key)) {
      grouped.set(key, { title: d.seriesTitle, decisions: [] });
    }
    grouped.get(key)!.decisions.push(d);
  }

  const filteredGroups = Array.from(grouped.entries()).filter(
    ([, g]) =>
      !search ||
      g.title.toLowerCase().includes(search.toLowerCase()) ||
      g.decisions.some(
        (d) =>
          d.boardMemberEmail.toLowerCase().includes(search.toLowerCase()) ||
          d.decisionType.toLowerCase().includes(search.toLowerCase())
      )
  );

  const handleLogout = () => {
    logout();
    onLogout?.();
  };

  // If a series is selected, show detailed panel
  if (selectedSeriesId) {
    return (
      <div
        className="decision-dashboard"
        style={{
          padding: "24px",
          maxWidth: "900px",
          margin: "0 auto",
          fontFamily: "system-ui, sans-serif",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: "16px",
          }}
        >
          <button
            onClick={() => setSelectedSeriesId(null)}
            style={{
              padding: "6px 14px",
              background: "transparent",
              color: "#94a3b8",
              border: "1px solid #334155",
              borderRadius: "4px",
              cursor: "pointer",
              fontSize: "13px",
            }}
          >
            &larr; Back to All Decisions
          </button>
          <span style={{ fontSize: "13px", color: "#94a3b8" }}>
            {session.email}
          </span>
        </div>
        <SeriesDecisionsPanel
          seriesId={selectedSeriesId}
          seriesTitle={selectedSeriesTitle}
          boardMemberEmail={session.email}
        />
      </div>
    );
  }

  return (
    <div
      className="decision-dashboard"
      style={{
        padding: "24px",
        maxWidth: "1100px",
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
          marginBottom: "20px",
        }}
      >
        <h2 style={{ margin: 0, fontSize: "20px", fontWeight: 700 }}>
          Series Decisions
        </h2>
        <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
          <span style={{ fontSize: "13px", color: "#94a3b8" }}>
            {session.email}
          </span>
          <button
            onClick={loadAllDecisions}
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
            Refresh
          </button>
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

      {/* Search */}
      <input
        type="text"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        placeholder="Search by series title, member, or decision type..."
        style={{
          width: "100%",
          padding: "8px 12px",
          background: "#0f172a",
          color: "#e0e0e0",
          border: "1px solid #334155",
          borderRadius: "6px",
          fontSize: "13px",
          marginBottom: "16px",
          boxSizing: "border-box",
        }}
      />

      {/* Loading */}
      {loading && (
        <div style={{ color: "#94a3b8", fontSize: "13px" }}>
          Loading decisions...
        </div>
      )}

      {/* No data */}
      {!loading && filteredGroups.length === 0 && (
        <div
          style={{
            color: "#64748b",
            fontSize: "13px",
            padding: "24px 0",
            textAlign: "center",
          }}
        >
          {allDecisions.length === 0
            ? "No decisions have been made yet."
            : "No results match your search."}
        </div>
      )}

      {/* Grouped decisions */}
      {filteredGroups.map(([seriesId, group]) => {
        const latest = group.decisions[0];
        return (
          <div
            key={seriesId}
            className="decision-group"
            style={{
              background: "#1e293b",
              borderRadius: "8px",
              border: "1px solid #334155",
              marginBottom: "10px",
              overflow: "hidden",
            }}
          >
            <div
              onClick={() => {
                setSelectedSeriesId(seriesId);
                setSelectedSeriesTitle(group.title);
              }}
              style={{
                padding: "14px 16px",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                cursor: "pointer",
                transition: "background 0.15s",
              }}
              onMouseEnter={(e) =>
                (e.currentTarget.style.background = "#33415544")
              }
              onMouseLeave={(e) =>
                (e.currentTarget.style.background = "transparent")
              }
            >
              <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                <div style={{ fontWeight: 600, fontSize: "14px" }}>
                  {group.title}
                </div>
                <div
                  style={{
                    fontSize: "11px",
                    color: "#64748b",
                  }}
                >
                  #{seriesId}
                </div>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                {latest && (
                  <>
                    <span
                      style={{
                        display: "inline-block",
                        padding: "3px 8px",
                        borderRadius: "10px",
                        fontSize: "11px",
                        fontWeight: 600,
                        color: "#fff",
                        background: DECISION_COLORS[latest.decisionType] ?? "#64748b",
                      }}
                    >
                      {DECISION_LABELS[latest.decisionType] ?? latest.decisionType}
                    </span>
                    <span style={{ fontSize: "11px", color: "#64748b" }}>
                      {group.decisions.length} decision
                      {group.decisions.length !== 1 ? "s" : ""}
                    </span>
                  </>
                )}
                <span style={{ color: "#64748b", fontSize: "14px" }}>
                  &rarr;
                </span>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
