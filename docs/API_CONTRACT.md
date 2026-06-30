# API Contract

> File nay la hop dong giua Backend, Frontend va Database. Neu doi endpoint/request/response/status thi phai cap nhat file nay truoc khi merge. `database/schema.sql` la source of truth cho persistence: table, DB role, DB status, vote value.

## 1. Local seed accounts

CÃ¡c account dÆ°á»›i Ä‘Ã¢y lÃ  seed/bootstrap data local Ä‘á»ƒ demo, khÃ´ng pháº£i nguá»“n Ä‘á»‹nh nghÄ©a nghiá»‡p vá»¥ thay cho `database/schema.sql`.

```text
admin@manga.local / Admin@123
mangaka@manga.local / Mangaka@123
assistant@manga.local / Assistant@123
editor@manga.local / Editor@123
board@manga.local / Board@123
board2@manga.local / Board2@123
board3@manga.local / Board3@123
```

## 2. Canonical tables, roles, statuses

Persistence tables chÃ­nh trong `database/schema.sql`:

```text
users, roles, permissions, role_permissions, skills, user_skills, assistant_profiles
proposals, series, board_votes, chapters, pages, tasks, submissions, annotations, reader_metrics
```

- Flow 1 proposal table is `proposals`; Flow 2 production table is `series`, linked by `series.proposal_id`. `database/schema.sql` is canonical for these fields.
- `chapters` vÃ  `pages` khÃ´ng cÃ³ status column trong DB.
- Regions lÃ  entity riÃªng (MangakaPageRegionDto), lÆ°u coordinates dáº¡ng x, y, widthPct, heightPct. Task gÃ¡n vÃ o region qua regionId.
- `submissions` lÆ°u output asset cá»§a Assistant. `annotations` lÆ°u editor page markup. `reader_metrics` dÃ¹ng cho metrics/ranking.
- API workflow filters hiá»‡n dÃ¹ng email (`authorEmail`, `editorEmail`, `memberEmail`, `assistantEmail`) dÃ¹ DB ownership dÃ¹ng user id FKs.

### Roles

| DB role | User-facing/code role |
|---|---|
| `Admin` | `Admin` |
| `Mangaka` | `Mangaka` |
| `Assistant` | `Assistant` |
| `Editor` | `TantouEditor` / Tantou Editor |
| `Board` | `EditorialBoardMember` / Editorial Board Member |

`users.status` trong DB: `ACTIVE`, `INACTIVE`.

### Proposal status

| DB `proposals.status` | UI/code wording |
|---|---|
| `DRAFT` | `Draft` |
| `SUBMITTED_TO_EDITOR` | `SubmittedToEditor` / Submitted to Tantou Editor |
| `REVISION_REQUESTED` | `NeedsRevision` |
| `UNDER_BOARD_REVIEW` | `UnderBoardReview` |
| `APPROVED` | `Approved` |
| `REJECTED` | `Rejected` |

Do not use `PENDING` as proposal status.

### Series status

| DB `series.status` | UI/code wording |
|---|---|
| `ACTIVE` | `Active` |
| `COMPLETED` | `Completed` |
| `CANCELLED` | `Cancelled` |

`ACTIVE` (default, set when series created after proposal approval). `COMPLETED` once all chapters are done. `CANCELLED` when board records a CANCEL decision.

### Task status

| DB `tasks.status` | UI/code wording |
|---|---|
| `ASSIGNED` | `Pending` / Assigned |
| `PENDING_REVIEW` | `Submitted` / Pending Review |
| `APPROVED` | `Approved` |
| `REVISION_REQUESTED` | `RedoRequested` / Revision Requested |

Do not use old `Completed` as task status.

### Chapter status

| DB `chapters.status` | UI/code wording |
|---|---|
| `DRAFT` | `DRAFT` |
| `IN_PROGRESS` | `IN_PROGRESS` |
| `COMPLETED` | `COMPLETED` |

DB default is `DRAFT`. Status auto-advances: DRAFT → IN_PROGRESS (when first page uploaded). Mangaka calls `completeChapter` to advance IN_PROGRESS → COMPLETED (backend validates all tasks Approved first).

### Page status

| DB `pages.status` | UI/code wording |
|---|---|
| `DRAFT` | `DRAFT` |
| `IN_TASK` | `IN_TASK` |
| `DONE` | `DONE` |

`DRAFT` when page is first uploaded. `IN_TASK` after at least one task is assigned. `DONE` when all tasks on the page are Approved.

### Board votes

`board_votes.decision`: `APPROVE`, `REJECT`. DB enforces one vote per board member per proposal.

## 3. Manuscript validation and AI summary - Owner: Member 1

```text
POST /api/mangaka/proposals/preview-upload
```

Expected response fields:

```text
valid
summary
warningMessages
fileName
fileType
fileSize
```

## 4. Auth/Admin/Mangaka proposal - Owner: Member 1

```text
POST /api/auth/login
GET /admin/accounts
POST /admin/accounts
PUT /admin/accounts/{id}
DELETE /admin/accounts/{id}
GET /admin/skills
POST /admin/skills
PUT /admin/skills/{id}
DELETE /admin/skills/{id}

GET /api/mangaka/proposals?authorEmail={email}
POST /api/mangaka/proposals
PUT /api/mangaka/proposals/{id}
DELETE /api/mangaka/proposals/{id}?authorEmail={email}
POST /api/mangaka/proposals/upload
GET /api/mangaka/proposals/files/{fileName}
PUT /api/mangaka/proposals/{id}/submit
```

> âš ï¸ Admin prefix lÃ  `/admin`, KHÃ”NG cÃ³ `/api` trÆ°á»›c. CÃ¡c nhÃ³m cÃ²n láº¡i Ä‘á»u cÃ³ `/api/`.

## 5. Review/Board - Owner: Member 2

```text
GET /api/editor/proposals?editorEmail={email}
PUT /api/editor/proposals/{id}/request-revision
PUT /api/editor/proposals/{id}/forward-board

GET /api/board/proposals?memberEmail={email}
PUT /api/board/proposals/{id}/approve
PUT /api/board/proposals/{id}/reject
```

## 6. Production/Assistant - Owner: Member 2

```text
GET /api/mangaka/proposals/{proposalId}/chapters?authorEmail={email}
POST /api/mangaka/proposals/{proposalId}/chapters?authorEmail={email}
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages?authorEmail={email}
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions?authorEmail={email}
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks?authorEmail={email}

GET /api/assistant/tasks?assistantEmail={email}
PUT /api/assistant/tasks/{taskId}/start
PUT /api/assistant/tasks/{taskId}/submit

PUT /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/approve?authorEmail={email}
PUT /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/redo?authorEmail={email}
PUT /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/complete?authorEmail={email}
```

## 7. Backend runtime profiles

- `application.properties`: PostgreSQL config máº·c Ä‘á»‹nh, láº¥y tá»« env `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DDL_AUTO`.
- `application-local.properties`: H2 local demo, mode PostgreSQL.
- `application-demo.properties`: táº¯t datasource/JPA auto-config Ä‘á»ƒ cháº¡y demo in-memory service.

## 8. Rule cap nhat API

Khi them/sua API, ghi vao day:

```text
Endpoint:
Owner:
Purpose:
Request body:
Response body:
Status transition:
Frontend screen using it:
Database tables touched:
```


