# Implementation Guide - SWP391 Manga Workflow Project

Tài liệu này giải thích cách chia code hiện tại để giáo viên hoặc thành viên nhóm đọc project dễ hơn.

## 1. Tổng quan luồng chạy

```text
Người dùng nhập email/password
        ↓
Frontend LoginForm gọi authService.login()
        ↓
POST /api/auth/login
        ↓
AuthController nhận request
        ↓
InMemoryAuthService kiểm tra tài khoản qua InMemoryAccountService
        ↓
Backend trả LoginResponse gồm accessToken, user, dashboardPath
        ↓
Frontend lưu session vào localStorage
        ↓
Nếu user là Admin thì mở AdminAccountDashboard
Nếu user là Mangaka thì mở MangakaDashboard
Nếu user là TantouEditor thì mở TantouEditorDashboard
Nếu user là EditorialBoardMember thì mở EditorialBoardDashboard
Nếu user là Assistant thì mở AssistantDashboard
```

## 2. Frontend

Frontend nằm trong:

```text
src/frontend
```

### 2.1. Entry point

| File | Vai trò |
|---|---|
| `src/frontend/src/main.tsx` | Render React app vào HTML |
| `src/frontend/src/App.tsx` | Component app chính |
| `src/frontend/src/pages/LoginPage.tsx` | Quyết định hiển thị login hay admin board sau khi đăng nhập |

### 2.2. Login UI

| File | Vai trò |
|---|---|
| `src/frontend/src/components/LoginForm.tsx` | Form nhập email/password, remember me, forgot password, trạng thái loading/error |
| `src/frontend/src/services/authService.ts` | Gọi API đăng nhập, lưu/xóa session trong `localStorage` |
| `src/frontend/src/types/auth.ts` | Kiểu dữ liệu login request/response/user role |

Luồng login chính:

1. Người dùng nhập email/password.
2. `LoginForm` gọi hàm `login(payload)` trong `authService.ts`.
3. Nếu thành công, response được lưu vào `localStorage` với key `mangaWorkflow.auth`.
4. `LoginPage` nhận session và điều hướng theo role:
   - `Admin` → `AdminAccountDashboard`.
   - `Mangaka` → `MangakaDashboard`.
   - `TantouEditor` → `TantouEditorDashboard`.
   - `EditorialBoardMember` → `EditorialBoardDashboard`.
   - `Assistant` → `AssistantDashboard`.
   - Role khác → safe signed-in workspace fallback, không hiển thị thông báo dang dở.

### 2.3. Admin dashboard UI

| File | Vai trò |
|---|---|
| `src/frontend/src/components/AdminAccountDashboard.tsx` | Màn hình board quản trị, gồm account management và skill/category management |
| `src/frontend/src/services/adminAccountService.ts` | Gọi API CRUD tài khoản |
| `src/frontend/src/services/skillService.ts` | Gọi API CRUD skill/category |
| `src/frontend/src/types/admin.ts` | Kiểu dữ liệu account, status, role, skill/category |
| `src/frontend/src/styles.css` | Toàn bộ CSS cho login và dashboard |

Dashboard được chia thành:

- Left rail/sidebar: tên project, link nhanh, thống kê nhanh.
- Header: thông tin người đăng nhập và nút logout.
- Accounts board: form tạo/sửa account, danh sách account, và gán skill/category cho từng account bằng checkbox nhỏ.
- Skills board: form tạo/sửa skill/category và danh sách skill/category.

### 2.4. Mangaka proposal + production dashboard UI

| File | Vai trò |
|---|---|
| `src/frontend/src/components/MangakaDashboard.tsx` | Mangaka workspace: proposal/manuscript + post-approval chapter/page/region/task production |
| `src/frontend/src/services/mangakaProposalService.ts` | Gọi API proposal/manuscript của Mangaka |
| `src/frontend/src/services/mangakaProductionService.ts` | Gọi API chapter/page/region/task sau khi proposal approved/serializing |
| `src/frontend/src/types/mangaka.ts` | Kiểu dữ liệu proposal/status/request và production DTO |

Mangaka dashboard được chia thành:

- Header: thông tin Mangaka và nút logout.
- Flow banner: Series proposal + manuscript draft → Submit Tantou Editor → Wait approval/revision → Production delegation after approval.
- Proposal form: tạo mới hoặc chỉnh sửa proposal ở trạng thái `Draft` / `NeedsRevision`, kèm `manuscriptFileName` metadata.
- Proposal board: hiển thị cards theo trạng thái, manuscript version/upload time, action `Edit` / `Submit to editor`.
- Production board: chỉ mở cho proposal `Approved` hoặc `Serializing`; cho phép tạo chapter, page metadata, region, và production task cho Assistant.

Luồng Mangaka demo:

```text
Mangaka login
    ↓
Create/update proposal + manuscript metadata
    ↓
Submit proposal to Tantou Editor
    ↓
If Approved/Serializing seed/demo state exists
    ↓
Create chapter
    ↓
Add page metadata
    ↓
Create page region
    ↓
Assign production task to Assistant
```

### 2.5. Tantou Editor and Editorial Board dashboard UI

| File | Vai trò |
|---|---|
| `src/frontend/src/components/TantouEditorDashboard.tsx` | Tantou Editor workspace: review proposal, forward Board, request revision, reject |
| `src/frontend/src/components/EditorialBoardDashboard.tsx` | Editorial Board workspace: approve/reject proposal đã được Editor forward |
| `src/frontend/src/services/tantouEditorProposalService.ts` | Gọi API Editor proposal review |
| `src/frontend/src/services/editorialBoardProposalService.ts` | Gọi API Board proposal decision |

Luồng review demo:

```text
Mangaka submits proposal
    ↓
Tantou Editor reviews proposal
    ↓
Forward to Editorial Board
    ↓
Editorial Board approves proposal
    ↓
Proposal becomes Approved and production opens for Mangaka
```

### 2.6. Assistant task dashboard UI

| File | Vai trò |
|---|---|
| `src/frontend/src/components/AssistantDashboard.tsx` | Assistant workspace: xem task được giao, start task, submit work |
| `src/frontend/src/services/assistantTaskService.ts` | Gọi API Assistant task list/start/submit |
| `src/frontend/src/types/assistant.ts` | Kiểu dữ liệu task và request submit/start |

Luồng Assistant demo:

```text
Assistant login
    ↓
Xem task board theo Pending / In Progress / Submitted
    ↓
Chọn task để xem proposal/chapter/page/region context
    ↓
Start task
    ↓
Nhập submitted file name + submission note
    ↓
Submit work
```

## 3. Backend

Backend nằm trong:

```text
src/backend
```

### 3.1. Entry point và config

| File | Vai trò |
|---|---|
| `MangaWorkflowApiApplication.java` | Entry point Spring Boot |
| `application.properties` | Cấu hình port `8080` và CORS |
| `config/CorsProperties.java` | Đọc cấu hình CORS |
| `config/WebConfig.java` | Cho phép frontend `localhost:5173` gọi backend |

### 3.2. Auth module

| File | Vai trò |
|---|---|
| `controller/AuthController.java` | API `POST /api/auth/login` |
| `service/AuthService.java` | Interface đăng nhập |
| `service/InMemoryAuthService.java` | Logic đăng nhập demo |
| `model/LoginRequest.java` | Request gồm email/password |
| `model/LoginResponse.java` | Response gồm accessToken, user, dashboardPath |
| `model/AuthenticatedUser.java` | Thông tin user trả về frontend |

Luồng xử lý:

1. `AuthController.login()` nhận `LoginRequest`.
2. Controller gọi `authService.login(request)`.
3. `InMemoryAuthService` kiểm tra tài khoản qua `InMemoryAccountService.authenticate()`.
4. Nếu đúng, tạo token demo và trả user + đường dẫn dashboard.
5. Nếu sai, trả HTTP `401` với message tiếng Việt.

### 3.3. Account management module

| File | Vai trò |
|---|---|
| `controller/AdminAccountController.java` | API CRUD tài khoản admin |
| `service/InMemoryAccountService.java` | Xử lý tài khoản; runtime dùng PostgreSQL repository, test dùng in-memory fallback |
| `model/AccountDto.java` | Dữ liệu account trả về frontend |
| `model/AccountCreateRequest.java` | Dữ liệu tạo account |
| `model/AccountUpdateRequest.java` | Dữ liệu cập nhật account |
| `model/AccountStatus.java` | Trạng thái `Active` / `Inactive` |
| `model/UserRole.java` | Role cố định của hệ thống |

Endpoint:

| Method | Endpoint | Controller method |
|---|---|---|
| GET | `/api/admin/accounts` | `list()` |
| POST | `/api/admin/accounts` | `create()` |
| PUT | `/api/admin/accounts/{id}` | `update()` |
| PUT | `/api/admin/accounts/{id}/status` | `status()` |
| DELETE | `/api/admin/accounts/{id}` | `delete()` |

Dữ liệu seed ban đầu:

| Role | Email | Password |
|---|---|---|
| Admin | `admin@manga.local` | `Admin@123` |
| Mangaka | `mangaka@manga.local` | `Mangaka@123` |
| Assistant | `assistant@manga.local` | `Assistant@123` |
| TantouEditor | `editor@manga.local` | `Editor@123` |
| EditorialBoardMember | `board@manga.local` | `Board@123` |

### 3.4. Skill/category management module

| File | Vai trò |
|---|---|
| `controller/AdminSkillController.java` | API CRUD skill/category |
| `service/InMemorySkillCategoryService.java` | Xử lý skill/category; runtime dùng PostgreSQL repository, test dùng in-memory fallback |
| `model/SkillCategoryDto.java` | Dữ liệu skill/category trả về frontend |
| `model/SkillCategoryCreateRequest.java` | Request tạo skill/category |
| `model/SkillCategoryUpdateRequest.java` | Request cập nhật skill/category |
| `model/SkillStatusRequest.java` | Request bật/tắt trạng thái active |

Endpoint:

| Method | Endpoint | Controller method |
|---|---|---|
| GET | `/api/admin/skills` | `list()` |
| POST | `/api/admin/skills` | `create()` |
| PUT | `/api/admin/skills/{id}` | `update()` |
| PUT | `/api/admin/skills/{id}/status` | `status()` |
| DELETE | `/api/admin/skills/{id}` | `delete()` |

### 3.5. Mangaka proposal/manuscript + production module

| File | Vai trò |
|---|---|
| `controller/MangakaProposalController.java` | API proposal/manuscript dành cho Mangaka |
| `controller/MangakaProductionController.java` | API chapter/page/region/task production cho proposal đã Approved/Serializing |
| `service/InMemoryMangaProposalService.java` | Xử lý proposal/manuscript/review; runtime lưu PostgreSQL, test dùng in-memory fallback |
| `service/InMemoryMangakaProductionService.java` | Xử lý chapter/page/region/task; runtime lưu PostgreSQL, test dùng in-memory fallback |
| `model/MangaProposalDto.java` | Dữ liệu proposal trả về frontend, gồm manuscript metadata |
| `model/MangaProposalCreateRequest.java` | Request tạo draft proposal |
| `model/MangaProposalUpdateRequest.java` | Request cập nhật proposal/resubmit manuscript metadata |
| `model/MangaProposalSubmitRequest.java` | Request submit proposal |
| `model/MangaProposalStatus.java` | Trạng thái `Draft`, `SubmittedToEditor`, `NeedsRevision`, `Approved`, `Serializing`, `Rejected` |
| `model/MangakaChapterDto.java` / `MangakaPageDto.java` / `MangakaPageRegionDto.java` / `MangakaProductionTaskDto.java` | DTO production nested data |

Endpoint:

| Method | Endpoint | Controller method |
|---|---|---|
| GET | `/api/mangaka/proposals?authorEmail={email}` | `list()` |
| POST | `/api/mangaka/proposals` | `create()` |
| PUT | `/api/mangaka/proposals/{id}` | `update()` |
| PUT | `/api/mangaka/proposals/{id}/submit` | `submit()` |
| GET | `/api/mangaka/proposals/{proposalId}/chapters?authorEmail={email}` | `listChapters()` |
| POST | `/api/mangaka/proposals/{proposalId}/chapters?authorEmail={email}` | `createChapter()` |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages?authorEmail={email}` | `addPage()` |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions?authorEmail={email}` | `addRegion()` |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks?authorEmail={email}` | `assignTask()` |

Business rule demo:

- Mangaka chỉ xem/sửa/submit proposal thuộc email của mình.
- Submit Editor yêu cầu có manuscript metadata (`manuscriptFileName`).
- Chỉ proposal `Draft` hoặc `NeedsRevision` mới được sửa/submit.
- Production board chỉ mở cho proposal `Approved` hoặc `Serializing`.
- Chapter/page/region/task đều kiểm tra ownership theo `authorEmail`.
- Region bounds phải nằm trong 0..100% và không vượt biên page.

### 3.6. Tantou Editor and Editorial Board review module

| File | Vai trò |
|---|---|
| `controller/TantouEditorProposalController.java` | API Editor xem/forward/request revision/reject proposal |
| `controller/EditorialBoardProposalController.java` | API Board xem/approve/reject proposal |
| `model/EditorProposalReviewRequest.java` | Request Editor gồm email và note |
| `model/BoardProposalDecisionRequest.java` | Request Board gồm email và note |
| `service/InMemoryMangaProposalService.java` | Lưu trạng thái review và metadata Editor/Board |

Endpoint Editor:

| Method | Endpoint | Controller method |
|---|---|---|
| GET | `/api/tantou-editor/proposals?editorEmail={email}` | `list()` |
| PUT | `/api/tantou-editor/proposals/{id}/forward-board` | `forwardToBoard()` |
| PUT | `/api/tantou-editor/proposals/{id}/request-revision` | `requestRevision()` |
| PUT | `/api/tantou-editor/proposals/{id}/reject` | `reject()` |

Endpoint Board:

| Method | Endpoint | Controller method |
|---|---|---|
| GET | `/api/editorial-board/proposals?memberEmail={email}` | `list()` |
| PUT | `/api/editorial-board/proposals/{id}/approve` | `approve()` |
| PUT | `/api/editorial-board/proposals/{id}/reject` | `reject()` |

Business rule demo:

- Editor xử lý proposal ở trạng thái `SubmittedToEditor`.
- Forward Board chuyển proposal sang `UnderBoardReview`.
- Editor có thể chuyển proposal sang `NeedsRevision` hoặc `Rejected`.
- Board chỉ approve/reject proposal ở trạng thái `UnderBoardReview`.
- Board approve chuyển proposal sang `Approved`, từ đó Mangaka mở được production.

### 3.7. Assistant task module

| File | Vai trò |
|---|---|
| `controller/AssistantTaskController.java` | API Assistant xem/start/submit task production |
| `service/InMemoryMangakaProductionService.java` | Lưu task production và xử lý transition Assistant |
| `model/AssistantTaskDto.java` | DTO task kèm proposal/chapter/page/region context và submission metadata |
| `model/AssistantTaskActionRequest.java` | Request start task gồm `assistantEmail` |
| `model/AssistantTaskSubmitRequest.java` | Request submit task gồm `assistantEmail`, `submittedFileName`, `submissionNote` |

Endpoint:

| Method | Endpoint | Controller method |
|---|---|---|
| GET | `/api/assistant/tasks?assistantEmail={email}` | `list()` |
| PUT | `/api/assistant/tasks/{taskId}/start` | `start()` |
| PUT | `/api/assistant/tasks/{taskId}/submit` | `submit()` |

Business rule demo:

- Assistant chỉ xem/start/submit task có `assistantEmail` trùng tài khoản của mình.
- Task `Pending` hoặc `RedoRequested` có thể chuyển sang `InProgress`.
- Task `Pending`, `InProgress`, hoặc `RedoRequested` có thể submit work.
- Submit yêu cầu `submittedFileName`; `submissionNote` là metadata mô tả phần đã hoàn thành.
- Backend seed sẵn task `603` cho `assistant@manga.local` để demo ngay sau khi restart.

## 4. Test backend

Test nằm trong:

```text
src/backend/src/test/java/com/mangaworkflow/api
```

| Test file | Kiểm tra |
|---|---|
| `AuthControllerTest.java` | API login thành công/thất bại |
| `InMemoryAuthServiceTest.java` | Logic đăng nhập |
| `AdminAccountControllerTest.java` | API account CRUD/status |
| `InMemoryAccountServiceTest.java` | Logic account service |
| `AdminSkillControllerTest.java` | API skill CRUD/status |
| `InMemorySkillCategoryServiceTest.java` | Logic skill service |
| `MangakaProposalControllerTest.java` | API tạo/submit proposal và smoke access production endpoint |
| `TantouEditorProposalControllerTest.java` | API Editor forward proposal sang Board |
| `EditorialBoardProposalControllerTest.java` | API Board approve proposal |
| `AssistantTaskControllerTest.java` | API Assistant list/start/submit task |
| `InMemoryMangaProposalServiceTest.java` | Logic proposal list/create/update/submit, manuscript requirement, review transition gate |

Chạy test:

```bash
cd src/backend
mvnw.cmd test
```

## 5. PostgreSQL persistence mode

Backend hiện hỗ trợ 2 chế độ:

1. **Runtime Spring Boot**: service được Spring inject repository và lưu dữ liệu vào PostgreSQL.
2. **Unit/controller test**: service no-arg constructor vẫn chạy in-memory để test nhanh, không cần database.

Cấu hình PostgreSQL nằm trong `src/backend/src/main/resources/application.properties` và đọc qua biến môi trường:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/manga_workflow'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD='<your-local-password>'
$env:DB_DDL_AUTO='update'
```

Các repository/entity nằm dưới:

```text
src/backend/src/main/java/com/mangaworkflow/persistence
```

Database schema chuẩn hiện tại:

```text
schema (1).sql
schema (1).sql
```

## 6. Phân biệt phần đã làm và phần định hướng

### Đã làm chạy được

- Auth/login demo.
- Admin dashboard frontend.
- Mangaka proposal/manuscript dashboard frontend.
- Mangaka production board frontend for approved/serializing proposals.
- Tantou Editor review dashboard frontend.
- Editorial Board approval dashboard frontend.
- Assistant task dashboard frontend.
- Account CRUD demo.
- Skill/category CRUD demo.
- Mangaka proposal create/update/submit demo with manuscript metadata.
- Mangaka chapter/page/region/task creation demo after approval.
- PostgreSQL persistence for users, roles, skills, series/proposal workflow, board votes, chapters, pages, tasks, and submissions.
- Backend tests.
- Frontend build.

### Chưa làm full production

- JWT/security production.
- Phân quyền backend theo token.
- Full annotation trực tiếp trên file/ảnh.
- Full Editorial Board voting nhiều thành viên.
- Assistant review/redo loop nâng cao sau khi submit task.
- Reader poll/ranking workflow.
- Payment/earnings thật.

Các phần chưa làm đã được ghi trong tài liệu MVP để triển khai sau theo thứ tự ưu tiên.

## 7. Quy trình code đề xuất để tiếp tục

1. Chuẩn hóa database schema/entity/repository theo PostgreSQL production.
2. Thêm migration tool như Flyway/Liquibase.
3. Thêm bảo mật token/JWT.
4. Nâng cấp workflow proposal review thành annotation/voting thật.
5. Nâng cấp workflow task/skill assignment bằng database và review loop.
6. Hoàn thiện dashboard theo từng role.
7. Viết thêm test cho từng module.

## 8. Gợi ý thuyết trình ngắn

Khi trình bày với giáo viên, có thể nói:

> Nhóm em chia project thành backend Spring Boot và frontend React. Phiên bản hiện tại demo được một vòng workflow chính: Mangaka tạo và submit proposal, Tantou Editor review và forward, Editorial Board approve, Mangaka mở production để tạo chapter/page/region/task, Assistant nhận task và submit work. Backend hiện đã kết nối PostgreSQL để lưu dữ liệu chính theo schema hiện tại như users, roles, skills, series, board votes, chapters, pages, tasks và submissions. Unit test vẫn giữ fallback in-memory để kiểm thử nhanh.
