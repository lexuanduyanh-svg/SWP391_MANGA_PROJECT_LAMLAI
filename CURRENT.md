# CURRENT.md — Flow 2 Production Progress

## Goal
Restore Flow 2 ve ban full truoc khi giam scope con 3 nguoi.

## Constraints & Preferences
- Proposal = Flow 1 (pitch/review), Series = Flow 2 (production, created after approval).
- Frontend structure: form.txt target architecture.
- Task assignment is region-based: chapter -> page -> region -> task.
- No AI Summary.

## Progress
### Done
- **Flow 2 deep design**: Documented Series/Chapter/Page/Task lifecycles, state machines, business rules, authorization matrix, data model, sequence diagrams.
- **Core Flow 2 Restored**: region drawing (VisualCanvas), annotations, rankings, series decisions.
- **Backend: Annotations API** (`/api/pages/{pageId}/annotations`) — AnnotationEntity, AnnotationRepository, InMemoryAnnotationService, AnnotationController. Full CRUD + resolve. Seed data on page 601.
- **Backend: Reader Metrics & Rankings API** (`/api/series/{seriesId}/metrics`, `/api/rankings`) — ReaderMetricEntity (preexisting), ReaderMetricRepository (preexisting), InMemoryReaderMetricService, ReaderMetricController. Composite score formula: (sales*0.4 + likes*0.3 + shares*0.2 + votes*0.1)/1000. Seed 2 metrics for series 801.
- **Backend: Series Decisions API** (`/api/series/{seriesId}/decisions`, `/api/decisions`) — SeriesDecisionEntity, SeriesDecisionRepository, InMemorySeriesDecisionService, SeriesDecisionController. Decision types: MAINTAIN/RESCHEDULE/CANCEL/CHANGE_FORMAT. Seed 1 decision for series 801.
- **30 new backend files** across entities, repositories, DTOs, services, controllers.
- **Frontend: types** — `annotation.ts`, `ranking.ts`, `seriesDecision.ts`
- **Frontend: services** — `annotationService.ts`, `rankingService.ts`, `seriesDecisionService.ts`
- **Frontend: PageAnnotations.tsx** — Canvas-like annotation pin system on page images (click to add pin, content popup, resolve, sidebar list, color-coded open/resolved).
- **Frontend: RankingsDashboard.tsx** — Two-tab dashboard (Metrics Input form + Rankings leaderboard with medal emoji, composite scores, sortable table).
- **Frontend: SeriesDecisionsPanel.tsx** — Panel with decision history + new decision form (type selector with conditional fields for frequency/format).
- **Frontend: SeriesDecisionsDashboard.tsx** — Full-page overview with search, grouped-by-series cards, drill-down to detail panel.
- **Updated styles.css** — Appended styles for annotations, rankings, decisions components.
- **Backend tests**: 29/29 tests pass.
- **Frontend build**: `npm run build` passes.
- **Fixed taskType misconception**: Removed task type dropdown (skill FK), payment, referenceFileName, regionCoordinates from TaskEntity/DTOs/services. Task assignment is now: select region → write free-text instructions → set deadline → assign assistant. Updated all docs (CURRENT.md, MVP_SCOPE_AND_BUSINESS_RULES.md, database.txt, schema.sql, postman, TEAM_TASK_ASSIGNMENT.md).
- **Simplified chapter status**: MangakaChapterStatus 7→3: DRAFT, IN_PROGRESS, COMPLETED. Updated backend enum, service, tests (29/29 pass).
- **completeChapter endpoint**: `PUT /chapters/{chapterId}/complete`. Validates all tasks Approved, sets COMPLETED, notifies Editor. Frontend Publish button + status badge on chapter cards.
- **Fixed Flow 1 API URL mismatches**: TantouEditor frontend was calling `/api/tantou-editor/` (wrong) → corrected to `/api/editor/`. EditorialBoard frontend was calling `/api/editorial-board/` → corrected to `/api/board/`.
- **Added editor Reject button**: TantouEditorDashboard: added Reject button (was missing despite backend having rejectByEditor).
- **Cleaned dead tables**: Removed `permissions`, `role_permissions`, `assistant_profiles` tables + their 7 Java entity/repo files (no service code used them).
- **Cleaned series status**: Schema: `HIATUS` removed, kept `ACTIVE`/`COMPLETED`/`CANCELLED`.
- **Fixed chapters default**: `DEFAULT 'Draft'` → `'DRAFT'` to match enum.

### In Progress
- (none)

### Blocked
- Render build failing — needs build logs from user to diagnose

## Key Decisions
- Page Annotations: spatial coordinates stored as JSONB `{x, y}` percentage strings, rendered as positioned circle pins.
- Composite score formula: weighted linear (sales 40%, likes 30%, shares 20%, votes 10%) / 1000.
- Series Decisions: dedicated entity + table (not modifying series status directly), for audit trail.
- All new services follow existing memory-mode pattern (Map + inner record + toDto() + seed data).
- New controllers follow the existing try/catch → ResponseEntity CONFLICT pattern.
- Task assignment = select region + write free-text instructions (NOT a skill-type dropdown)
- Chapter statuses simplified to 3: DRAFT → IN_PROGRESS → COMPLETED
- COMPLETED = all tasks on all pages are Approved, then mangaka clicks "Publish" button → notifies Editor

## Next Steps
1. Get Render build logs to debug Docker build failure
2. Integrate PageAnnotations into TantouEditorDashboard for editors to annotate pages.
3. Integrate RankingsDashboard into EditorialBoardDashboard.
4. Integrate SeriesDecisionsPanel/Dashboard into EditorialBoardDashboard.
5. Frontend routing: wire new components into the app's login→dashboard flow.
6. Demo end-to-end flows.

## Critical Context
- `MangakaDashboard.tsx` is ~3067 lines — contains Flow 1 (proposal CRUD) + Flow 2 (chapter/page/task/region) in one file.
- Backend seed has proposal "4" (Approved) linked to series "801", with seed chapter "600" and page "601".
- Annotations are seeded on page "601" (2 sample annotations).
- Reader metrics seeded with 2 entries for series "801".
- Series decisions seeded with 1 MAINTAIN decision for series "801".
- API endpoints consolidated: AnnotationController at `/api/pages/{pageId}/annotations`, ReaderMetricController at `/api/series/{seriesId}/metrics` + `/api/rankings`, SeriesDecisionController at `/api/series/{seriesId}/decisions` + `/api/decisions`.
- `npm run build` passes. Backend compile + tests pass.

## Relevant Files
- `backend/.../entities/AnnotationEntity.java`
- `backend/.../entities/SeriesDecisionEntity.java`
- `backend/.../repositories/AnnotationRepository.java`
- `backend/.../repositories/SeriesDecisionRepository.java`
- `backend/.../services/InMemoryAnnotationService.java`
- `backend/.../services/InMemoryReaderMetricService.java`
- `backend/.../services/InMemorySeriesDecisionService.java`
- `backend/.../controllers/AnnotationController.java`
- `backend/.../controllers/ReaderMetricController.java`
- `backend/.../controllers/SeriesDecisionController.java`
- `backend/.../dtos/AnnotationDto.java`, `AnnotationCreateRequest.java`
- `backend/.../dtos/ReaderMetricDto.java`, `ReaderMetricCreateRequest.java`, `SeriesRankingDto.java`
- `backend/.../dtos/SeriesDecisionDto.java`, `SeriesDecisionRequest.java`
- `frontend/src/types/annotation.ts`, `ranking.ts`, `seriesDecision.ts`
- `frontend/src/services/annotationService.ts`, `rankingService.ts`, `seriesDecisionService.ts`
- `frontend/src/components/PageAnnotations.tsx`
- `frontend/src/components/RankingsDashboard.tsx`
- `frontend/src/components/SeriesDecisionsPanel.tsx`
- `frontend/src/components/SeriesDecisionsDashboard.tsx`
