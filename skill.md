# skill.md — SWP391_NEW Efficient Development Guide

**Mục tiêu:** Hướng dẫn agent làm việc hiệu quả cho project mới **Manga Creation Workflow and Publishing Management System**, bám đúng kiến trúc mục tiêu trong `form.txt`, đúng database mục tiêu trong `database.txt`, ít tốn context/token, và luôn verify trước khi báo done.

**Áp dụng cho:** Mọi session/agent làm việc trong:

```text
C:\Users\AD\OneDrive\Máy tính\Giao trình FPT\KY5\SWP391_NEW
```

---

## 1. Skill này dùng để làm gì

Skill này giúp agent:

1. Làm đúng **new project mode**.
2. Bám theo `CURRENT.md`, `form.txt`, `database.txt`.
3. Không đọc/paste toàn bộ repo nếu task chỉ cần vài file.
4. Chia việc theo feature/module rõ ràng.
5. Giữ backend, frontend, database, docs đồng bộ.
6. Verify bằng read/build/test trước khi báo xong.
7. Chuẩn bị handoff để session sau resume nhanh.

---

## 2. Context bắt buộc trước khi làm việc

Trước khi sửa code hoặc tài liệu, đọc theo thứ tự:

1. `CURRENT.md`
2. `rule.md`
3. `form.txt` nếu task liên quan architecture/layout
4. `database.txt` nếu task liên quan schema/entity/API data model
5. File feature liên quan trong `backend` hoặc `frontend`
6. Docs liên quan trong `docs/`

Không đọc toàn bộ repo nếu task chỉ cần 1–3 file.

---

## 3. Core project direction

Project này ưu tiên kiến trúc đơn giản, rõ ràng, và đúng nghiệp vụ manga workflow.

### Main flow cần nhớ

```text
Mangaka tạo proposal + upload manuscript
-> Mangaka submit cho Tantou Editor
-> Tantou Editor request revision hoặc forward cho Editorial Board
-> 3 Board members vote approve/reject
-> System auto-decides by majority
-> Nếu approved, Mangaka tạo chapter/page/task
-> Assistant bắt đầu và submit task
-> Mangaka approve hoặc request redo
```

### Target structure theo `form.txt`

- `backend/`
  - `src/api/`
  - `src/application/`
  - `src/domain/`
  - `src/infrastructure/`
  - `src/config/`
  - `tests/`
- `ai-subsystem/`
- `frontend/`
  - `src/components/`
  - `src/views/`
- `storage-server/`
- `database/`
  - `migrations/`
  - `seeds/`

---

## 4. Database scope phải bám theo

Các nhóm bảng chính từ `database.txt`:

### Access control
- `roles`
- `permissions`
- `role_permissions`
- `users`
- `assistant_profiles`
- `skills`
- `user_skills`

### Content
- `series`
- `chapters`
- `pages`

### Workflow
- `tasks`
- `submissions`
- `annotations`

### Strategy / metrics
- `reader_metrics`
- `board_votes`

Khi cần thêm bảng mới, phải giải thích rõ nó có thật sự cần cho MVP hay không.

---

## 5. Canonical roles

Dùng role chuẩn này trong code/docs/data:

| DB value | Backend enum preferred | Display |
|---|---|---|
| `admin` | `Admin` | Admin |
| `mangaka` | `Mangaka` | Mangaka |
| `assistant` | `Assistant` | Assistant |
| `tantou_editor` | `TantouEditor` | Tantou Editor |
| `editorial_board_member` | `EditorialBoardMember` | Editorial Board Member |

Không tự ý đổi tên thành nhãn cũ nếu không có lý do rõ ràng.

---

## 6. Chọn context theo loại task

| Task | Nên đọc |
|---|---|
| Login/auth | backend controllers/services/dtos + frontend login form/service |
| User/role management | user/role-related backend + admin UI |
| Proposal workflow | series/manuscript/submission data + editor/mangaka screens |
| Board voting | vote flow backend + board UI |
| Production workflow | chapter/page/task/submission entities + task UI |
| DB migration | `database.txt` + schema/migration files |
| UI polish | component/view/CSS liên quan, không đọc backend nếu API không đổi |
| Full-stack feature | backend DTO + frontend types/services + UI |

---

## 7. Implementation conventions

### Backend

- Tách rõ `controllers`, `services`, `entities`, `repositories`, `dtos`.
- DTO nên chỉ mang dữ liệu cần thiết.
- Business rules đặt ở application/service layer, không nhét hết vào controller.
- Validation nên làm sớm ở boundary.
- Nếu có workflow/state, viết rõ trạng thái và transition.

### Frontend

- Role dashboards nên tách theo view.
- Component dùng lại nên để trong `components/`.
- Không để một file ôm quá nhiều UI logic nếu có thể chia nhỏ.
- API types phải khớp với backend contract.

### Database

- PostgreSQL là mặc định.
- Dùng migration thay vì sửa tay schema chạy đua.
- Seed data nên tách riêng.
- Dùng naming nhất quán giữa tables/columns/entities.

---

## 8. Token/context saving rules

- Đọc đúng file liên quan, không đọc cả repo nếu không cần.
- Khi báo kết quả, ghi path + kết luận ngắn thay vì paste toàn file.
- Khi cần hiểu một flow, ưu tiên read file đã liên quan thay vì search mù.
- Không lặp lại cùng một context nếu chưa có gì mới.

---

## 9. Verification checklist

### Documentation-only task
- Read lại file đã tạo/sửa.
- Confirm path đúng trong `SWP391_NEW`.
- Check nội dung có khớp `CURRENT.md`, `form.txt`, `database.txt`.

### Backend task
- Read back file changed.
- Run build/test nếu có thể, ví dụ `mvn test` hoặc `mvn package`.
- Check compile errors/warnings relevant.

### Frontend task
- Read back file changed.
- Run build nếu có thể, ví dụ `npm run build`.
- Check contract/data shape if API changed.

### Database task
- Read relevant schema sections.
- Check naming and relationships against `database.txt`.
- Validate syntax if possible.

### Full-stack task
- Backend build/test.
- Frontend build.
- Contract consistency between backend and frontend.

---

## 10. When to update `CURRENT.md`

Update `CURRENT.md` when:

- user says update current / ghi nhớ lại / cập nhật
- project direction changes
- major file set changes
- new decision is locked
- blocker appears
- build/test result matters for future session

Keep it as working memory, not a noisy log of every tiny read.

---

## 11. Good agent behavior

- Ask before guessing on architecture/database decisions.
- Keep changes bounded to the task.
- Prefer simple, maintainable structure.
- Respect the target docs over legacy code.
- Verify before claiming done.
- Leave enough breadcrumbs for next session.

---

## 12. Common mistakes to avoid

- Treating old clone or old schema as source of truth.
- Implementing extra features beyond MVP without request.
- Mixing role names inconsistently.
- Creating unnecessary module fragmentation.
- Editing too many files at once without reason.
- Reporting done without read-back or build/test.
- Forgetting to keep docs aligned with code.

---

## 13. Quick resume checklist

Before starting any work:

1. Read `CURRENT.md`.
2. Read `rule.md`.
3. Read `form.txt` / `database.txt` if relevant.
4. Identify only the minimum files needed.
5. Edit with intent.
6. Verify.
7. Update `CURRENT.md` if the change matters for future sessions.

---

**Golden rule:** Bám target docs mới, sửa ít nhưng đúng, verify trước khi báo done, và giữ project dễ tiếp tục cho session sau.
