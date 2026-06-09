# API Contract

> File nay la hop dong giua Backend, Frontend va Database. Neu doi endpoint/request/response/status thi phai cap nhat file nay truoc khi merge.

## 1. Demo accounts

```text
admin@manga.local / Admin@123
mangaka@manga.local / Mangaka@123
assistant@manga.local / Assistant@123
editor@manga.local / Editor@123
board@manga.local / Board@123
board2@manga.local / Board2@123
board3@manga.local / Board3@123
```

## 2. Status chinh

Proposal status:

```text
Draft
SubmittedToEditor
NeedsRevision
UnderBoardReview
Approved
Rejected
InProduction
```

Task status:

```text
Pending
InProgress
Submitted
RedoRequested
Completed
```

## 3.5 Manuscript validation and AI summary - Owner: Member 1

```text
POST /api/mangaka/proposals/preview-upload
```

### Purpose

- Validate selected manuscript file before final upload.
- Return a short content summary for Mangaka confirmation.
- Return warnings if file type, size, or content extraction is not suitable.

### Expected response fields

```text
valid
summary
warningMessages
fileName
fileType
fileSize
```

## 3. Auth/Admin/Proposal - Owner: Member 1

```text
POST /api/auth/login
GET /api/admin/accounts
POST /api/admin/accounts
PUT /api/admin/accounts/{id}
DELETE /api/admin/accounts/{id}
GET /api/admin/skills
POST /api/admin/skills
PUT /api/admin/skills/{id}
DELETE /api/admin/skills/{id}
GET /api/mangaka/proposals
POST /api/mangaka/proposals
PUT /api/mangaka/proposals/{id}
DELETE /api/mangaka/proposals/{id}
POST /api/mangaka/proposals/upload
GET /api/mangaka/proposals/files/{fileName}
PUT /api/mangaka/proposals/{id}/submit
```

## 4. Review/Board/Production/Assistant - Owner: Member 2

```text
GET /api/editor/proposals
PUT /api/editor/proposals/{id}/request-revision
PUT /api/editor/proposals/{id}/forward-board
GET /api/board/proposals
PUT /api/board/proposals/{id}/approve
PUT /api/board/proposals/{id}/reject
GET /api/mangaka/proposals/{proposalId}/chapters
POST /api/mangaka/proposals/{proposalId}/chapters
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks
GET /api/assistant/tasks
PUT /api/assistant/tasks/{taskId}/start
PUT /api/assistant/tasks/{taskId}/submit
PUT /api/mangaka/tasks/{taskId}/approve
PUT /api/mangaka/tasks/{taskId}/redo
```

## 5. Rule cap nhat API

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
