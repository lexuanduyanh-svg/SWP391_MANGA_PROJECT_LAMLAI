# SWP391 Manga Project Lam Lai

Project cho hệ thống **Manga Creation Workflow and Publishing Management System**.

> **Cập nhật 2026-06-27:** Nhóm còn 3 người (2 backend + 1 frontend), 3 tuần còn lại. Đã giảm scope: bỏ AI summary preview, bỏ region drawing, bỏ annotations, bỏ rankings screen. Task được gán ở mức **page** thay vì pixel-level region. Chi tiết tại `docs/requirements/MVP_SCOPE_AND_BUSINESS_RULES.md` mục 4.5 và `docs/TEAM_TASK_ASSIGNMENT.md`.

Bản này được tạo lại tại `SWP391_NEW` để nhóm chia module và merge dễ hơn. Luồng chính tách 2 bước:
1. Mangaka tạo series/proposal, upload manuscript, Tantou Editor review, Editorial Board vote.
2. Sau khi approved thì Mangaka vào production, tạo chapter/page/task, Assistant submit work và Mangaka duyệt kết quả.

Repository đang dùng để demo/develop:

```text
https://github.com/lexuanduyanh-svg/SWP391_MANGA_PROJECT_LAMLAI
```

## 1. Source of truth persistence

`database/schema.sql` là canonical source of truth cho persistence: table, DB roles, status enum/value và quan hệ dữ liệu. Tài liệu/API/UI phải map theo schema này.

- DB roles seeded: `Admin`, `Mangaka`, `Assistant`, `Editor`, `Board`.
- UI/code roles: `Admin`, `Mangaka`, `Assistant`, `TantouEditor`/Tantou Editor -> DB `Editor`, `EditorialBoardMember`/Editorial Board Member -> DB `Board`.
- Unified proposal/series table: `series` (`series_id`, `mangaka_id`, `tantou_editor_id`, `title`, `synopsis`, `genre`, `status`, `publishing_frequency`, `editor_notes`, timestamps).
- Series DB statuses: `DRAFT`, `SUBMITTED_TO_EDITOR`, `REVISION_REQUESTED`, `UNDER_BOARD_REVIEW`, `APPROVED`, `REJECTED`.
- UI/code series wording: Draft, SubmittedToEditor, NeedsRevision, UnderBoardReview, Approved, Rejected.
- Task DB statuses: `ASSIGNED`, `PENDING_REVIEW`, `APPROVED`, `REVISION_REQUESTED`.
- UI/code task wording: Pending/Assigned, Submitted/Pending Review, Approved, RedoRequested/Revision Requested.
- `tasks.region_coordinates` JSONB column kept in schema but **not used in V2 UI** (set null or `{"fullPage": true}`); no VisualCanvas region drawing.
- `submissions` store Assistant output assets (file + note).
- `annotations` and `reader_metrics` **kept in schema but NOT implemented in V2 UI/API**.

## 2. Trạng thái hiện tại

**Flow 1 (Proposal workflow) — HOÀN THÀNH ✅**

- Login theo role và lưu phiên đăng nhập ở frontend bằng `localStorage`.
- Admin dashboard: quản lý account demo, skill/category và gán skill/category cho account.
- Mangaka dashboard (Flow 1):
  - Tạo/sửa/xóa proposal.
  - Validate file đầu vào trước upload (JS validation: type, size).
  - Upload manuscript file thật qua backend.
  - `Save & Submit to Tantou` để lưu và gửi proposal trong một bước.
  - Theo dõi trạng thái review bằng modal chi tiết.
- Tantou Editor dashboard:
  - Xem proposal mới nhất trước.
  - Download manuscript.
  - Forward proposal sang Editorial Board.
  - Request revision cho Mangaka.
- Editorial Board dashboard:
  - Có 3 tài khoản Board demo.
  - Mỗi Board member được vote một lần: Approve hoặc Reject.
  - Khi đủ 3 phiếu, backend tự quyết định theo đa số.
  - Approve đa số chuyển series sang `APPROVED` / `Approved`, mở production cho Mangaka.

**Flow 2 (Production workflow) — ĐANG IMPLEMENT (3 tuần)**

- Sau khi Board approve, Mangaka tạo chapter/page/task (task gán ở page level, không có region).
- Assistant nhận/start/submit task (upload file + note).
- Mangaka review task: approve hoặc request redo.

**Đã CẮT khỏi V2 scope:**

- ~~AI summary preview~~ (cần Python service riêng)
- ~~Region drawing / VisualCanvas~~ (task gán page level)
- ~~Annotations (editor markup pins)~~ (dùng `series.editor_notes` text thay thế)
- ~~Rankings screen / reader metrics UI~~
- ~~Earnings calculation logic~~ (chỉ hiển thị seed value)

Backend Java Spring Boot, persistence qua PostgreSQL hoặc H2 local profile.
Frontend React + Vite + TypeScript (folder `Build as requested/`).

## 3. Demo flow chính để trình bày

Luồng khuyến nghị khi demo với giáo viên:

1. Login `mangaka@manga.local / Mangaka@123`.
2. Vào Mangaka proposal form, nhập title/genre/synopsis và upload manuscript file.
3. Bấm `Save & Submit to Tantou`.
4. Login `editor@manga.local / Editor@123`.
5. Tantou Editor mở proposal mới, download manuscript nếu cần, rồi `Forward to Board` hoặc `Request Revision`.
6. Login lần lượt 3 Board accounts:
   - `board@manga.local / Board@123`
   - `board2@manga.local / Board2@123`
   - `board3@manga.local / Board3@123`
7. Mỗi Board member vote Approve/Reject. Khi đủ 3 phiếu, hệ thống tự chốt đa số.
8. Login lại Mangaka, kiểm tra proposal chuyển sang `APPROVED` / `Approved`; hệ thống tạo/update `series` từ proposal và dùng title của proposal làm series title.
9. Mangaka vào Production, tạo chapter/page/task region và assign cho Assistant.
10. Login `assistant@manga.local / Assistant@123`, start task và submit file/note.
11. Login lại Mangaka, mở View Structure/Production để xem submission và approve (`APPROVED`) hoặc request redo (`REVISION_REQUESTED`).

Kịch bản chi tiết: xem `docs/DEMO_SCRIPT.md`.

## 4. Công nghệ sử dụng

| Phần | Công nghệ | Thư mục |
|---|---|---|
| Backend API | Java 8, Spring Boot 2.7.18, Maven Wrapper | `backend` |
| Frontend UI | React, TypeScript, Vite | `frontend` |
| Runtime database | PostgreSQL hoặc local H2 profile | `backend/src/main/resources` |
| Tài liệu nghiệp vụ | Markdown + PostgreSQL schema draft | `docs` |

## 5. Cấu trúc project

```text
SWP391_NEW/
├── README.md
├── docs/
│   ├── AI_ASSISTANT_CONTEXT.md
│   ├── API_CONTRACT.md
│   ├── DEMO_SCRIPT.md
│   ├── GIT_WORKFLOW.md
│   ├── IMPLEMENTATION_GUIDE.md
│   ├── PROJECT_RULES.md
│   ├── SESSION_HANDOFF.md
│   ├── TEAM_TASK_ASSIGNMENT.md
│   ├── TEST_CASES.md
│   ├── database/
│   │   └── <archived/optional database notes>
│   └── requirements/
│       └── MVP_SCOPE_AND_BUSINESS_RULES.md
├── backend/
│   ├── pom.xml
│   ├── mvnw.cmd
│   └── src/
│       ├── main/java/com/mangastudio/workflow/
│       │   ├── config/
│       │   ├── controllers/
│       │   ├── dtos/
│       │   ├── entities/
│       │   ├── repositories/
│       │   └── services/
│       └── test/java/com/mangastudio/workflow/
├── frontend/
│   ├── package.json
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── services/
│       ├── types/
│       └── styles.css
├── database/
│   ├── application.properties.template
│   └── schema.sql
└── ai-subsystem/
    ├── api_bridge.py
    ├── models/
    └── scripts/
```

## 6. Cách chạy project

### 6.1. Chạy backend nhanh cho local

Cách này dùng profile `local` với H2 file database, không cần cài PostgreSQL:

```bash
cd backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Backend chạy tại:

```text
http://localhost:8080
```

H2 console nếu cần kiểm tra dữ liệu local:

```text
http://localhost:8080/h2-console
```

Thông tin H2 mặc định trong profile local:

```text
JDBC URL: jdbc:h2:file:~/swp391-local-demo-db/manga_workflow
User: sa
Password: <empty>
```

### 6.2. Chạy backend với PostgreSQL

Nếu muốn chạy bằng PostgreSQL thật, tạo database `manga_workflow`, sau đó set biến môi trường trước khi start backend:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/manga_workflow'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD='<your-local-password>'
$env:DB_DDL_AUTO='update'
```

Rồi chạy:

```bash
cd backend
mvnw.cmd spring-boot:run
```

Không commit mật khẩu database vào source code hoặc README.

### 6.3. Chạy frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend chạy tại:

```text
http://localhost:5173
```

Nếu backend chạy ở URL khác, cấu hình `VITE_API_BASE_URL` cho frontend.

## 7. Local seed accounts

Các account này là seed/bootstrap data local aligned to `database/schema.sql`, dùng để test/demo.

| Role | Email | Password | Ghi chú |
|---|---|---|---|
| Admin | `admin@manga.local` | `Admin@123` | Quản lý account và skill/category |
| Mangaka | `mangaka@manga.local` | `Mangaka@123` | Tạo proposal, production, duyệt task Assistant |
| Assistant | `assistant@manga.local` | `Assistant@123` | Nhận/start/submit production task |
| Tantou Editor | `editor@manga.local` | `Editor@123` | Review proposal, request revision, forward Board |
| Editorial Board Member 1 | `board@manga.local` | `Board@123` | Vote proposal |
| Editorial Board Member 2 | `board2@manga.local` | `Board2@123` | Vote proposal |
| Editorial Board Member 3 | `board3@manga.local` | `Board3@123` | Vote proposal |

## 8. API chính

### Auth

| Method | Endpoint | Chức năng |
|---|---|---|
| POST | `/api/auth/login` | Đăng nhập |

### Admin accounts

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/admin/accounts` | Lấy danh sách tài khoản |
| POST | `/api/admin/accounts` | Tạo tài khoản |
| PUT | `/api/admin/accounts/{id}` | Cập nhật tài khoản |
| PUT | `/api/admin/accounts/{id}/status` | Bật/tắt trạng thái tài khoản |
| DELETE | `/api/admin/accounts/{id}` | Xóa tài khoản |

### Skills/categories

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/admin/skills` | Lấy danh sách skill/category |
| POST | `/api/admin/skills` | Tạo skill/category |
| PUT | `/api/admin/skills/{id}` | Cập nhật skill/category |
| PUT | `/api/admin/skills/{id}/status` | Bật/tắt trạng thái |
| DELETE | `/api/admin/skills/{id}` | Xóa skill/category |

### Mangaka proposals / manuscripts

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/mangaka/proposals?authorEmail={email}` | Lấy proposal của Mangaka |
| POST | `/api/mangaka/proposals` | Tạo draft proposal kèm manuscript metadata |
| PUT | `/api/mangaka/proposals/{id}` | Cập nhật draft/needs revision |
| PUT | `/api/mangaka/proposals/{id}/submit` | Submit proposal sang Tantou Editor review |
| DELETE | `/api/mangaka/proposals/{id}?authorEmail={email}` | Xóa proposal của Mangaka |
| POST | `/api/mangaka/proposals/upload` | Upload manuscript file |
| GET | `/api/mangaka/proposals/files/{fileName}` | Download manuscript file |

File upload hiện lưu ở máy chạy backend:

```text
<user-home>/swp391-uploads/manuscripts
```

### Mangaka production after approval

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/mangaka/proposals/{proposalId}/chapters?authorEmail={email}` | Xem chapter production của proposal đã `APPROVED` / Approved |
| POST | `/api/mangaka/proposals/{proposalId}/chapters?authorEmail={email}` | Tạo chapter |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages?authorEmail={email}` | Thêm page metadata |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions?authorEmail={email}` | Tạo region trên page |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks?authorEmail={email}` | Giao production task cho Assistant |
| PUT | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/approve?authorEmail={email}` | Mangaka approve task Assistant đã submit |
| PUT | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/redo?authorEmail={email}` | Mangaka request redo task Assistant |

### Tantou Editor review

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/editor/proposals?editorEmail={email}` | Xem proposal cần Tantou review và lịch sử review |
| PUT | `/api/editor/proposals/{id}/forward-board` | Forward proposal sang Editorial Board |
| PUT | `/api/editor/proposals/{id}/request-revision` | Yêu cầu Mangaka sửa proposal |
| PUT | `/api/editor/proposals/{id}/reject` | Backend vẫn còn endpoint reject, nhưng UI demo hiện không dùng nút này |

### Editorial Board voting

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/board/proposals?memberEmail={email}` | Xem proposal đang chờ Board vote/quyết định |
| PUT | `/api/board/proposals/{id}/approve` | Gửi phiếu Approve của Board member hiện tại |
| PUT | `/api/board/proposals/{id}/reject` | Gửi phiếu Reject của Board member hiện tại |

Ghi chú:

- Board member chỉ vote một lần cho mỗi proposal.
- Khi đủ 3 phiếu, backend tự tính đa số.
- Đa số Approve chuyển proposal sang `Approved`; ngược lại chuyển sang `Rejected`.

### Assistant tasks

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/assistant/tasks?assistantEmail={email}` | Assistant xem task được giao kèm chapter/page/region context |
| PUT | `/api/assistant/tasks/{taskId}/start` | Assistant nhận/xử lý task đang `ASSIGNED` / Pending hoặc `REVISION_REQUESTED` / RedoRequested |
| PUT | `/api/assistant/tasks/{taskId}/submit` | Submit file/note và chuyển task sang `PENDING_REVIEW` / Submitted |

## 9. Cách đọc code theo từng phần

Nên đọc theo thứ tự sau:

1. `docs/IMPLEMENTATION_GUIDE.md` để hiểu luồng xử lý và phân chia module.
2. `backend/src/main/java/com/mangastudio/workflow/controllers` để xem API nhận request.
3. `backend/src/main/java/com/mangastudio/workflow/services` để xem logic xử lý workflow.
4. `backend/src/main/java/com/mangastudio/workflow/entities` và `repositories` để xem persistence.
5. `backend/src/main/java/com/mangastudio/workflow/dtos` để xem DTO/request/response.
6. `frontend/src/services` để xem frontend gọi API.
7. `frontend/src/components` và `frontend/src/pages` để xem UI.
8. `backend/src/test` để xem test backend.

## 10. Kiểm tra build/test

### Backend test

```bash
cd backend
mvnw.cmd test
```

### Backend package

```bash
cd backend
mvnw.cmd package
```

### Frontend build

```bash
cd frontend
npm run build
```

## 11. Persistence và seed/local data

Backend hiện có 2 cách chạy chính:

1. **Local demo profile (`local`)**: dùng H2 file database, dễ chạy trên máy demo, không cần PostgreSQL.
2. **Default profile**: dùng PostgreSQL thông qua `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DDL_AUTO`.

Các nhóm dữ liệu chính đã được lưu qua repository/database ở runtime:

- Account/login seed local.
- Skill/category và account skill assignment.
- Mangaka proposal + manuscript metadata.
- Tantou Editor review metadata.
- Editorial Board vote/decision metadata.
- Chapter/page/task region production workflow.
- Assistant task submission metadata.

Lưu ý local/dev:

- Password seed local đang lưu dạng plain text để phục vụ test/demo, chưa phải production security.
- Chưa có JWT/session backend production đầy đủ.
- Phân quyền backend hiện dựa trên email/role local, chưa có token guard production.
- File upload/download đã có lưu file local, nhưng chưa có storage service/cloud storage.
- Chưa có annotation trực tiếp trên ảnh/PDF.
- Chưa có reader poll/ranking UI hoàn chỉnh.

## 12. Tài liệu liên quan

- `docs/AI_ASSISTANT_CONTEXT.md` - context/rule cho lần làm sau.
- `docs/TEAM_TASK_ASSIGNMENT.md` - chia nhiệm vụ nhóm 5 người.
- `docs/PROJECT_RULES.md` - rule code/merge/build/test.
- `docs/API_CONTRACT.md` - hợp đồng API giữa backend/frontend/database.
- `docs/GIT_WORKFLOW.md` - cách chia branch và merge.
- `docs/DEMO_SCRIPT.md` - kịch bản demo cho giáo viên.
- `docs/TEST_CASES.md` - test case/smoke test chính.
- `docs/V1_CHANGE_REQUEST_AI_SUMMARY.md` - yêu cầu V1 mới cho file validation + AI summary.
- `docs/IMPLEMENTATION_GUIDE.md`
- `docs/SESSION_HANDOFF.md`
- `docs/requirements/MVP_SCOPE_AND_BUSINESS_RULES.md`
- `database/schema.sql` - canonical database schema for the current project

## 13. Ghi chú cho giáo viên

Project hiện chia rõ thành các lớp:

1. Frontend React hiển thị dashboard theo role và gọi API.
2. Backend controller nhận request/trả response.
3. Backend service xử lý business flow demo.
4. Repository/entity lưu dữ liệu runtime bằng H2 local hoặc PostgreSQL.

Mục tiêu bản này là chứng minh luồng workflow end-to-end trước: từ proposal của Mangaka, Tantou review, Board vote, production task handoff, Assistant submit, đến Mangaka duyệt task. Các phần production security, cloud storage, annotation chuyên sâu, ranking/poll đầy đủ được để ở future work.
