# rule.md — SWP391_NEW Manga Workflow Project Rule

**Mục đích:** Giữ mọi session/agent làm việc đúng hướng cho project mới **Manga Creation Workflow and Publishing Management System**, tránh sửa nhầm theo kiến trúc cũ, luôn có context rõ ràng, verify trước khi báo xong, và dễ resume khi mất session.

**Áp dụng cho:** Mọi agent/session làm việc trong project:

```text
C:\Users\AD\OneDrive\Máy tính\Giao trình FPT\KY5\SWP391_NEW
```

---

## 1. Nguồn sự thật bắt buộc

Khi có bất kỳ quyết định nào về kiến trúc, database, module, hoặc business flow, ưu tiên theo thứ tự sau:

1. `CURRENT.md` — file ghi nhớ trạng thái, hướng làm, luật session.
2. `form.txt` — kiến trúc/folder structure mục tiêu của project mới.
3. `database.txt` — database model mục tiêu của project mới.
4. Code/demo cũ trong `backend` và `frontend` — chỉ dùng để tham khảo workflow/demo behavior nếu còn phù hợp, không xem là chuẩn kiến trúc cuối.
5. `README.md` và docs khác — tham khảo bổ sung.

**Quy tắc quan trọng:**

- Không copy nguyên kiến trúc cũ nếu nó trái với `form.txt`.
- Không dùng schema cũ nếu nó trái với `database.txt`.
- `Member 1` là nhãn phân công công việc của team, không phải user/role/account trong app.
- Không tạo package/class/module tên `Member 1`; đó chỉ là nhãn phân công team, không phải tên code.
- Nếu user nói “cập nhật”, “update current”, “ghi nhớ lại”, phải update `CURRENT.md`.
- Trả lời mặc định bằng tiếng Việt, trừ khi user hỏi bằng tiếng Anh hoặc yêu cầu tiếng Anh rõ ràng.
- Luôn coi flow nghiệp vụ đúng là:
  - Flow 1: Mangaka tạo proposal/draft -> Tantou Editor request revision hoặc forward Board -> Board vote -> approved/rejected
  - Flow 2: Chỉ khi approved mới tạo series từ proposal title -> Mangaka production chapter/page/task -> Assistant submit -> Mangaka review
---

## 2. Khi bắt đầu session mới

Trước khi sửa file, đọc theo thứ tự:

1. `rule.md`
2. `skill.md`
3. `CURRENT.md`
4. Nếu task liên quan kiến trúc: `form.txt`
5. Nếu task liên quan database/backend/entity/API: `database.txt`
6. Nếu task liên quan UI: file frontend/view/component liên quan
7. Nếu task liên quan backend: file controller/service/entity/repository liên quan

Không đọc toàn bộ repo nếu task chỉ cần vài file.

---

## 3. Phạm vi sửa file

Được phép sửa trong project:

```text
C:\Users\AD\OneDrive\Máy tính\Giao trình FPT\KY5\SWP391_NEW
```

Không sửa các project khác như `SWP391`, `SWP391_clone`, hoặc `previous_session` trừ khi user yêu cầu rõ ràng.

Không commit/push/amend/force-push nếu user chưa yêu cầu rõ ràng.

Không commit hoặc ghi vào docs/source:

- password thật
- token/API key
- local DB files
- `node_modules`
- `target`
- `dist`
- uploads/storage runtime
- logs runtime

---

## 4. Cách xử lý project mới

Project này đang được xem như **new project mode**.

Nghĩa là:

- `form.txt` là kiến trúc target.
- `database.txt` là database target.
- `backend` và `frontend` hiện tại chỉ là baseline/reference.
- Nếu rebuild/refactor, ưu tiên cấu trúc đơn giản, ít class hơn, module rõ hơn.
- Không bị ràng buộc phải giữ nguyên cách chia class cũ.

Luồng nghiệp vụ chính cần giữ:

```text
Mangaka tạo proposal + upload manuscript
-> Mangaka submit cho Tantou Editor
-> Tantou Editor request revision hoặc forward cho Editorial Board
-> 3 Board members vote approve/reject
-> System auto-decides by majority
-> Nếu approved, Mangaka tạo chapter/page/task
-> Assistant nhận/làm/submit task
-> Mangaka approve hoặc request redo
```

---

## 5. Quy tắc database

Khi làm database:

1. `database.txt` là source of truth.
2. PostgreSQL là DB mục tiêu.
3. Migration nên đặt trong:

```text
database/migrations/
```

4. Seed data nên đặt trong:

```text
database/seeds/
```

5. Nếu tạo schema mới, phải bao gồm tối thiểu các nhóm bảng trong `database.txt`:
   - user/access: `roles`, `permissions`, `role_permissions`, `users`, `assistant_profiles`, `skills`, `user_skills`
   - content: `series`, `chapters`, `pages`
   - production: `tasks`, `submissions`, `annotations`
   - metrics/board: `reader_metrics`, `board_votes`
6. Không dùng SQL Server/MySQL syntax nếu mục tiêu là PostgreSQL.
7. Dùng `jsonb` cho tọa độ vùng/annotation nếu cần lưu bounding box/spatial data.

---

## 6. Quy tắc backend

Target backend architecture hiện tại:

```text
backend/src/main/java/com/mangastudio/workflow/controllers/
backend/src/main/java/com/mangastudio/workflow/services/
backend/src/main/java/com/mangastudio/workflow/repositories/
backend/src/main/java/com/mangastudio/workflow/entities/
backend/src/main/java/com/mangastudio/workflow/dtos/
backend/src/main/java/com/mangastudio/workflow/config/
backend/src/test/java/com/mangastudio/workflow/
```

Ý nghĩa:

- `controllers/`: controller/route entry point
- `services/`: service, business rules, workflow/state machine
- `repositories/`: Spring Data repository/persistence access
- `entities/`: JPA entities
- `dtos/`: request/response DTOs, enums, core API data shape
- `config/`: DB/server/security/CORS configuration

Java Spring Boot mapping:

```text
controllers/  -> REST controllers
services/     -> service/usecase
entities/     -> JPA entities
dtos/         -> request/response DTOs
repositories/ -> repository/storage/external clients
config/       -> Spring config/security/CORS
```

---

## 7. Quy tắc frontend

Target architecture theo `form.txt`:

```text
frontend/src/components/
frontend/src/views/
```

Role dashboards nên nằm trong:

```text
frontend/src/views/MangakaDashboard/
frontend/src/views/AssistantDashboard/
frontend/src/views/EditorDashboard/
frontend/src/views/BoardDashboard/
```

Component dùng lại đặt trong:

```text
frontend/src/components/
```

Ví dụ:

```text
components/VisualCanvas/
components/forms/
components/tables/
components/layout/
```

Không nhồi quá nhiều workflow vào một file nếu file trở nên khó maintain.

---

## 8. Quy tắc storage và AI subsystem

Nếu task liên quan upload/file:

```text
storage-server/manuscripts/
storage-server/submissions/
storage-server/annotations/
storage-server/references/
```

Không lưu file upload runtime vào git.

Nếu task liên quan AI:

```text
ai-subsystem/models/
ai-subsystem/scripts/
ai-subsystem/api_bridge.py
```

AI subsystem là optional/placeholder trừ khi user yêu cầu triển khai thật.

---

## 9. Quy tắc verify trước khi báo done

Tùy loại task:

### Documentation-only

- Read lại file đã tạo/sửa.
- Kiểm tra path đúng trong `SWP391_NEW`.

### Database

- Read lại migration/schema.
- Kiểm tra tên bảng/cột bám `database.txt`.
- Nếu có thể, validate bằng PostgreSQL hoặc ít nhất check syntax thủ công.

### Backend

- Read lại file đã sửa.
- Chạy build/test phù hợp nếu môi trường cho phép.
- Với Maven/Spring Boot thường dùng:

```text
mvn test
mvn package
```

### Frontend

- Read lại file đã sửa.
- Chạy build nếu có thể:

```text
npm run build
```

### Full-stack

- Backend build/test.
- Frontend build.
- API contract giữa backend DTO và frontend types/services phải khớp.

Nếu không chạy được verify, nói rõ lý do.

---

## 10. Quy tắc chạy server/process

Không chạy trực tiếp process long-running và chờ tool treo.

Backend/frontend/dev server/watch/server phải chạy detached/background, có:

- PID
- log path stdout/stderr
- URL/port
- readiness check bounded timeout
- cách stop process

Port thường dùng:

```text
Backend: 8080
Frontend: 5173
```

Nếu cần DB password mà chưa có, hỏi user. Không auto-loop.

---

## 11. Quy tắc cập nhật `CURRENT.md`

Khi user nói:

```text
cập nhật
update current
cập nhật session
ghi nhớ lại
```

thì update `CURRENT.md` với:

- việc vừa làm
- file đã thay đổi
- quyết định mới
- blocker/rủi ro
- next steps
- nếu project direction thay đổi

Không tự ý update `CURRENT.md` cho mọi thay đổi nhỏ nếu user không yêu cầu, trừ khi thay đổi đó ảnh hưởng lâu dài đến project.

---

## 12. Demo accounts tham khảo

Nếu cần demo theo baseline cũ, tài khoản tham khảo:

| Role | Email | Password |
|---|---|---|
| Admin | `admin@manga.local` | `Admin@123` |
| Mangaka | `mangaka@manga.local` | `Mangaka@123` |
| Assistant | `assistant@manga.local` | `Assistant@123` |
| Tantou Editor | `editor@manga.local` | `Editor@123` |
| Board 1 | `board@manga.local` | `Board@123` |
| Board 2 | `board2@manga.local` | `Board2@123` |
| Board 3 | `board3@manga.local` | `Board3@123` |

Không hard-code password thật trong production logic.

---

## 13. Những lỗi cần tránh

- Sửa nhầm `SWP391_clone` hoặc project cũ.
- Dùng code cũ làm chuẩn kiến trúc thay vì `form.txt`.
- Dùng schema cũ làm chuẩn database thay vì `database.txt`.
- Tạo quá nhiều class/module nhỏ không cần thiết.
- Đổi role name ở frontend nhưng quên backend/database.
- Báo done khi chưa read-back/build/test.
- Tự động commit/push.
- Ghi secret vào docs/log/source.
- Chạy server long-running trực tiếp trong shell.

---

## 14. Minimal resume checklist

Khi agent/session mới vào project:

1. Read `rule.md`.
2. Read `skill.md`.
3. Read `CURRENT.md`.
4. Nếu architecture/database relevant, read `form.txt` và `database.txt`.
5. Xác nhận đang làm trong:

```text
C:\Users\AD\OneDrive\Máy tính\Giao trình FPT\KY5\SWP391_NEW
```

6. Chỉ sửa đúng file liên quan.
7. Verify trước khi báo done.

---

**Golden rule:** Bám `CURRENT.md` + `form.txt` + `database.txt`. Code cũ chỉ để tham khảo. Sửa ít nhưng đúng. Verify rồi mới báo xong.


