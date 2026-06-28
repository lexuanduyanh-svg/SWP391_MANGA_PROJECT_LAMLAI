# SWP391 Manga API — Danh sách API cho thành viên

> Tài liệu này liệt kê **toàn bộ API thật đang chạy** theo đúng code backend (`com.mangastudio.workflow.controllers`).
> Dùng bản này để gọi API, KHÔNG truy cập thẳng link gốc.

## 0. Thông tin chung

| Mục | Giá trị |
|---|---|
| **Base URL (deploy)** | `https://swp391-manga-api.onrender.com` |
| **Base URL (local)** | `http://localhost:8080` |
| Server port | `8080` |
| Context path | (không có) |
| CORS cho phép | `http://localhost:5173` (đổi qua env `CORS_ORIGINS`), áp dụng cho `/api/**` |

> ⚠️ Truy cập thẳng `https://swp391-manga-api.onrender.com/` sẽ ra **Whitelabel Error Page 404**.
> Đây là **bình thường** — server vẫn chạy, chỉ là không có trang ở path gốc `/`.
> Phải gọi đúng endpoint bên dưới mới có dữ liệu.

### Cách test nhanh
```bash
# Đăng nhập (POST)
curl -X POST https://swp391-manga-api.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@manga.local","password":"Admin@123"}'

# Lấy danh sách tài khoản (GET) — mở thẳng trên trình duyệt được
https://swp391-manga-api.onrender.com/admin/accounts
```

### Tài khoản demo
```text
admin@manga.local     / Admin@123
mangaka@manga.local   / Mangaka@123
assistant@manga.local / Assistant@123
editor@manga.local    / Editor@123
board@manga.local     / Board@123
```

---

## 1. 🔐 Auth — `AuthController`

| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/auth/login` | Đăng nhập, trả về thông tin user/role |

---

## 2. 👤 Admin — `AdminController` (lưu ý: prefix là `/admin`, KHÔNG có `/api`)

### Quản lý tài khoản
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/admin/accounts` | Lấy danh sách tài khoản |
| POST | `/admin/accounts` | Tạo tài khoản mới |
| PUT | `/admin/accounts/{id}` | Cập nhật tài khoản |
| PUT | `/admin/accounts/{id}/status` | Đổi trạng thái (ACTIVE / INACTIVE) |
| PUT | `/admin/accounts/{id}/skills` | Cập nhật kỹ năng cho tài khoản |
| DELETE | `/admin/accounts/{id}` | Xóa tài khoản |

### Quản lý kỹ năng (skills)
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/admin/skills` | Lấy danh sách kỹ năng |
| POST | `/admin/skills` | Tạo kỹ năng mới |
| PUT | `/admin/skills/{id}` | Cập nhật kỹ năng |
| PUT | `/admin/skills/{id}/status` | Đổi trạng thái kỹ năng |
| DELETE | `/admin/skills/{id}` | Xóa kỹ năng |

---

## 3. 🖊️ Mangaka — Proposal — `MangakaController` (`/api/mangaka/proposals`)

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/mangaka/proposals?authorEmail={email}` | Danh sách proposal của mangaka |
| POST | `/api/mangaka/proposals` | Tạo proposal mới |
| PUT | `/api/mangaka/proposals/{id}` | Cập nhật proposal |
| PUT | `/api/mangaka/proposals/{id}/submit` | Nộp proposal cho Editor |
| DELETE | `/api/mangaka/proposals/{id}?authorEmail={email}` | Xóa proposal |
| POST | `/api/mangaka/proposals/preview-upload` | Upload + validate + AI summary bản thảo |
| POST | `/api/mangaka/proposals/upload` | Upload file proposal |
| GET | `/api/mangaka/proposals/files/{fileName}` | Tải/xem file đã upload |

---

## 4. 📖 Mangaka — Sản xuất (Chapter / Page / Task) — `MangakaProductionController`

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/mangaka/proposals/{proposalId}/chapters` | Danh sách chapter |
| POST | `/api/mangaka/proposals/{proposalId}/chapters` | Tạo chapter mới |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages` | Tạo page trong chapter |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/tasks` | Giao task cho page |
| PUT | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/tasks/{taskId}/approve` | Duyệt task |
| PUT | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/tasks/{taskId}/redo` | Yêu cầu làm lại task |

---

## 5. ✏️ Editor (Tantou) — `EditorController` (`/api/editor/proposals`)

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/editor/proposals?editorEmail={email}` | Danh sách proposal cần duyệt |
| PUT | `/api/editor/proposals/{id}/forward-board` | Chuyển lên Ban giám đốc |
| PUT | `/api/editor/proposals/{id}/request-revision` | Yêu cầu mangaka chỉnh sửa |
| PUT | `/api/editor/proposals/{id}/reject` | Từ chối proposal |

---

## 6. 🏛️ Board (Ban giám đốc) — `BoardController` (`/api/board/proposals`)

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/board/proposals?memberEmail={email}` | Danh sách proposal chờ duyệt |
| PUT | `/api/board/proposals/{id}/approve` | Phê duyệt proposal |
| PUT | `/api/board/proposals/{id}/reject` | Từ chối proposal |

---

## 7. 🤝 Assistant — `AssistantTaskController` (`/api/assistant/tasks`)

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/assistant/tasks?assistantEmail={email}` | Danh sách task được giao |
| PUT | `/api/assistant/tasks/{taskId}/start` | Bắt đầu làm task |
| PUT | `/api/assistant/tasks/{taskId}/submit` | Nộp task hoàn thành |

---

## 8. Trạng thái (status) dùng trong hệ thống

### Proposal / Series status
`DRAFT` → `SUBMITTED_TO_EDITOR` → `REVISION_REQUESTED` / `UNDER_BOARD_REVIEW` → `APPROVED` / `REJECTED`

### Task status
`ASSIGNED` → `PENDING_REVIEW` → `APPROVED` / `REVISION_REQUESTED`

### User status
`ACTIVE`, `INACTIVE`

---

> 📌 **Lưu ý chênh lệch path:**
> - Nhóm Admin dùng prefix `/admin` (**không** có `/api`).
> - Các nhóm còn lại dùng `/api/...`.
> - Task nằm trực tiếp dưới `pages` (`.../pages/{pageId}/tasks`), **không** có cấp `regions`.
