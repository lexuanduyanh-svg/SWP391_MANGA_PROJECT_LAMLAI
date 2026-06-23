# Demo Script

## 1. Muc tieu demo

Chung minh workflow manga end-to-end theo `database/schema.sql`:

```text
Flow 1: Mangaka -> Tantou Editor -> Editorial Board -> Approved/Rejected
Flow 2: Mangaka -> Production (chapter/page/region/task) -> Assistant -> Mangaka review
```

`series` la table unified cho proposal/series; status DB hien thi kem mapping UI/code trong demo.

## 2. Chuan bi

Backend:

```bash
cd backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

URL:

```text
Frontend: http://localhost:5173
Backend: http://localhost:8080
```

## 3. Local seed accounts

```text
admin@manga.local / Admin@123
mangaka@manga.local / Mangaka@123
assistant@manga.local / Assistant@123
editor@manga.local / Editor@123
board@manga.local / Board@123
board2@manga.local / Board2@123
board3@manga.local / Board3@123
```

DB roles seeded: `Admin`, `Mangaka`, `Assistant`, `Editor`, `Board`. UI gọi `Editor` là Tantou Editor và `Board` là Editorial Board Member.

## 4. Flow demo chinh

1. Login Mangaka.
2. Tao series/proposal moi ở trạng thái `DRAFT` / `Draft` với title, genre, synopsis.
3. Chon manuscript file, preview validate/summary, confirm rồi upload.
4. Bấm `Save & Submit to Tantou`: status sang `SUBMITTED_TO_EDITOR` / `SubmittedToEditor`.
5. Login Tantou Editor (`/api/editor/...`).
6. Mo proposal vua submit, download manuscript neu can.
7. Chon mot trong hai nhanh:
   - `Request Revision`: status sang `REVISION_REQUESTED` / `NeedsRevision`; Mangaka update và submit lại.
   - `Forward to Board`: status sang `UNDER_BOARD_REVIEW` / `UnderBoardReview`.
8. Login lan luot Board accounts (`/api/board/...`).
9. Moi Board member vote `APPROVE` hoặc `REJECT`; DB chỉ cho một vote/member/series.
10. Sau khi Board approve, hệ thống tạo/update `series` từ proposal, dùng title của proposal làm series title.
11. Neu approved, login lai Mangaka.
12. Mangaka quản lý production: tạo `chapters`, `pages`, task region bằng `tasks.region_coordinates` JSONB; DB không có table regions riêng.
13. Assign task cho Assistant: DB status `ASSIGNED`, UI có thể hiển thị `Pending`/Assigned.
14. Login Assistant, xem `/api/assistant/tasks?assistantEmail=...`.
15. Assistant submit work asset/note: output lưu ở `submissions`, task sang `PENDING_REVIEW` / `Submitted`.
16. Login lai Mangaka, approve hoặc request redo bằng nested endpoint:
    - `.../tasks/{taskId}/approve`: `APPROVED` / `Approved`.
    - `.../tasks/{taskId}/redo`: `REVISION_REQUESTED` / `RedoRequested`.

## 5. Checklist ket qua

- Series status đi đúng: `DRAFT` -> `SUBMITTED_TO_EDITOR` -> `REVISION_REQUESTED` hoặc `UNDER_BOARD_REVIEW` -> `APPROVED`/`REJECTED`.
- Không dùng proposal status cũ như `PENDING` hoặc `InProduction`.
- Board vote value là `APPROVE`/`REJECT`; mỗi member chỉ vote một lần.
- Task status đi đúng: `ASSIGNED` -> `PENDING_REVIEW` -> `APPROVED` hoặc `REVISION_REQUESTED`.
- UI khong bao loi do, backend khong crash.
