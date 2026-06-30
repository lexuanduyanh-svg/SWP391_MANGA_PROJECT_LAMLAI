# Flow Overview — Status Machines

## Flow 1: Proposal (Pitch / Phê duyệt ý tưởng)

**Actors:** Mangaka → Editor → Editorial Board

### Status machine

```
                    ┌─────────────────────────┐
                    │         DRAFT           │  ← Mangaka tạo proposal mới
                    └──────────┬──────────────┘
                               │ Mangaka gửi (submit)
                               ▼
          ┌─────────────────────────────┐
          │    SUBMITTED_TO_EDITOR      │  ← Editor được thông báo
          └──────────────┬──────────────┘
                         │ Editor review
                         ▼
           ┌──────────────────────┐
           │  UNDER_BOARD_REVIEW  │  ← Editor thấy ổn, chuyển lên Board
           └──────────────────────┘
                    │
          ┌─────────┴──────────┐
          ▼                    ▼
   ┌──────────────┐    ┌──────────────────┐
   │   APPROVED   │    │    REJECTED      │
   │              │    │                  │
   │ → tạo Series │    │ → kết thúc       │
   └──────────────┘    └──────────────────┘
```

**Revision loop:**

```
 SUBMITTED_TO_EDITOR
      │ Editor yêu cầu sửa
      ▼
 REVISION_REQUESTED  ← Mangaka sửa lại
      │ Mangaka gửi lại
      ▼
 SUBMITTED_TO_EDITOR (quay lại review)
```

### Transition table

| From | Action | To | Actor | Guard |
|---|---|---|---|---|
| `DRAFT` | submit | `SUBMITTED_TO_EDITOR` | Mangaka | nội dung đầy đủ |
| `SUBMITTED_TO_EDITOR` | send to board | `UNDER_BOARD_REVIEW` | Editor | Editor đồng ý |
| `SUBMITTED_TO_EDITOR` | request revision | `REVISION_REQUESTED` | Editor | có feedback |
| `REVISION_REQUESTED` | resubmit | `SUBMITTED_TO_EDITOR` | Mangaka | đã sửa theo feedback |
| `UNDER_BOARD_REVIEW` | approve | `APPROVED` | Board | vote ≥ threshold |
| `UNDER_BOARD_REVIEW` | reject | `REJECTED` | Board | vote < threshold |

---

## Flow 2: Production (Sản xuất chapter)

**Actors:** Mangaka → Assistant → Editor

### Hierarchy

```
Series (ACTIVE / COMPLETED / CANCELLED)
 └── Chapter (DRAFT / IN_PROGRESS / COMPLETED)
      └── Page (DRAFT / IN_TASK / DONE)
           └── Region (coordinates x,y,w,h)
                └── Task (ASSIGNED → PENDING_REVIEW → APPROVED / REVISION_REQUESTED)
```

### Full status machine

```
  ┌─────────────────────────────────────────────────────────────┐
  │                          SERIES                             │
  │                                                             │
  │  (tạo từ proposal APPROVED)                                 │
  │       │                                                     │
  │       ▼                                                     │
  │   ACTIVE ───────────────────────────► COMPLETED             │
  │       │                              (tất cả chapter done)  │
  │       │                                                     │
  │       ├──► CANCELLED (Board quyết định hủy)                 │
  │                                                             │
  │  ┌────────────────────────────────────────────────────┐     │
  │  │                    CHAPTER                         │     │
  │  │                                                    │     │
  │  │   DRAFT ───────► IN_PROGRESS ──────► COMPLETED     │     │
  │  │   (tạo)     upload page đầu    all tasks Approved  │     │
  │  │                                                    │     │
  │  │          ◄────── REJECTED (Editor reject)          │     │
  │  │                                                    │     │
  │  │  ┌─────────────────────────────────────────────┐   │     │
  │  │  │                  PAGE                        │   │     │
  │  │  │                                              │   │     │
  │  │  │   DRAFT ──────────► IN_TASK ───────► DONE    │   │     │
  │  │  │   (upload)   assign task    all tasks done   │   │     │
  │  │  │                                              │   │     │
  │  │  │    ┌────────────────────────────────┐        │   │     │
  │  │  │    │           TASK                  │        │   │     │
  │  │  │    │                                  │        │   │     │
  │  │  │    │  ASSIGNED ──► IN_PROGRESS        │        │   │     │
  │  │  │    │     │           │                │        │   │     │
  │  │  │    │     │           ▼                │        │   │     │
  │  │  │    │     │      SUBMITTED             │        │   │     │
  │  │  │    │     │           │                │        │   │     │
  │  │  │    │     │     ┌─────┴──────┐         │        │   │     │
  │  │  │    │     │     ▼            ▼         │        │   │     │
  │  │  │    │  APPROVED      REVISION_         │        │   │     │
  │  │  │    │                 REQUESTED        │        │   │     │
  │  │  │    │                     │            │        │   │     │
  │  │  │    │                     ▼            │        │   │     │
  │  │  │    │                ASSIGNED (redo)   │        │   │     │
  │  │  │    └──────────────────────────────────┘        │   │     │
  │  │  └─────────────────────────────────────────────┘   │     │
  │  └────────────────────────────────────────────────────┘     │
  └─────────────────────────────────────────────────────────────┘
```

### 2a. Series status

| From | Action | To | Actor | Guard |
|---|---|---|---|---|
| *(khi proposal APPROVED)* | khởi tạo | `ACTIVE` | Hệ thống | proposal được duyệt |
| `ACTIVE` | hoàn tất | `COMPLETED` | Hệ thống | tất cả chapter COMPLETED |
| `ACTIVE` | hủy | `CANCELLED` | Board | có quyết định CANCEL |

### 2b. Chapter status

| From | Action | To | Actor | Guard |
|---|---|---|---|---|
| *(khi proposal APPROVED)* | khởi tạo | `DRAFT` | Hệ thống | series ACTIVE |
| `DRAFT` | upload page đầu tiên | `IN_PROGRESS` | Mangaka | upload file ảnh page |
| `IN_PROGRESS` | nhấn "Publish" / completeChapter | `COMPLETED` | Mangaka | tất cả task các page đều APPROVED |
| `IN_PROGRESS` | editor reject chapter | `IN_PROGRESS` | Editor | (giữ nguyên, chỉ reject chapter để thông báo) |

### 2c. Page status

| From | Action | To | Actor | Guard |
|---|---|---|---|---|
| *(khi upload)* | khởi tạo | `DRAFT` | Mangaka | upload file ảnh |
| `DRAFT` | assign task đầu tiên | `IN_TASK` | Mangaka | chọn region + ghi instructions + chọn assistant |
| `IN_TASK` | tất cả task được APPROVED | `DONE` | Hệ thống | mọi task trên page đều APPROVED |

### 2d. Task status

| From | Action | To | Actor | Guard |
|---|---|---|---|---|
| *(khi Mangaka assign)* | khởi tạo | `ASSIGNED` | Mangaka | chọn assistant, ghi instructions, set deadline |
| `ASSIGNED` | bắt đầu làm | `IN_PROGRESS` | Assistant | task được nhận |
| `IN_PROGRESS` | nộp bài | `SUBMITTED` | Assistant | upload file kết quả |
| `SUBMITTED` | duyệt | `APPROVED` | Mangaka | kết quả OK |
| `SUBMITTED` | yêu cầu làm lại | `REVISION_REQUESTED` | Mangaka | có feedback sửa |
| `REVISION_REQUESTED` | làm lại và nộp | `SUBMITTED` | Assistant | đã sửa theo feedback |

---

## End-to-End Example (Từ lúc tạo proposal đến chapter hoàn tất)

```
Mangaka tạo Proposal ──► DRAFT
  ↓ gửi
SUBMITTED_TO_EDITOR
  ↓ Editor gửi lên Board
UNDER_BOARD_REVIEW
  ↓ Board vote đậu
APPROVED ──► Hệ thống tạo Series (ACTIVE) + Chapter đầu (DRAFT)
  ↓ Mangaka upload ảnh page 1, 2, 3
Chapter IN_PROGRESS
  ↓ Page 1: Mangaka vẽ region, gán task "Tô đen vùng A" cho Assistant X
Page 1 IN_TASK
  ↓ Assistant X nhận task, làm, nộp
Task SUBMITTED
  ↓ Mangaka duyệt OK
Task APPROVED → Page 1 DONE
  ↓ (tương tự Page 2, 3 đều DONE)
Mangaka nhấn "Publish" → Chapter COMPLETED
  ↓ (nếu chapter cuối cùng)
Series COMPLETED
```
