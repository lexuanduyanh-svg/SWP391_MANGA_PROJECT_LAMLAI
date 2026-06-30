# Team Task Assignment - SWP391 Manga Project Lam Lai

> Cap nhat 2026-06-27: Scope restored: quay lai ke hoach nhom 5 nguoi, truoc khi giam scope.
> Giu full Flow 2: AI summary preview, region drawing / VisualCanvas, annotations, rankings/reader metrics.

## 1. Team theo scope restore

Nhom quay lai layout ban dau 5 nguoi:

| Vi tri | So luong |
|--------|---------|
| Backend developer | 2 |
| Frontend developer | 1 |
| Database / persistence | 1 |
| PM/BA/QA/Docs/Integration | 1 |

**Flow 1 (Proposal workflow) da hoan thanh** ✅

Con lai: Flow 2 (Production workflow) theo ban full truoc khi giam scope.

## 2. Scope full theo ke hoach ban dau

### Giu lai trong scope

| Tinh nang | Ly do giu |
|-----------|-----------|
| AI Summary preview (`/preview-upload` endpoint, `api_bridge.py`) | Thuoc V1 change request va huong AI/RBL cua de tai |
| Region drawing (VisualCanvas, pixel-level selection) | La diem chinh cua Flow 2: task gan theo region tren page |
| Annotations (Editor markup pins tren manuscript/page) | Ho tro Tantou review va feedback truc tiep |
| Rankings screen (`reader_metrics` UI + logic) | Ho tro Flow 3 va quyet dinh serialization |

### Khong con ap dung ban giam scope 3 nguoi

| Noi dung cua ban giam scope | Trang thai hien tai |
|---------------------------|----------------|
| Task gan thang vao page, khong co region | Bo, quay lai chapter -> page -> region -> task |
| `region_coordinates = null` / full-page only | Bo, region la entity rieng (x, y, w, h) |
| Chi JS validation type/size, bo AI preview | Bo, giu AI summary preview trong scope |
| Bo annotations/rankings | Bo, giu annotations/rankings trong scope |

### Flow 2 bat buoc

- Manuscript upload (Mangaka upload file proposal)
- AI summary preview truoc upload/submission
- Chapter -> Page -> Region -> Task flow
- Region drawing / VisualCanvas
- Assistant start/submit task
- Mangaka approve/redo task
- Editor annotations
- Reader metrics / rankings
- Earnings estimate tu approved tasks

## 3. Phan cong 5 nguoi — Flow 2

### Backend Dev 1 — Production Service (Mangaka side)

**Phu trach:**
- `InMemoryMangakaProductionService.java` — hoan thien CRUD:
  - Tao chapter (title, chapter_number)
  - Tao page (page_number, manuscript_file_path optional)
  - Tao task tren page (description, deadline, assign assistant_id)
  - Can region logic - task gan theo vung tren page
- Unit tests cho production service
- Ket noi PostgreSQL cho production data

**API so huu:**

```text
GET  /api/mangaka/proposals/{proposalId}/chapters
POST /api/mangaka/proposals/{proposalId}/chapters
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks
PUT  /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/approve
PUT  /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/redo
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

## 4. Lich lam viec

| Tuan | Backend Dev 1 | Backend Dev 2 | Database/QA | Frontend Dev | PM/BA/Docs |
|------|--------------|---------------|-------------|--------------|------------|
| **Tuan 1** | Chapter/page/region/task CRUD | Assistant task start/submit | Schema + seed Flow 2 | Production UI + VisualCanvas base | Update scope/API docs |
| **Tuan 2** | Task review + earnings rules | Submission/version flow | Integration tests + data checks | Assistant board + annotations UI | Test cases + demo checklist |
| **Tuan 3** | Bug fix + API polish | Newman/API smoke tests | Regression test + DB review | Ranking/reader metrics UI polish | Demo script + teacher checkpoint |

## 5. Flow demo sau khi restore scope

```text
Flow 1 (DONE ✅):
Mangaka tao proposal + upload manuscript
-> Submit cho Tantou Editor
-> Tantou request revision hoac forward Board
-> 3 Board members vote
-> He thong auto Approved/Rejected

Flow 2 (scope full):
Proposal APPROVED
-> Mangaka tao chapter (title, so chuong)
-> Mangaka tao page (so trang, upload anh page neu co)
-> Mangaka ve/chon region tren page bang VisualCanvas
-> Mangaka tao task tren region + assign cho Assistant
-> Assistant start task
-> Assistant submit (upload file + note)
-> Mangaka approve (APPROVED) hoac request redo (REVISION_REQUESTED)
-> Editor/Board xem reader metrics, rankings neu demo Flow 3
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
