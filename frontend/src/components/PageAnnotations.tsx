import { useCallback, useEffect, useRef, useState } from "react";
import type { Annotation, AnnotationCreateRequest } from "../types/annotation";
import {
  listAnnotations,
  createAnnotation,
  resolveAnnotation,
} from "../services/annotationService";

interface PageAnnotationsProps {
  pageId: string;
  pageImageUrl: string;
  editorEmail: string;
  readOnly?: boolean;
}

interface PinState {
  x: number;
  y: number;
  showForm: boolean;
  content: string;
}

const pinColors = {
  unresolved: "#ef4444",
  resolved: "#22c55e",
  new: "#3b82f6",
};

export default function PageAnnotations({
  pageId,
  pageImageUrl,
  editorEmail,
  readOnly = false,
}: PageAnnotationsProps) {
  const [annotations, setAnnotations] = useState<Annotation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newPin, setNewPin] = useState<PinState | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const imgRef = useRef<HTMLDivElement>(null);

  const loadAnnotations = useCallback(async () => {
    try {
      setLoading(true);
      const data = await listAnnotations(pageId);
      setAnnotations(data);
      setError(null);
    } catch (e: unknown) {
      setError(
        e instanceof Error ? e.message : "Failed to load annotations"
      );
    } finally {
      setLoading(false);
    }
  }, [pageId]);

  useEffect(() => {
    loadAnnotations();
  }, [loadAnnotations]);

  const handleImageClick = useCallback(
    (e: React.MouseEvent<HTMLDivElement>) => {
      if (readOnly) return;
      const rect = e.currentTarget.getBoundingClientRect();
      const x = ((e.clientX - rect.left) / rect.width) * 100;
      const y = ((e.clientY - rect.top) / rect.height) * 100;
      setNewPin({ x, y, showForm: true, content: "" });
    },
    [readOnly]
  );

  const handleSubmitAnnotation = useCallback(async () => {
    if (!newPin || !newPin.content.trim()) return;
    setSubmitting(true);
    try {
      const coordinates = JSON.stringify({
        x: Math.round(newPin.x * 10) / 10,
        y: Math.round(newPin.y * 10) / 10,
      });
      const request: AnnotationCreateRequest = {
        editorEmail,
        spatialCoordinates: coordinates,
        content: newPin.content.trim(),
      };
      const result = await createAnnotation(pageId, request);
      if (result) {
        setAnnotations((prev) => [...prev, result]);
      }
      setNewPin(null);
    } catch (e: unknown) {
      setError(
        e instanceof Error ? e.message : "Failed to create annotation"
      );
    } finally {
      setSubmitting(false);
    }
  }, [newPin, editorEmail, pageId]);

  const handleResolve = useCallback(
    async (annotationId: string) => {
      try {
        const result = await resolveAnnotation(pageId, annotationId);
        if (result) {
          setAnnotations((prev) =>
            prev.map((a) => (a.id === annotationId ? result : a))
          );
        }
      } catch (e: unknown) {
        setError(
          e instanceof Error ? e.message : "Failed to resolve annotation"
        );
      }
    },
    [pageId]
  );

  const parseCoords = (
    coords: string | null
  ): { x: number; y: number } | null => {
    if (!coords) return null;
    try {
      return JSON.parse(coords) as { x: number; y: number };
    } catch {
      return null;
    }
  };

  return (
    <div style={{ display: "flex", gap: "16px", fontFamily: "system-ui, sans-serif", color: "#e0e0e0" }}>
      <div style={{ flex: 1, minWidth: 0 }}>
        {/* Image area */}
        <div
          ref={imgRef}
          onClick={handleImageClick}
          style={{
            position: "relative",
            border: "1px solid #333",
            borderRadius: "8px",
            overflow: "hidden",
            cursor: readOnly ? "default" : "crosshair",
            background: "#1a1a2e",
          }}
        >
          <img
            src={pageImageUrl}
            alt="Page"
            style={{ display: "block", width: "100%", height: "auto" }}
            onError={(e) => {
              (e.target as HTMLImageElement).style.display = "none";
            }}
          />

          {/* Existing annotation pins */}
          {annotations.map((ann) => {
            const pos = parseCoords(ann.spatialCoordinates);
            if (!pos) return null;
            const isResolved = ann.resolved === "true";
            return (
              <div
                key={ann.id}
                title={`${ann.editorEmail}: ${ann.content}`}
                style={{
                  position: "absolute",
                  left: `${pos.x}%`,
                  top: `${pos.y}%`,
                  width: "20px",
                  height: "20px",
                  borderRadius: "50%",
                  background: isResolved ? pinColors.resolved : pinColors.unresolved,
                  border: "2px solid #fff",
                  transform: "translate(-50%, -50%)",
                  cursor: "pointer",
                  zIndex: 10,
                  boxShadow: "0 2px 4px rgba(0,0,0,0.4)",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: "10px",
                  fontWeight: "bold",
                  color: "#fff",
                }}
              >
                {isResolved ? "✓" : "!"}
              </div>
            );
          })}

          {/* New pin placement */}
          {newPin && (
            <div
              style={{
                position: "absolute",
                left: `${newPin.x}%`,
                top: `${newPin.y}%`,
                zIndex: 20,
              }}
            >
              {/* Pin marker */}
              <div
                style={{
                  width: "20px",
                  height: "20px",
                  borderRadius: "50%",
                  background: pinColors.new,
                  border: "2px solid #fff",
                  transform: "translate(-50%, -50%)",
                  boxShadow: "0 2px 4px rgba(0,0,0,0.4)",
                }}
              />
              {/* Content form popup */}
              {newPin.showForm && (
                <div
                  style={{
                    position: "absolute",
                    top: "12px",
                    left: "12px",
                    background: "#1e293b",
                    border: "1px solid #334155",
                    borderRadius: "8px",
                    padding: "12px",
                    minWidth: "250px",
                    boxShadow: "0 8px 16px rgba(0,0,0,0.5)",
                    zIndex: 30,
                  }}
                  onClick={(e) => e.stopPropagation()}
                >
                  <textarea
                    value={newPin.content}
                    onChange={(e) =>
                      setNewPin({ ...newPin, content: e.target.value })
                    }
                    placeholder="Enter annotation content..."
                    rows={3}
                    style={{
                      width: "100%",
                      background: "#0f172a",
                      color: "#e0e0e0",
                      border: "1px solid #334155",
                      borderRadius: "4px",
                      padding: "8px",
                      fontFamily: "inherit",
                      fontSize: "13px",
                      resize: "vertical",
                      boxSizing: "border-box",
                    }}
                  />
                  <div
                    style={{
                      display: "flex",
                      gap: "8px",
                      marginTop: "8px",
                      justifyContent: "flex-end",
                    }}
                  >
                    <button
                      onClick={() => setNewPin(null)}
                      style={{
                        padding: "6px 12px",
                        background: "#334155",
                        color: "#94a3b8",
                        border: "none",
                        borderRadius: "4px",
                        cursor: "pointer",
                        fontSize: "13px",
                      }}
                    >
                      Cancel
                    </button>
                    <button
                      onClick={handleSubmitAnnotation}
                      disabled={submitting || !newPin.content.trim()}
                      style={{
                        padding: "6px 12px",
                        background:
                          submitting || !newPin.content.trim()
                            ? "#475569"
                            : "#3b82f6",
                        color: "#fff",
                        border: "none",
                        borderRadius: "4px",
                        cursor:
                          submitting || !newPin.content.trim()
                            ? "not-allowed"
                            : "pointer",
                        fontSize: "13px",
                      }}
                    >
                      {submitting ? "Saving..." : "Save"}
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Sidebar */}
      <div
        className="annotations-sidebar"
        style={{
          width: "320px",
          flexShrink: 0,
          display: "flex",
          flexDirection: "column",
          gap: "8px",
        }}
      >
        <div
          style={{
            fontSize: "14px",
            fontWeight: 600,
            padding: "8px 0",
            borderBottom: "1px solid #334155",
          }}
        >
          Annotations ({annotations.length})
        </div>

        {loading && (
          <div style={{ color: "#94a3b8", fontSize: "13px" }}>
            Loading annotations...
          </div>
        )}

        {error && (
          <div
            style={{
              color: "#ef4444",
              fontSize: "13px",
              padding: "8px",
              background: "#7f1d1d33",
              borderRadius: "4px",
            }}
          >
            {error}
            <button
              onClick={loadAnnotations}
              style={{
                marginLeft: "8px",
                padding: "2px 8px",
                background: "transparent",
                color: "#ef4444",
                border: "1px solid #ef4444",
                borderRadius: "4px",
                cursor: "pointer",
                fontSize: "12px",
              }}
            >
              Retry
            </button>
          </div>
        )}

        {!loading && annotations.length === 0 && (
          <div style={{ color: "#64748b", fontSize: "13px", padding: "16px 0", textAlign: "center" }}>
            No annotations yet.
            {!readOnly && " Click on the page to add one."}
          </div>
        )}

        <div
          style={{
            display: "flex",
            flexDirection: "column",
            gap: "8px",
            overflowY: "auto",
            maxHeight: "500px",
          }}
        >
          {annotations.map((ann) => {
            const pos = parseCoords(ann.spatialCoordinates);
            const isResolved = ann.resolved === "true";
            return (
              <div
                key={ann.id}
                className="annotation-item"
                style={{
                  padding: "10px 12px",
                  background: isResolved ? "#064e3b22" : "#1e293b",
                  borderRadius: "6px",
                  border: `1px solid ${isResolved ? "#065f4633" : "#334155"}`,
                }}
              >
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "4px",
                  }}
                >
                  <span
                    style={{
                      fontSize: "11px",
                      color: "#94a3b8",
                    }}
                  >
                    {ann.editorEmail}
                  </span>
                  <span
                    style={{
                      fontSize: "10px",
                      padding: "2px 6px",
                      borderRadius: "10px",
                      background: isResolved
                        ? "#22c55e33"
                        : "#ef444433",
                      color: isResolved ? "#22c55e" : "#ef4444",
                    }}
                  >
                    {isResolved ? "Resolved" : "Open"}
                  </span>
                </div>
                <div
                  style={{
                    fontSize: "13px",
                    color: "#e0e0e0",
                    lineHeight: 1.4,
                    wordBreak: "break-word",
                  }}
                >
                  {ann.content}
                </div>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginTop: "6px",
                  }}
                >
                  <span style={{ fontSize: "11px", color: "#64748b" }}>
                    {pos ? `@ (${pos.x}%, ${pos.y}%)` : ""}
                    {ann.createdAt
                      ? ` • ${new Date(ann.createdAt).toLocaleDateString()}`
                      : ""}
                  </span>
                  {!isResolved && !readOnly && (
                    <button
                      onClick={() => handleResolve(ann.id)}
                      style={{
                        padding: "3px 10px",
                        background: "#22c55e33",
                        color: "#22c55e",
                        border: "1px solid #22c55e66",
                        borderRadius: "4px",
                        cursor: "pointer",
                        fontSize: "11px",
                      }}
                    >
                      Resolve
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
