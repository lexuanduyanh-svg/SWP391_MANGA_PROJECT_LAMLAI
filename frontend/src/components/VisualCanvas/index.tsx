import { useEffect, useRef, useState, useCallback } from "react";

export interface RegionRect {
  id: string;
  x: number; // percentage 0-100
  y: number;
  widthPct: number;
  heightPct: number;
  regionType: string;
  note: string;
}

interface VisualCanvasProps {
  /** URL of the page image to display */
  imageUrl: string;
  /** Existing regions to render */
  regions: RegionRect[];
  /** Called when a new region is drawn */
  onRegionCreate?: (rect: {
    x: number;
    y: number;
    widthPct: number;
    heightPct: number;
  }) => void;
  /** Called when a region is clicked (for edit/delete) */
  onRegionClick?: (region: RegionRect) => void;
  /** If true, drawing is disabled */
  readonly?: boolean;
  /** If true, shows drawing mode UI */
  drawingMode?: boolean;
  onToggleDrawing?: () => void;
}

const REGION_COLORS = [
  "rgba(59, 130, 246, 0.3)",
  "rgba(239, 68, 68, 0.3)",
  "rgba(34, 197, 94, 0.3)",
  "rgba(234, 179, 8, 0.3)",
  "rgba(168, 85, 247, 0.3)",
  "rgba(249, 115, 22, 0.3)",
];

export default function VisualCanvas({
  imageUrl,
  regions,
  onRegionCreate,
  onRegionClick,
  readonly = false,
  drawingMode = false,
  onToggleDrawing,
}: VisualCanvasProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [drawing, setDrawing] = useState(false);
  const [startPos, setStartPos] = useState({ x: 0, y: 0 });
  const [currentPos, setCurrentPos] = useState({ x: 0, y: 0 });
  const [imgLoaded, setImgLoaded] = useState(false);

  const getPos = useCallback(
    (e: React.MouseEvent<HTMLDivElement>) => {
      const rect = containerRef.current?.getBoundingClientRect();
      if (!rect) return { x: 0, y: 0 };
      return {
        x: ((e.clientX - rect.left) / rect.width) * 100,
        y: ((e.clientY - rect.top) / rect.height) * 100,
      };
    },
    [],
  );

  const handleMouseDown = useCallback(
    (e: React.MouseEvent<HTMLDivElement>) => {
      if (!drawingMode || readonly) return;
      setDrawing(true);
      setStartPos(getPos(e));
      setCurrentPos(getPos(e));
    },
    [drawingMode, readonly, getPos],
  );

  const handleMouseMove = useCallback(
    (e: React.MouseEvent<HTMLDivElement>) => {
      if (!drawing) return;
      setCurrentPos(getPos(e));
    },
    [drawing, getPos],
  );

  const handleMouseUp = useCallback(() => {
    if (!drawing) return;
    setDrawing(false);
    const x = Math.min(startPos.x, currentPos.x);
    const y = Math.min(startPos.y, currentPos.y);
    const w = Math.abs(currentPos.x - startPos.x);
    const h = Math.abs(currentPos.y - startPos.y);
    if (w > 1 && h > 1 && onRegionCreate) {
      onRegionCreate({ x, y, widthPct: w, heightPct: h });
    }
  }, [drawing, startPos, currentPos, onRegionCreate]);

  const previewRect = drawing
    ? {
        left: `${Math.min(startPos.x, currentPos.x)}%`,
        top: `${Math.min(startPos.y, currentPos.y)}%`,
        width: `${Math.abs(currentPos.x - startPos.x)}%`,
        height: `${Math.abs(currentPos.y - startPos.y)}%`,
      }
    : null;

  return (
    <div style={{ position: "relative", display: "inline-block", maxWidth: "100%" }}>
      {!readonly && onToggleDrawing && (
        <div style={{ marginBottom: 8 }}>
          <button
            onClick={onToggleDrawing}
            style={{
              padding: "6px 16px",
              background: drawingMode ? "#ef4444" : "#3b82f6",
              color: "#fff",
              border: "none",
              borderRadius: 6,
              cursor: "pointer",
              fontSize: 14,
            }}
          >
            {drawingMode ? "Exit Draw Mode" : "Draw Region"}
          </button>
          {drawingMode && (
            <span style={{ marginLeft: 12, fontSize: 13, color: "#666" }}>
              Click and drag to draw a region on the page
            </span>
          )}
        </div>
      )}

      <div
        ref={containerRef}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseUp}
        style={{
          position: "relative",
          cursor: drawingMode ? "crosshair" : "default",
          border: "1px solid #e5e7eb",
          borderRadius: 8,
          overflow: "hidden",
          display: "inline-block",
        }}
      >
        {/* Page Image */}
        <img
          src={imageUrl}
          alt="Page"
          onLoad={() => setImgLoaded(true)}
          style={{
            display: "block",
            maxWidth: 600,
            maxHeight: 800,
            opacity: imgLoaded ? 1 : 0.5,
          }}
        />

        {/* Existing Regions */}
        {regions.map((r, i) => (
          <div
            key={r.id}
            onClick={(e) => {
              e.stopPropagation();
              onRegionClick?.(r);
            }}
            style={{
              position: "absolute",
              left: `${r.x}%`,
              top: `${r.y}%`,
              width: `${r.widthPct}%`,
              height: `${r.heightPct}%`,
              background: REGION_COLORS[i % REGION_COLORS.length],
              border: "2px solid rgba(59, 130, 246, 0.8)",
              borderRadius: 4,
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontSize: 11,
              color: "#fff",
              fontWeight: "bold",
              textShadow: "0 1px 2px rgba(0,0,0,0.5)",
              transition: "border-color 0.15s",
              boxSizing: "border-box",
            }}
            title={`${r.regionType}: ${r.note || "No note"}`}
          >
            <span style={{ pointerEvents: "none" }}>{r.regionType}</span>
          </div>
        ))}

        {/* Preview rectangle while drawing */}
        {previewRect && (
          <div
            style={{
              position: "absolute",
              left: previewRect.left,
              top: previewRect.top,
              width: previewRect.width,
              height: previewRect.height,
              background: "rgba(59, 130, 246, 0.15)",
              border: "2px dashed #3b82f6",
              borderRadius: 4,
              pointerEvents: "none",
              boxSizing: "border-box",
            }}
          />
        )}

        {/* Fallback placeholder when image not loaded */}
        {!imgLoaded && (
          <div
            style={{
              position: "absolute",
              inset: 0,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              color: "#9ca3af",
              fontSize: 14,
            }}
          >
            Loading page image...
          </div>
        )}
      </div>

      {/* Region Legend */}
      {regions.length > 0 && (
        <div style={{ marginTop: 8, display: "flex", flexWrap: "wrap", gap: 8 }}>
          {regions.map((r, i) => (
            <div
              key={r.id}
              style={{
                display: "flex",
                alignItems: "center",
                gap: 4,
                fontSize: 12,
                color: "#374151",
              }}
            >
              <span
                style={{
                  width: 12,
                  height: 12,
                  borderRadius: 2,
                  background: REGION_COLORS[i % REGION_COLORS.length],
                  border: "1px solid rgba(59, 130, 246, 0.6)",
                  display: "inline-block",
                }}
              />
              <span>
                {r.regionType}
                {r.note ? `: ${r.note}` : ""}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
