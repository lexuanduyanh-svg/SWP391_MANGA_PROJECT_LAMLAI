import { useCallback, useEffect, useState } from "react";
import type {
  SeriesDecision,
  SeriesDecisionRequest,
  SeriesDecisionType,
} from "../types/seriesDecision";
import {
  listDecisions,
  makeDecision,
} from "../services/seriesDecisionService";

interface SeriesDecisionsPanelProps {
  seriesId: string;
  seriesTitle: string;
  boardMemberEmail: string;
}

const DECISION_LABELS: Record<SeriesDecisionType, string> = {
  MAINTAIN: "Maintain",
  RESCHEDULE: "Reschedule",
  CANCEL: "Cancel",
  CHANGE_FORMAT: "Change Format",
};

const DECISION_COLORS: Record<SeriesDecisionType, string> = {
  MAINTAIN: "#3b82f6",
  RESCHEDULE: "#f59e0b",
  CANCEL: "#ef4444",
  CHANGE_FORMAT: "#a855f7",
};

const badgeStyle = (type: SeriesDecisionType): React.CSSProperties => ({
  display: "inline-block",
  padding: "3px 10px",
  borderRadius: "12px",
  fontSize: "11px",
  fontWeight: 600,
  color: "#fff",
  background: DECISION_COLORS[type],
});

export default function SeriesDecisionsPanel({
  seriesId,
  seriesTitle,
  boardMemberEmail,
}: SeriesDecisionsPanelProps) {
  const [decisions, setDecisions] = useState<SeriesDecision[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [decisionType, setDecisionType] = useState<SeriesDecisionType | "">("");
  const [reason, setReason] = useState("");
  const [newFrequency, setNewFrequency] = useState("");
  const [newFormat, setNewFormat] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const loadDecisions = useCallback(async () => {
    try {
      setLoading(true);
      const data = await listDecisions(seriesId);
      setDecisions(data);
      setError(null);
    } catch (e: unknown) {
      setError(
        e instanceof Error ? e.message : "Failed to load decisions"
      );
    } finally {
      setLoading(false);
    }
  }, [seriesId]);

  useEffect(() => {
    loadDecisions();
  }, [loadDecisions]);

  const handleSubmit = useCallback(async () => {
    if (!decisionType || !reason.trim()) return;
    setSubmitting(true);
    setError(null);
    try {
      const request: SeriesDecisionRequest = {
        boardMemberEmail,
        decisionType: decisionType as SeriesDecisionType,
        reason: reason.trim(),
        ...(decisionType === "RESCHEDULE" && newFrequency
          ? { newFrequency }
          : {}),
        ...(decisionType === "CHANGE_FORMAT" && newFormat
          ? { newFormat }
          : {}),
      };
      const result = await makeDecision(seriesId, request);
      if (result) {
        setDecisions((prev) => [result, ...prev]);
      }
      setDecisionType("");
      setReason("");
      setNewFrequency("");
      setNewFormat("");
    } catch (e: unknown) {
      setError(
        e instanceof Error ? e.message : "Failed to submit decision"
      );
    } finally {
      setSubmitting(false);
    }
  }, [
    decisionType,
    reason,
    newFrequency,
    newFormat,
    boardMemberEmail,
    seriesId,
  ]);

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

  return (
    <div
      className="series-decision-panel"
      style={{
        fontFamily: "system-ui, sans-serif",
        color: "#e0e0e0",
      }}
    >
      <div
        style={{
          fontSize: "16px",
          fontWeight: 700,
          marginBottom: "4px",
        }}
      >
        {seriesTitle}
      </div>
      <div
        style={{
          fontSize: "12px",
          color: "#64748b",
          marginBottom: "16px",
        }}
      >
        Series #{seriesId}
      </div>

      {/* New Decision Form */}
      <div
        className="decision-form"
        style={{
          background: "#1e293b",
          borderRadius: "8px",
          padding: "16px",
          marginBottom: "20px",
          border: "1px solid #334155",
        }}
      >
        <div
          style={{
            fontSize: "14px",
            fontWeight: 600,
            marginBottom: "12px",
          }}
        >
          Make a Decision
        </div>

        <div style={{ marginBottom: "12px" }}>
          <label
            style={{
              fontSize: "12px",
              color: "#94a3b8",
              display: "block",
              marginBottom: "4px",
            }}
          >
            Decision Type
          </label>
          <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
            {(Object.keys(DECISION_LABELS) as SeriesDecisionType[]).map(
              (type) => (
                <button
                  key={type}
                  onClick={() => setDecisionType(type)}
                  style={{
                    padding: "6px 14px",
                    borderRadius: "20px",
                    border: `1px solid ${
                      decisionType === type
                        ? DECISION_COLORS[type]
                        : "#334155"
                    }`,
                    background:
                      decisionType === type
                        ? `${DECISION_COLORS[type]}33`
                        : "transparent",
                    color:
                      decisionType === type
                        ? DECISION_COLORS[type]
                        : "#94a3b8",
                    cursor: "pointer",
                    fontSize: "12px",
                    fontWeight: decisionType === type ? 600 : 400,
                  }}
                >
                  {DECISION_LABELS[type]}
                </button>
              )
            )}
          </div>
        </div>

        {/* Conditional fields */}
        {decisionType === "RESCHEDULE" && (
          <div style={{ marginBottom: "12px" }}>
            <label
              style={{
                fontSize: "12px",
                color: "#94a3b8",
                display: "block",
                marginBottom: "4px",
              }}
            >
              New Frequency
            </label>
            <input
              type="text"
              value={newFrequency}
              onChange={(e) => setNewFrequency(e.target.value)}
              placeholder='e.g. "WEEKLY", "MONTHLY", "BIWEEKLY"'
              style={inputStyle}
            />
          </div>
        )}

        {decisionType === "CHANGE_FORMAT" && (
          <div style={{ marginBottom: "12px" }}>
            <label
              style={{
                fontSize: "12px",
                color: "#94a3b8",
                display: "block",
                marginBottom: "4px",
              }}
            >
              New Format
            </label>
            <input
              type="text"
              value={newFormat}
              onChange={(e) => setNewFormat(e.target.value)}
              placeholder='e.g. "DIGITAL", "PRINT", "SPECIAL"'
              style={inputStyle}
            />
          </div>
        )}

        <div style={{ marginBottom: "12px" }}>
          <label
            style={{
              fontSize: "12px",
              color: "#94a3b8",
              display: "block",
              marginBottom: "4px",
            }}
          >
            Reason
          </label>
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Explain the reason for this decision..."
            rows={3}
            style={{
              ...inputStyle,
              resize: "vertical",
            }}
          />
        </div>

        <button
          onClick={handleSubmit}
          disabled={
            submitting || !decisionType || !reason.trim()
          }
          style={{
            padding: "8px 20px",
            background:
              submitting || !decisionType || !reason.trim()
                ? "#475569"
                : "#3b82f6",
            color: "#fff",
            border: "none",
            borderRadius: "6px",
            cursor:
              submitting || !decisionType || !reason.trim()
                ? "not-allowed"
                : "pointer",
            fontSize: "14px",
          }}
        >
          {submitting ? "Submitting..." : "Submit Decision"}
        </button>
      </div>

      {/* Error message */}
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

      {/* Decision History */}
      <div
        style={{
          fontSize: "14px",
          fontWeight: 600,
          marginBottom: "12px",
        }}
      >
        Decision History
      </div>

      {loading ? (
        <div style={{ color: "#94a3b8", fontSize: "13px" }}>
          Loading decisions...
        </div>
      ) : decisions.length === 0 ? (
        <div
          style={{
            color: "#64748b",
            fontSize: "13px",
            padding: "16px 0",
            textAlign: "center",
          }}
        >
          No decisions recorded yet.
        </div>
      ) : (
        <div className="series-decision-history">
          {decisions.map((d) => (
            <div
              key={d.id}
              className="series-decision-item"
              style={{
                padding: "12px",
                background: "#1e293b",
                borderRadius: "6px",
                border: "1px solid #334155",
                marginBottom: "8px",
              }}
            >
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  marginBottom: "8px",
                }}
              >
                <span style={badgeStyle(d.decisionType)}>
                  {DECISION_LABELS[d.decisionType]}
                </span>
                <span style={{ fontSize: "11px", color: "#64748b" }}>
                  {d.decidedAt
                    ? new Date(d.decidedAt).toLocaleString()
                    : ""}
                </span>
              </div>

              {d.reason && (
                <div
                  style={{
                    fontSize: "13px",
                    color: "#cbd5e1",
                    marginBottom: "4px",
                    lineHeight: 1.4,
                  }}
                >
                  {d.reason}
                </div>
              )}

              <div
                style={{
                  fontSize: "11px",
                  color: "#94a3b8",
                  display: "flex",
                  gap: "16px",
                  flexWrap: "wrap",
                }}
              >
                <span>By: {d.boardMemberEmail}</span>
                {d.newFrequency && (
                  <span>Frequency: {d.newFrequency}</span>
                )}
                {d.newFormat && <span>Format: {d.newFormat}</span>}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
