# Demo Script

> Cap nhat 2026-06-27: Da cat AI summary preview va region drawing. Task gan vao page (khong ve vung pixel).

## 1. Muc tieu demo

Chung minh workflow manga end-to-end:

```text
Flow 1 (DONE): Mangaka -> Tantou Editor -> Editorial Board -> Approved/Rejected
Flow 2 (IN PROGRESS): Mangaka -> Production (chapter/page/task) -> Assistant -> Mangaka review
```

**Nhung gi DA CAT khoi demo:**

- ~~AI summary preview truoc khi upload~~
- ~~Region drawing / VisualCanvas~~
- ~~Annotations (editor markup pins tren anh)~~
- ~~Rankings / reader metrics screen~~

## 2. Chuan bi

Backend (local H2 - khong can PostgreSQL):

```bash
cd backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Frontend:

```bash
cd "Build as requested"
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

DB roles: `Admin`, `Mangaka`, `Assistant`, `Editor`, `Board`.
UI map: `Editor` -> Tantou Editor; `Board` -> Editorial Board Member.

## 4. Flow 1 — Proposal/Approval (DA HOAN THANH)

1. Login Mangaka (`mangaka@manga.local`).
2. Tao series/proposal moi: title, genre, synopsis. Status: `DRAFT` / `Draft`.
3. Chon manuscript file, validate type/size (JS validation), upload.
4. Bam `Save & Submit to Tantou`: status sang `SUBMITTED_TO_EDITOR` / `SubmittedToEditor`.
5. Login Tantou Editor (`editor@manga.local`).
6. Mo proposal vua submit, download manuscript neu can.
7. Chon mot trong hai:
   - `Request Revision`: status -> `REVISION_REQUESTED` / `NeedsRevision`. Mangaka update va submit lai.
   - `Forward to Board`: status -> `UNDER_BOARD_REVIEW` / `UnderBoardReview`.
8. Login lan luot 3 Board accounts:
   - `board@manga.local / Board@123`
   - `board2@manga.local / Board2@123`
   - `board3@manga.local / Board3@123`
9. Moi Board member vote `APPROVE` hoac `REJECT`. Chi duoc vote 1 lan/member/series.
10. Sau khi du 3 phieu, he thong tu quyet dinh theo da so.
11. Approved -> `series` duoc tao/update tu proposal, dung title proposal lam series title.

## 5. Flow 2 — Production (DANG IMPLEMENT)

> Task gan vao PAGE, khong can ve vung (no region drawing).

12. Login lai Mangaka. Proposal da chuyen sang `APPROVED`.
13. Vao Production: tao Chapter moi (title, so chuong).
14. Them Page vao chapter (so trang, tuy chon upload anh trang).
15. Tao Task tren page:
    - Mo ta cong viec
    - Payment amount
    - Chon assistant de assign
    - `region_coordinates` = null (khong can ve)
16. Login Assistant (`assistant@manga.local`).
17. Xem task duoc giao kem chapter/page context.
18. Bam `Start` de nhan task. Status: `ASSIGNED`.
19. Lam xong, bam `Submit`: upload file + nhap note. Status: `PENDING_REVIEW` / `Submitted`.
20. Login lai Mangaka.
21. Mo task vua submit, xem file/note.
22. Chon:
    - `Approve`: task chuyen `APPROVED` / `Approved`.
    - `Request Redo`: task chuyen `REVISION_REQUESTED` / `RedoRequested`. Assistant lam lai.

## 6. Checklist ket qua

**Flow 1:**
- Series status: `DRAFT` -> `SUBMITTED_TO_EDITOR` -> `REVISION_REQUESTED` hoac `UNDER_BOARD_REVIEW` -> `APPROVED`/`REJECTED`.
- Khong dung status cu nhu `PENDING` hoac `InProduction`.
- Board vote value: `APPROVE`/`REJECT`; moi member chi vote 1 lan.

**Flow 2:**
- Chapter/page/task tao duoc va luu DB.
- Task status: `ASSIGNED` -> `PENDING_REVIEW` -> `APPROVED` hoac `REVISION_REQUESTED`.
- File submission luu vao `storage-server/submissions/`.
- UI khong bao loi, backend khong crash.
