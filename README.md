# SWP391 Manga Project Lam Lai

Demo project cho hệ thống **Manga Creation Workflow and Publishing Management System**. Bản này được tạo lại tại `SWP391_NEW` để nhóm 5 người có thể chia module, học code theo phần và merge dễ hơn. Baseline hiện tại vẫn giữ luồng trình diễn end-to-end cho SWP391: đăng nhập theo vai trò cố định, quản trị tài khoản/kỹ năng, Mangaka gửi proposal có manuscript upload, Tantou Editor review, Editorial Board bỏ phiếu 3 thành viên, mở production, giao việc Assistant, Assistant submit work và Mangaka duyệt kết quả.

Repository đang dùng để demo/develop:

```text
https://github.com/lexuanduyanh-svg/SWP391_MANGA_PROJECT_LAMLAI
```

## 1. Trạng thái hiện tại

Bản hiện tại đã có thể chạy demo các màn hình chính:

- Login theo role mẫu và lưu phiên đăng nhập ở frontend bằng `localStorage`.
- Admin dashboard: quản lý account demo, skill/category và gán skill/category cho account.
- Mangaka dashboard:
  - Tạo/sửa/xóa proposal.
  - Validate file đầu vào trước upload.
  - Xem AI/content summary preview của file đã chọn trước khi submit.
  - Upload manuscript file thật qua backend.
  - `Save & Submit to Tantou` để lưu và gửi proposal trong một bước.
  - Theo dõi trạng thái review bằng modal chi tiết.
  - Sau khi Board approve, tạo chapter/page/region/task để giao việc sản xuất.
  - Review task Assistant đã submit: approve hoặc request redo.
- Tantou Editor dashboard:
  - Xem proposal mới nhất trước.
  - Download manuscript.
  - Forward proposal sang Editorial Board.
  - Request revision cho Mangaka.
  - UI hiện tại không dùng luồng reject ở vòng Tantou để demo gọn hơn.
- Editorial Board dashboard:
  - Có 3 tài khoản Board demo.
  - Mỗi Board member được vote một lần: Approve hoặc Reject.
  - Khi đủ 3 phiếu, backend tự quyết định theo đa số.
  - Approve đa số chuyển proposal sang `Approved`, mở production cho Mangaka.
- Assistant dashboard:
  - Xem task được giao kèm chapter/page/region context.
  - Start task.
  - Submit file/note hoàn thành.
  - Xem lịch sử task và estimated demo earnings.
- Backend Java Spring Boot, có persistence qua database khi cấu hình profile/database.
- Frontend React + Vite + TypeScript.

## 2. Demo flow chính để trình bày

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
8. Login lại Mangaka, kiểm tra proposal chuyển sang `Approved` nếu đa số approve.
9. Mangaka vào Production, tạo chapter/page/region/task và assign cho Assistant.
10. Login `assistant@manga.local / Assistant@123`, start task và submit file/note.
11. Login lại Mangaka, mở View Structure/Production để xem submission và approve hoặc request redo.

## 3. Công nghệ sử dụng

| Phần | Công nghệ | Thư mục |
|---|---|---|
| Backend API | Java 8, Spring Boot 2.7.18, Maven Wrapper | `backend` |
| Frontend UI | React, TypeScript, Vite | `frontend` |
| Runtime database | PostgreSQL hoặc local H2 profile | `backend/src/main/resources` |
| Tài liệu nghiệp vụ | Markdown + PostgreSQL schema draft | `docs` |

## 4. Cấu trúc project

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

## 5. Cách chạy project

### 5.1. Chạy backend nhanh cho demo local

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

### 5.2. Chạy backend với PostgreSQL

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

### 5.3. Chạy frontend

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

## 6. Tài khoản demo

| Role | Email | Password | Ghi chú |
|---|---|---|---|
| Admin | `admin@manga.local` | `Admin@123` | Quản lý account và skill/category |
| Mangaka | `mangaka@manga.local` | `Mangaka@123` | Tạo proposal, production, duyệt task Assistant |
| Assistant | `assistant@manga.local` | `Assistant@123` | Nhận/start/submit production task |
| Tantou Editor | `editor@manga.local` | `Editor@123` | Review proposal, request revision, forward Board |
| Editorial Board Member 1 | `board@manga.local` | `Board@123` | Vote proposal |
| Editorial Board Member 2 | `board2@manga.local` | `Board2@123` | Vote proposal |
| Editorial Board Member 3 | `board3@manga.local` | `Board3@123` | Vote proposal |

## 7. API chính

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
| GET | `/api/mangaka/proposals/{proposalId}/chapters?authorEmail={email}` | Xem chapter production của proposal đã Approved/Serializing |
| POST | `/api/mangaka/proposals/{proposalId}/chapters?authorEmail={email}` | Tạo chapter |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages?authorEmail={email}` | Thêm page metadata |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions?authorEmail={email}` | Tạo region trên page |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks?authorEmail={email}` | Giao production task cho Assistant |
| PUT | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/approve?authorEmail={email}` | Mangaka approve task Assistant đã submit |
| PUT | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/redo?authorEmail={email}` | Mangaka request redo task Assistant |

### Tantou Editor review

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/tantou-editor/proposals?editorEmail={email}` | Xem proposal cần Tantou review và lịch sử review |
| PUT | `/api/tantou-editor/proposals/{id}/forward-board` | Forward proposal sang Editorial Board |
| PUT | `/api/tantou-editor/proposals/{id}/request-revision` | Yêu cầu Mangaka sửa proposal |
| PUT | `/api/tantou-editor/proposals/{id}/reject` | Backend vẫn còn endpoint reject, nhưng UI demo hiện không dùng nút này |

### Editorial Board voting

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/editorial-board/proposals?memberEmail={email}` | Xem proposal đang chờ Board vote/quyết định |
| PUT | `/api/editorial-board/proposals/{id}/approve` | Gửi phiếu Approve của Board member hiện tại |
| PUT | `/api/editorial-board/proposals/{id}/reject` | Gửi phiếu Reject của Board member hiện tại |

Ghi chú:

- Board member chỉ vote một lần cho mỗi proposal.
- Khi đủ 3 phiếu, backend tự tính đa số.
- Đa số Approve chuyển proposal sang `Approved`; ngược lại chuyển sang `Rejected`.

### Assistant tasks

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/assistant/tasks?assistantEmail={email}` | Assistant xem task được giao kèm chapter/page/region context |
| PUT | `/api/assistant/tasks/{taskId}/start` | Chuyển task từ `Pending`/`RedoRequested` sang `InProgress` |
| PUT | `/api/assistant/tasks/{taskId}/submit` | Submit file/note hoàn thành và chuyển task sang `Submitted` |

## 8. Cách đọc code theo từng phần

Nên đọc theo thứ tự sau:

1. `docs/IMPLEMENTATION_GUIDE.md` để hiểu luồng xử lý và phân chia module.
2. `backend/src/main/java/com/mangastudio/workflow/controllers` để xem API nhận request.
3. `backend/src/main/java/com/mangastudio/workflow/services` để xem logic xử lý workflow.
4. `backend/src/main/java/com/mangastudio/workflow/entities` và `repositories` để xem persistence.
5. `backend/src/main/java/com/mangastudio/workflow/dtos` để xem DTO/request/response.
6. `frontend/src/services` để xem frontend gọi API.
7. `frontend/src/components` và `frontend/src/pages` để xem UI.
8. `backend/src/test` để xem test backend.

## 9. Kiểm tra build/test

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

## 10. Persistence và dữ liệu demo

Backend hiện có 2 cách chạy chính:

1. **Local demo profile (`local`)**: dùng H2 file database, dễ chạy trên máy demo, không cần PostgreSQL.
2. **Default profile**: dùng PostgreSQL thông qua `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DDL_AUTO`.

Các nhóm dữ liệu chính đã được lưu qua repository/database ở runtime:

- Account/login demo.
- Skill/category và account skill assignment.
- Mangaka proposal + manuscript metadata.
- Tantou Editor review metadata.
- Editorial Board vote/decision metadata.
- Chapter/page/region/task production workflow.
- Assistant task submission metadata.

Lưu ý demo:

- Password demo đang lưu dạng plain text để phục vụ bài trình bày, chưa phải production security.
- Chưa có JWT/session backend production đầy đủ.
- Phân quyền backend hiện dựa trên email/role demo, chưa có token guard production.
- File upload/download đã có lưu file local, nhưng chưa có storage service/cloud storage.
- Chưa có annotation trực tiếp trên ảnh/PDF.
- Chưa có reader poll/ranking UI hoàn chỉnh.

## 11. Tài liệu liên quan

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
- `schema (1).sql` - canonical database schema for the current project
- `schema (1).sql` - synced copy of the canonical schema

## 12. Ghi chú cho giáo viên

Project hiện chia rõ thành các lớp:

1. Frontend React hiển thị dashboard theo role và gọi API.
2. Backend controller nhận request/trả response.
3. Backend service xử lý business flow demo.
4. Repository/entity lưu dữ liệu runtime bằng H2 local hoặc PostgreSQL.

Mục tiêu bản này là chứng minh luồng workflow end-to-end trước: từ proposal của Mangaka, Tantou review, Board vote, production task handoff, Assistant submit, đến Mangaka duyệt task. Các phần production security, cloud storage, annotation chuyên sâu, ranking/poll đầy đủ được để ở future work.
