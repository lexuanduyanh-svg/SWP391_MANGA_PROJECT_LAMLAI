# Team Task Assignment - SWP391 Manga Project Lam Lai

> Cap nhat 2026-06-27: Nhom con lai 3 nguoi (2 member dropped out).
> Scope da duoc giam de phu hop voi 3 nguoi / 3 tuan con lai.

## 1. Thay doi team

Nhom ban dau 5 nguoi, hien tai con 3 nguoi:

| Vi tri | So luong |
|--------|---------|
| Backend developer | 2 |
| Frontend developer | 1 |
| PM/BA/QA | 0 (be dam nhiem boi backend/frontend) |

**Flow 1 (Proposal workflow) da hoan thanh** ✅

Con lai: Flow 2 (Production workflow) — 3 tuan.

## 2. Scope giam (so voi ke hoach ban dau)

### Cat bo hoan toan

| Tinh nang | Ly do cat |
|-----------|-----------|
| ❌ AI Summary preview (`/preview-upload` endpoint, `api_bridge.py`) | Can Python service + AI model rieng, khong kha thi trong 3 tuan |
| ❌ Region drawing (VisualCanvas, pixel-level selection) | Canvas UI phuc tap, khong bat buoc demo |
| ❌ Annotations (Editor markup pins tren anh page) | Khong co trong demo script bat buoc |
| ❌ Rankings screen (`reader_metrics` UI + logic) | Stretch goal, khong core workflow |

### Don gian hoa

| Tinh nang | Truoc | Sau |
|-----------|-------|-----|
| Region | Ve vung pixel tren anh, luu JSONB toa do | Task gan thang vao page, `region_coordinates = null` |
| File validation | AI summary preview truoc upload | Chi JS validation type/size o frontend |
| Earnings | Tinh dong tu tasks approved | Hien thi so seed static tu DB |
| Submission | Upload file + AI annotation | Upload file + text note |

### Giu nguyen

- ✅ Manuscript upload (Mangaka upload file proposal)
- ✅ Submission file upload (Assistant submit file hoan thanh)
- ✅ Chapter → Page → Task flow
- ✅ Assistant start/submit task
- ✅ Mangaka approve/redo task

## 3. Phan cong 3 nguoi — Flow 2

### Backend Dev 1 — Production Service (Mangaka side)

**Phu trach:**
- `InMemoryMangakaProductionService.java` — hoan thien CRUD:
  - Tao chapter (title, chapter_number)
  - Tao page (page_number, manuscript_file_path optional)
  - Tao task tren page (description, payment, assign assistant_id)
  - Khong can region logic — task gan thang vao page
- Unit tests cho production service
- Ket noi PostgreSQL cho production data

**API so huu:**

```text
GET  /api/mangaka/proposals/{proposalId}/chapters
POST /api/mangaka/proposals/{proposalId}/chapters
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/tasks
PUT  /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/tasks/{taskId}/approve
PUT  /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/tasks/{taskId}/redo
```

**Deliverable:**
- Chapter/page/task CRUD hoat dong voi PostgreSQL
- Task approve/redo doi status dung
- Unit tests pass

**Branch:**
```text
feature/backend-production-mangaka
```

---

### Backend Dev 2 — Assistant Task Service + Integration

**Phu trach:**
- `AssistantTaskController.java` — hoan thien:
  - List tasks cua assistant (kem chapter/page context)
  - Start task (ASSIGNED → PENDING_REVIEW)
  - Submit task (upload file + note, chuyen sang PENDING_REVIEW)
- Task approve/redo (APPROVED / REVISION_REQUESTED)
- Seed data demo cho Flow 2
- Newman API test cho Flow 2
- Fix bugs integration BE1 + BE2

**API so huu:**

```text
GET /api/assistant/tasks?assistantEmail={email}
PUT /api/assistant/tasks/{taskId}/start
PUT /api/assistant/tasks/{taskId}/submit
```

**Deliverable:**
- Assistant nhan/start/submit task hoat dong
- File submission luu vao `storage-server/submissions/`
- Newman Flow 2 test pass
- Seed data du cho demo

**Branch:**
```text
feature/backend-assistant-tasks
```

---

### Frontend Dev — Production UI

**Phu trach:**
- `ProductionDashboard.tsx` — Mangaka sau khi proposal approved:
  - Danh sach chapter
  - Tao chapter → tao page → tao task → assign assistant
  - Xem task status, approve/redo
- `AssistantTaskBoard.tsx` — Assistant:
  - 3-column kanban: Pending → In Progress → Submitted
  - Start task, submit file + note
- Ket noi FE-BE cho Flow 2
- Polish UI cho demo

**Deliverable:**
- `npm run build` pass
- Mangaka tao duoc chapter/page/task va giao cho assistant
- Assistant start/submit duoc
- Mangaka approve/redo duoc
- UI khong bi ket o bat ky buoc nao

**Branch:**
```text
feature/frontend-production-ui
```

---

## 4. Lich 3 tuan

| Tuan | Backend Dev 1 | Backend Dev 2 | Frontend Dev |
|------|--------------|---------------|-------------|
| **Tuan 1** | Complete `MangakaProductionService` (chapter/page/task CRUD, khong region) | Complete `AssistantTaskController` (start/submit voi file+note) | `ProductionDashboard`: Mangaka tao chapter/page/task, assign assistant |
| **Tuan 2** | Unit tests production + PostgreSQL integration | Task approve/redo + tests + seed data | `AssistantTaskBoard`: start/submit UI, polish kanban |
| **Tuan 3** | Fix bugs, Newman API test Flow 2 | Fix bugs, endpoint polish | FE-BE integration, polish, demo prep |

## 5. Flow demo sau khi giam scope

```text
Flow 1 (DONE ✅):
Mangaka tao proposal + upload manuscript
-> Submit cho Tantou Editor
-> Tantou request revision hoac forward Board
-> 3 Board members vote
-> He thong auto Approved/Rejected

Flow 2 (Can implement trong 3 tuan):
Proposal APPROVED
-> Mangaka tao chapter (title, so chuong)
-> Mangaka tao page (so trang)
-> Mangaka tao task tren page + assign cho Assistant
   (Khong ve region, task = toan bo trang)
-> Assistant start task
-> Assistant submit (upload file + note)
-> Mangaka approve (APPROVED) hoac request redo (REVISION_REQUESTED)
```

## 6. Ai chiu trach nhiem khi co loi

| Loi | Assign |
|-----|--------|
| Login/Admin/Proposal/Auth loi | Backend Dev 1 (Flow 1 da done) |
| Tantou/Board API loi | Backend Dev 1 |
| Production chapter/page/task API loi | Backend Dev 1 |
| Assistant start/submit API loi | Backend Dev 2 |
| Data khong luu/mat sau restart | Backend Dev 2 |
| UI production dashboard loi | Frontend Dev |
| UI assistant board loi | Frontend Dev |
| UI khong goi duoc API | Frontend Dev |

## 7. Nguyen tac giu nguyen

- Khong doi API/status/database schema neu chua bao nhau.
- Truoc khi merge phai build/test phan lien quan.
- Khong commit secret, local DB file, node_modules, target, dist, uploads, logs.
- Khong commit/push khi chua co yeu cau ro rang.
- Canonical schema: `database/schema.sql`.
- Canonical backend package: `com.mangastudio.workflow`.
