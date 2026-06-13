# So sánh schema mới kết hợp với schema cũ

## 1. Các file đã đọc

### Bản mới 1

```txt
manga_database/manga_database/schema.sql
```

Đây là bản schema MVP ngắn gọn, có các bảng chính cho user, role, series, chapter, page, task, submission, annotation, metric và vote.

### Bản mới 2

```txt
schema.sql
```

Đây là bản phát triển thêm từ bản mới 1. Bản này giữ cấu trúc đơn giản của MVP nhưng có bổ sung vòng sửa cho series và task.

### Bản cũ

```txt
docs/database/schema_postgresql_v2.sql
```

Đây là bản schema cũ, chi tiết hơn nhiều, có nhiều bảng phụ trợ như audit log, notifications, board membership, manuscript versioning, task review, ranking, earning rate.

---

## 2. Kết luận nhanh

Nên dùng file sau làm bản chuẩn MVP hiện tại:

```txt
schema.sql
```

Lý do:

- Nó là bản kết hợp tốt hơn giữa bản schema ngắn trong `manga_database` và nhu cầu workflow thực tế.
- Nó có thêm `REVISION_REQUESTED` cho `series.status`.
- Nó có thêm `feedback_notes` cho `tasks`.
- Nó đổi task bị trả lại từ `REJECTED` sang `REVISION_REQUESTED`, hợp lý hơn cho luồng Mangaka yêu cầu Assistant sửa bài.
- Nó vẫn đủ đơn giản để nhóm frontend/backend triển khai trong SWP391.

Tuy nhiên, trước khi chốt hẳn, nên chỉnh thêm vài điểm nhỏ được liệt kê ở phần recommendation.

---

## 3. Bản mới 1 khác gì bản mới 2

### 3.1. Series có thêm trạng thái `REVISION_REQUESTED`

Bản mới 1 có:

```sql
'DRAFT',
'SUBMITTED_TO_EDITOR',
'UNDER_BOARD_REVIEW',
'APPROVED',
'REJECTED'
```

Bản mới 2 có thêm:

```sql
'REVISION_REQUESTED'
```

Ý nghĩa:

```txt
Editor có thể gửi lại series/proposal cho Mangaka sửa trước khi đưa lên Editorial Board.
```

Luồng hợp lý hơn:

```txt
DRAFT
-> SUBMITTED_TO_EDITOR
-> REVISION_REQUESTED
-> SUBMITTED_TO_EDITOR
-> UNDER_BOARD_REVIEW
-> APPROVED / REJECTED
```

Recommendation:

```txt
Nên giữ REVISION_REQUESTED trong series.status.
```

---

### 3.2. Tasks có thêm `feedback_notes`

Bản mới 2 có thêm:

```sql
feedback_notes TEXT
```

Ý nghĩa:

```txt
Mangaka có thể ghi lý do hoặc yêu cầu sửa cho Assistant khi task chưa được duyệt.
```

Nếu không có cột này, frontend/backend sẽ khó hiển thị vì sao task bị trả lại.

Recommendation:

```txt
Nên giữ feedback_notes.
```

---

### 3.3. Task status đổi từ `REJECTED` sang `REVISION_REQUESTED`

Bản mới 1:

```sql
'ASSIGNED', 'PENDING_REVIEW', 'APPROVED', 'REJECTED'
```

Bản mới 2:

```sql
'ASSIGNED', 'PENDING_REVIEW', 'APPROVED', 'REVISION_REQUESTED'
```

Ý nghĩa:

```txt
Với task của Assistant, thường không nên hiểu là reject vĩnh viễn, mà là yêu cầu sửa lại.
```

Recommendation:

```txt
Nên giữ REVISION_REQUESTED.
Nhưng có thể thêm lại REJECTED hoặc CANCELLED nếu muốn biểu diễn task bị hủy hoặc không chấp nhận hoàn toàn.
```

Đề xuất trạng thái task tốt hơn:

```sql
'ASSIGNED',
'PENDING_REVIEW',
'APPROVED',
'REVISION_REQUESTED',
'REJECTED',
'CANCELLED'
```

Nếu muốn MVP gọn thì có thể chỉ giữ:

```sql
'ASSIGNED',
'PENDING_REVIEW',
'APPROVED',
'REVISION_REQUESTED'
```

---

## 4. Bản mới kết hợp khác gì bản cũ

Bản mới kết hợp ở đây là:

```txt
schema.sql
```

Bản cũ là:

```txt
docs/database/schema_postgresql_v2.sql
```

---

## 5. Khác biệt lớn về triết lý thiết kế

### Bản mới kết hợp

Bản mới đi theo hướng:

```txt
MVP đơn giản, dễ code, dễ chia việc, dễ map sang Java Entity và React screen.
```

Có khoảng 15 bảng chính:

```txt
roles
permissions
role_permissions
users
assistant_profiles
skills
user_skills
series
chapters
pages
tasks
submissions
annotations
reader_metrics
board_votes
```

### Bản cũ

Bản cũ đi theo hướng:

```txt
Hệ thống đầy đủ hơn, gần production hơn, nhiều rule nghiệp vụ hơn.
```

Có khoảng 24 bảng:

```txt
users
skills
user_skills
editorial_boards
board_memberships
audit_logs
series
manuscripts
editor_annotations
submissions
submission_votes
chapters
pages
page_regions
page_versions
tasks
task_submissions
task_reviews
reader_poll_data
series_rankings
series_decisions
series_decision_votes
notifications
earning_rates
```

Kết luận:

```txt
Bản mới dễ làm hơn.
Bản cũ đầy đủ hơn nhưng phức tạp hơn rất nhiều.
```

---

## 6. So sánh từng module

## 6.1. User, Role, Permission

### Bản mới kết hợp

Có RBAC rõ ràng:

```sql
roles
permissions
role_permissions
users
```

User trỏ tới role bằng:

```sql
role_id INTEGER REFERENCES roles(role_id)
```

### Bản cũ

Không có bảng `roles`, `permissions`, `role_permissions`.

User lưu role trực tiếp trong cột:

```sql
role VARCHAR(40)
```

### Nhận xét

Bản mới tốt hơn nếu muốn có thiết kế phân quyền rõ hơn.

Nhưng bản mới hiện chưa seed permissions và role_permissions nên nếu backend thật sự dùng permission thì còn thiếu dữ liệu mặc định.

### Recommendation

Nên giữ thiết kế RBAC của bản mới.

Nên đổi:

```sql
role_id INTEGER REFERENCES roles(role_id)
```

thành:

```sql
role_id INTEGER NOT NULL REFERENCES roles(role_id)
```

Nếu chưa định dùng permission chi tiết trong MVP, có thể vẫn giữ bảng `permissions` và `role_permissions` để mở rộng sau.

---

## 6.2. User profile

### Bản mới kết hợp

Có bảng:

```sql
assistant_profiles
```

Dùng để lưu:

```sql
monthly_earnings
```

### Bản cũ

Không có `assistant_profiles`, nhưng có `earning_rates` để tính tiền theo loại task.

### Nhận xét

Bản mới dễ làm màn hình thu nhập cho Assistant hơn.

Bản cũ chuẩn hơn nếu muốn tính tiền theo rate, task type và thời gian hiệu lực.

### Recommendation

Với MVP, giữ `assistant_profiles.monthly_earnings` là được.

Nếu muốn tốt hơn, không nên chỉ lưu earnings cố định lâu dài. Sau này có thể tính từ `tasks.payment` hoặc thêm bảng payment/earning history.

---

## 6.3. Series / Proposal workflow

### Bản mới kết hợp

Bảng `series` gộp cả proposal lifecycle:

```sql
DRAFT
SUBMITTED_TO_EDITOR
REVISION_REQUESTED
UNDER_BOARD_REVIEW
APPROVED
REJECTED
```

Có thêm:

```sql
editor_notes TEXT
```

### Bản cũ

Bảng `series` có nhiều trạng thái hơn:

```sql
draft
submitted_to_editor
under_editor_review
needs_revision
escalated_to_board
approved
serializing
on_hold
cancelled
archived
```

Bản cũ còn tách `manuscripts`, `submissions`, `submission_votes` để xử lý proposal/manuscript chi tiết.

### Nhận xét

Bản mới đơn giản, dễ code.

Bản cũ đúng nghiệp vụ hơn nếu cần quản lý nhiều version manuscript, editor report, board voting chi tiết.

### Recommendation

Với SWP391/MVP, dùng bản mới.

Nên cân nhắc thêm trạng thái sau nếu cần demo rõ hơn:

```sql
SERIALIZING
CANCELLED
```

Nhưng nếu muốn giữ gọn thì chưa cần.

---

## 6.4. Editorial Board và voting

### Bản mới kết hợp

Chỉ có:

```sql
board_votes
```

Vote trực tiếp theo:

```sql
series_id
board_member_id
decision
```

Mỗi board member chỉ vote một lần cho một series:

```sql
UNIQUE (series_id, board_member_id)
```

### Bản cũ

Có đầy đủ hơn:

```sql
editorial_boards
board_memberships
submissions
submission_votes
```

### Nhận xét

Bản mới rất dễ làm.

Nhược điểm: không quản lý được board nào, nhiệm kỳ thành viên, board active/dissolved, submission nào được gửi tới board nào.

### Recommendation

Với MVP, giữ `board_votes` là đủ.

Nếu use case có yêu cầu Admin quản lý Editorial Board/member thì cần lấy lại từ bản cũ:

```sql
editorial_boards
board_memberships
```

Nếu không có use case đó thì không nên thêm vì sẽ phức tạp.

---

## 6.5. Chapter / Page production

### Bản mới kết hợp

Có:

```sql
chapters
pages
```

`pages` lưu file bằng:

```sql
manuscript_file_path
```

### Bản cũ

Có chi tiết hơn:

```sql
chapters
pages
page_regions
page_versions
```

Bản cũ có status cho chapter/page, dimension ảnh, version ảnh.

### Nhận xét

Bản mới đủ để upload page và tạo task theo page.

Bản cũ phù hợp nếu cần lưu nhiều version ảnh: original, assistant output, compiled, final.

### Recommendation

Với MVP, giữ bản mới.

Nên thêm `status` cho `chapters` và `pages` nếu frontend cần hiển thị tiến độ.

Ví dụ:

```sql
chapters.status
pages.status
```

Nhưng nếu backend service có thể tính từ task status thì chưa cần.

---

## 6.6. Task workflow

### Bản mới kết hợp

Bảng `tasks` có:

```sql
page_id
assistant_id
task_type
region_coordinates
payment
status
feedback_notes
```

Status:

```sql
ASSIGNED
PENDING_REVIEW
APPROVED
REVISION_REQUESTED
```

### Bản cũ

Task phức tạp hơn:

```sql
region_id
assigned_to
assigned_by
task_type
unit_count
rate_snapshot
status
due_at
submitted_at
approved_at
```

Ngoài ra còn có:

```sql
task_submissions
task_reviews
```

### Nhận xét

Bản mới hợp với UI React Canvas: Mangaka vẽ vùng và tạo task trực tiếp.

Bản cũ quản lý review/history tốt hơn.

### Recommendation

Nên dùng bản mới.

Nên cân nhắc thêm:

```sql
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

vào `tasks`.

Nên cân nhắc thêm `REJECTED` hoặc `CANCELLED` nếu có trường hợp task bị hủy.

---

## 6.7. Submissions

### Bản mới kết hợp

Có bảng:

```sql
submissions
```

Dùng cho Assistant nộp file task:

```sql
task_id
asset_file_path
timestamp
```

### Bản cũ

Có 2 khái niệm riêng:

```sql
submissions
```

cho Editor gửi manuscript/proposal lên Board.

và:

```sql
task_submissions
```

cho Assistant nộp task.

### Nhận xét

Bản mới dùng tên `submissions` cho task submission. Điều này đơn giản nhưng dễ gây nhầm nếu sau này có proposal submission.

Cột `timestamp` cũng không nên dùng làm tên cột vì dễ nhầm với kiểu dữ liệu SQL.

### Recommendation

Nên đổi tên cột:

```sql
timestamp
```

thành:

```sql
submitted_at
```

Nếu muốn rõ hơn, có thể đổi tên bảng:

```sql
submissions
```

thành:

```sql
task_submissions
```

Nhưng nếu muốn ít sửa code/entity thì chỉ đổi `timestamp` thành `submitted_at` là đủ.

---

## 6.8. Annotations

### Bản mới kết hợp

Có bảng:

```sql
annotations
```

Gắn annotation vào page:

```sql
page_id
editor_id
spatial_coordinates
content
created_at
```

### Bản cũ

Có:

```sql
editor_annotations
```

Gắn vào manuscript, có page_number, x, y, type, resolved.

### Nhận xét

Bản mới linh hoạt hơn vì dùng JSONB cho tọa độ.

Bản cũ validate tọa độ chặt hơn và có resolved status.

### Recommendation

Với MVP, giữ bản mới.

Nếu frontend cần mark comment đã xử lý, nên thêm:

```sql
resolved BOOLEAN DEFAULT FALSE
```

---

## 6.9. Reader metrics / Ranking

### Bản mới kết hợp

Có:

```sql
reader_metrics
```

Gồm:

```sql
sales_figures
likes_count
shares_count
votes_count
```

### Bản cũ

Có:

```sql
reader_poll_data
series_rankings
series_decisions
series_decision_votes
```

### Nhận xét

Bản mới đủ để `RankingService` tính điểm từ metrics.

Bản cũ lưu được cả ranking snapshot và quyết định chiến lược sau ranking.

### Recommendation

Với MVP, giữ `reader_metrics`.

Nếu cần hiển thị lịch sử ranking theo kỳ, nên thêm bảng `series_rankings` từ bản cũ hoặc để backend tính realtime từ `reader_metrics`.

---

## 6.10. Audit logs và notifications

### Bản mới kết hợp

Không có:

```sql
audit_logs
notifications
```

### Bản cũ

Có cả 2 bảng này.

### Nhận xét

Bản mới dễ làm hơn nhưng thiếu lịch sử hành động và thông báo hệ thống.

### Recommendation

Nếu không có yêu cầu use case cụ thể cho notification/audit, không nên thêm vào MVP.

Nếu cần demo thông báo task/series, có thể lấy lại bảng `notifications` từ bản cũ.

---

## 7. Các điểm nên chỉnh trong bản mới kết hợp

## 7.1. Nên đổi `submissions.timestamp` thành `submitted_at`

Hiện tại:

```sql
timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

Đề xuất:

```sql
submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

Lý do:

- Rõ nghĩa hơn.
- Tránh dùng tên giống kiểu dữ liệu SQL.
- Dễ map sang Java field `submittedAt`.

Mức ưu tiên:

```txt
Cao
```

---

## 7.2. Nên cho `users.role_id` là NOT NULL

Hiện tại:

```sql
role_id INTEGER REFERENCES roles(role_id)
```

Đề xuất:

```sql
role_id INTEGER NOT NULL REFERENCES roles(role_id)
```

Lý do:

- User trong hệ thống bắt buộc phải có role.
- Tránh user không có quyền/vai trò.

Mức ưu tiên:

```txt
Cao
```

---

## 7.3. Nên seed thêm skills mặc định

Hiện tại chỉ seed:

```sql
INSERT INTO roles (role_name) VALUES ('Admin'), ('Mangaka'), ('Assistant'), ('Editor'), ('Board');
```

Nhưng `tasks.task_type` đang reference tới:

```sql
skills(skill_id)
```

Đề xuất seed thêm:

```sql
INSERT INTO skills (skill_name) VALUES
('Background'),
('Inking'),
('Shading'),
('Screentone'),
('Effect Line'),
('Speech Bubble'),
('Cleanup'),
('Coloring');
```

Lý do:

- Có dữ liệu để tạo task ngay.
- Backend/frontend dễ demo hơn.

Mức ưu tiên:

```txt
Cao
```

---

## 7.4. Nên cân nhắc seed permissions và role_permissions

Vì bản mới có:

```sql
permissions
role_permissions
```

Nhưng chưa seed permission nào.

Nếu backend chưa dùng permission chi tiết thì có thể để sau.

Nếu backend có kiểm tra permission, cần seed ngay.

Mức ưu tiên:

```txt
Trung bình
```

---

## 7.5. Nên thêm `created_at` cho các bảng chính

Nên thêm cho:

```txt
users
chapters
pages
tasks
submissions
board_votes
reader_metrics
```

Lý do:

- Dễ debug.
- Dễ sort danh sách.
- Dễ hiển thị lịch sử.

Mức ưu tiên:

```txt
Trung bình
```

---

## 7.6. Nên xử lý `updated_at` tự động

Hiện tại `series` có:

```sql
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

Nhưng không có trigger tự update.

Có 2 cách:

### Cách đơn giản

Backend tự set `updatedAt` khi update entity.

### Cách database

Thêm function/trigger giống bản cũ:

```sql
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

Recommendation:

```txt
Nếu muốn schema sạch hơn, lấy trigger từ bản cũ.
Nếu muốn MVP đơn giản, để backend xử lý.
```

Mức ưu tiên:

```txt
Trung bình
```

---

## 7.7. Cân nhắc thêm `REJECTED` hoặc `CANCELLED` cho task

Hiện tại:

```sql
ASSIGNED
PENDING_REVIEW
APPROVED
REVISION_REQUESTED
```

Đề xuất nếu muốn đủ hơn:

```sql
ASSIGNED
PENDING_REVIEW
APPROVED
REVISION_REQUESTED
REJECTED
CANCELLED
```

Lý do:

- `REVISION_REQUESTED` là yêu cầu sửa.
- `REJECTED` là không chấp nhận.
- `CANCELLED` là task bị hủy.

Mức ưu tiên:

```txt
Thấp đến trung bình
```

---

## 7.8. Cân nhắc thêm `resolved` cho annotations

Hiện tại annotation không biết đã xử lý hay chưa.

Đề xuất:

```sql
resolved BOOLEAN DEFAULT FALSE
```

Mức ưu tiên:

```txt
Thấp đến trung bình
```

---

## 8. Những thứ từ bản cũ KHÔNG nên đưa vào MVP ngay

Không nên đưa ngay nếu muốn giữ project đơn giản:

```txt
audit_logs
page_versions
task_reviews
earning_rates
series_decision_votes
series_decisions
```

Lý do:

- Làm entity/service/controller phức tạp hơn.
- Tăng số màn hình frontend cần xử lý.
- Dễ bị scope creep.
- Không cần thiết nếu chỉ demo luồng chính.

---

## 9. Những thứ từ bản cũ CÓ THỂ cân nhắc lấy lại

Chỉ lấy nếu use case thật sự cần:

### 9.1. `notifications`

Dùng nếu cần thông báo:

- Series submitted.
- Revision requested.
- Task assigned.
- Task approved.
- Ranking updated.

### 9.2. `editorial_boards` và `board_memberships`

Dùng nếu Admin cần quản lý hội đồng biên tập và thành viên hội đồng.

### 9.3. `series_rankings`

Dùng nếu cần lưu lịch sử bảng xếp hạng theo kỳ, thay vì chỉ tính realtime.

---

## 10. Recommendation cuối cùng

### Nên chọn làm base

```txt
schema.sql
```

### Nên sửa nhỏ trước khi chốt

1. Đổi:

```sql
timestamp
```

thành:

```sql
submitted_at
```

2. Đổi:

```sql
role_id INTEGER REFERENCES roles(role_id)
```

thành:

```sql
role_id INTEGER NOT NULL REFERENCES roles(role_id)
```

3. Seed thêm skills mặc định.

4. Cân nhắc thêm `created_at` cho các bảng chính.

5. Cân nhắc thêm `REJECTED` hoặc `CANCELLED` cho `tasks.status` nếu muốn phân biệt task bị sửa và task bị hủy.

### Không nên merge toàn bộ bản cũ vào bản mới

Vì bản cũ quá đầy đủ, nếu merge hết sẽ làm project phình to và khó hoàn thành.

### Cách đi hợp lý nhất

```txt
Dùng schema.sql làm chuẩn MVP.
Chỉ lấy một vài ý tốt từ bản cũ: index, updated_at trigger, notifications nếu thật sự cần.
Không đưa các bảng phức tạp vào nếu chưa có màn hình/use case rõ ràng.
```

---

## 11. Bản chốt đề xuất ngắn gọn

Nếu cần chốt nhanh, đề xuất như sau:

```txt
Base: schema.sql
Keep: REVISION_REQUESTED ở series và tasks
Keep: feedback_notes ở tasks
Change: submissions.timestamp -> submissions.submitted_at
Change: users.role_id -> NOT NULL
Add: seed skills
Optional: created_at/updated_at cho bảng chính
Optional: notifications nếu cần demo thông báo
Do not add: full manuscript/page version/task review/audit system yet
```

## 12. Trạng thái hiện tại sau khi áp dụng

Các recommendation quan trọng đã được áp dụng vào cả 2 file schema:

- `schema.sql`
- `manga_database/manga_database/schema.sql`

Đã áp dụng:

- `users.role_id` thành `NOT NULL`
- `series.status` có `REVISION_REQUESTED`
- `tasks.status` có `REVISION_REQUESTED`
- `tasks.feedback_notes`
- `submissions.submitted_at`
- `skills` seed dữ liệu mặc định
- `permissions` và `role_permissions` seed dữ liệu cơ bản
- `created_at` / `updated_at` cho các bảng chính
- trigger `updated_at` cho các bảng phù hợp
- `resolved` cho `annotations`
- trạng thái task/chapter/page rõ hơn để dễ làm MVP và demo

Các phần chưa đưa vào vì scope MVP:

- `audit_logs`
- `notifications`
- `page_versions`
- `task_reviews`
- `task_submissions`
- `series_rankings` chi tiết
- `earning_rates`
- `editorial_boards` / `board_memberships`
```
