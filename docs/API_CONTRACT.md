# API Contract

> File nay la hop dong giua Backend, Frontend va Database. Neu doi endpoint/request/response/status thi phai cap nhat file nay truoc khi merge. `database/schema.sql` la source of truth cho persistence: table, DB role, DB status, vote value.

## 1. Local seed accounts

Các account dưới đây là seed/bootstrap data local để demo, không phải nguồn định nghĩa nghiệp vụ thay cho `database/schema.sql`.

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

Persistence tables chính trong `database/schema.sql`:

```text
users, roles, permissions, role_permissions, skills, user_skills, assistant_profiles
series, board_votes, chapters, pages, tasks, submissions, annotations, reader_metrics
```

- Unified proposal/series table is `series`; không có proposal table riêng. Trường chính: `series_id`, `mangaka_id`, `tantou_editor_id`, `title`, `synopsis`, `genre`, `status`, `publishing_frequency`, `editor_notes`, timestamps.
- `chapters` và `pages` không có status column trong DB.
- Không có table `regions`; region data nằm trong `tasks.region_coordinates` JSONB.
- `submissions` lưu output asset của Assistant. `annotations` lưu editor page markup. `reader_metrics` dùng cho metrics/ranking.
- API workflow filters hiện dùng email (`authorEmail`, `editorEmail`, `memberEmail`, `assistantEmail`) dù DB ownership dùng user id FKs.

### Roles

| DB role | User-facing/code role |
|---|---|
| `Admin` | `Admin` |
| `Mangaka` | `Mangaka` |
| `Assistant` | `Assistant` |
| `Editor` | `TantouEditor` / Tantou Editor |
| `Board` | `EditorialBoardMember` / Editorial Board Member |

`users.status` trong DB: `ACTIVE`, `INACTIVE`.

### Series/proposal status

| DB `series.status` | UI/code wording |
|---|---|
| `DRAFT` | `Draft` |
| `SUBMITTED_TO_EDITOR` | `SubmittedToEditor` / Submitted to Tantou Editor |
| `REVISION_REQUESTED` | `NeedsRevision` |
| `UNDER_BOARD_REVIEW` | `UnderBoardReview` |
| `APPROVED` | `Approved` |
| `REJECTED` | `Rejected` |

Do not use `PENDING` as proposal/series status. `Serializing` may appear only as backward app compatibility wording because DB schema does not define `SERIALIZING`.

### Task status

| DB `tasks.status` | UI/code wording |
|---|---|
| `ASSIGNED` | `Pending` / Assigned |
| `PENDING_REVIEW` | `Submitted` / Pending Review |
| `APPROVED` | `Approved` |
| `REVISION_REQUESTED` | `RedoRequested` / Revision Requested |

Do not use old `Completed` as task status.

### Board votes

`board_votes.decision`: `APPROVE`, `REJECT`. DB enforces one vote per board member per series.

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

> ⚠️ Admin prefix là `/admin`, KHÔNG có `/api` trước. Các nhóm còn lại đều có `/api/`.

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
```

## 7. Backend runtime profiles

- `application.properties`: PostgreSQL config mặc định, lấy từ env `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DDL_AUTO`.
- `application-local.properties`: H2 local demo, mode PostgreSQL.
- `application-demo.properties`: tắt datasource/JPA auto-config để chạy demo in-memory service.

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
