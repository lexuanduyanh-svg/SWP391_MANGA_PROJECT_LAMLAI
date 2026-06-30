# SWP391 Manga API â€” Danh sÃ¡ch API cho thÃ nh viÃªn

> TÃ i liá»‡u nÃ y liá»‡t kÃª **toÃ n bá»™ API tháº­t Ä‘ang cháº¡y** theo Ä‘Ãºng code backend (`com.mangastudio.workflow.controllers`).
> DÃ¹ng báº£n nÃ y Ä‘á»ƒ gá»i API, KHÃ”NG truy cáº­p tháº³ng link gá»‘c.

## 0. ThÃ´ng tin chung

| Má»¥c | GiÃ¡ trá»‹ |
|---|---|
| **Base URL (deploy)** | `https://swp391-manga-api.onrender.com` |
| **Base URL (local)** | `http://localhost:8080` |
| Server port | `8080` |
| Context path | (khÃ´ng cÃ³) |
| CORS cho phÃ©p | `http://localhost:5173` (Ä‘á»•i qua env `CORS_ORIGINS`), Ã¡p dá»¥ng cho `/api/**` |

> âš ï¸ Truy cáº­p tháº³ng `https://swp391-manga-api.onrender.com/` sáº½ ra **Whitelabel Error Page 404**.
> ÄÃ¢y lÃ  **bÃ¬nh thÆ°á»ng** â€” server váº«n cháº¡y, chá»‰ lÃ  khÃ´ng cÃ³ trang á»Ÿ path gá»‘c `/`.
> Pháº£i gá»i Ä‘Ãºng endpoint bÃªn dÆ°á»›i má»›i cÃ³ dá»¯ liá»‡u.

### CÃ¡ch test nhanh
```bash
# ÄÄƒng nháº­p (POST)
curl -X POST https://swp391-manga-api.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@manga.local","password":"Admin@123"}'

# Láº¥y danh sÃ¡ch tÃ i khoáº£n (GET) â€” má»Ÿ tháº³ng trÃªn trÃ¬nh duyá»‡t Ä‘Æ°á»£c
https://swp391-manga-api.onrender.com/admin/accounts
```

### TÃ i khoáº£n demo
```text
admin@manga.local     / Admin@123
mangaka@manga.local   / Mangaka@123
assistant@manga.local / Assistant@123
editor@manga.local    / Editor@123
board@manga.local     / Board@123
```

---

## 1. ðŸ” Auth â€” `AuthController`

| Method | Endpoint | MÃ´ táº£ |
|---|---|---|
| POST | `/api/auth/login` | ÄÄƒng nháº­p, tráº£ vá» thÃ´ng tin user/role |

---

## 2. ðŸ‘¤ Admin â€” `AdminController` (lÆ°u Ã½: prefix lÃ  `/admin`, KHÃ”NG cÃ³ `/api`)

### Quáº£n lÃ½ tÃ i khoáº£n
| Method | Endpoint | MÃ´ táº£ |
|---|---|---|
| GET | `/admin/accounts` | Láº¥y danh sÃ¡ch tÃ i khoáº£n |
| POST | `/admin/accounts` | Táº¡o tÃ i khoáº£n má»›i |
| PUT | `/admin/accounts/{id}` | Cáº­p nháº­t tÃ i khoáº£n |
| PUT | `/admin/accounts/{id}/status` | Äá»•i tráº¡ng thÃ¡i (ACTIVE / INACTIVE) |
| PUT | `/admin/accounts/{id}/skills` | Cáº­p nháº­t ká»¹ nÄƒng cho tÃ i khoáº£n |
| DELETE | `/admin/accounts/{id}` | XÃ³a tÃ i khoáº£n |

### Quáº£n lÃ½ ká»¹ nÄƒng (skills)
| Method | Endpoint | MÃ´ táº£ |
|---|---|---|
| GET | `/admin/skills` | Láº¥y danh sÃ¡ch ká»¹ nÄƒng |
| POST | `/admin/skills` | Táº¡o ká»¹ nÄƒng má»›i |
| PUT | `/admin/skills/{id}` | Cáº­p nháº­t ká»¹ nÄƒng |
| PUT | `/admin/skills/{id}/status` | Äá»•i tráº¡ng thÃ¡i ká»¹ nÄƒng |
| DELETE | `/admin/skills/{id}` | XÃ³a ká»¹ nÄƒng |

---

## 3. ðŸ–Šï¸ Mangaka â€” Proposal â€” `MangakaController` (`/api/mangaka/proposals`)

| Method | Endpoint | MÃ´ táº£ |
|---|---|---|
| GET | `/api/mangaka/proposals?authorEmail={email}` | Danh sÃ¡ch proposal cá»§a mangaka |
| POST | `/api/mangaka/proposals` | Táº¡o proposal má»›i |
| PUT | `/api/mangaka/proposals/{id}` | Cáº­p nháº­t proposal |
| PUT | `/api/mangaka/proposals/{id}/submit` | Ná»™p proposal cho Editor |
| DELETE | `/api/mangaka/proposals/{id}?authorEmail={email}` | XÃ³a proposal |
| POST | `/api/mangaka/proposals/preview-upload` | Upload + validate + AI summary báº£n tháº£o |
| POST | `/api/mangaka/proposals/upload` | Upload file proposal |
| GET | `/api/mangaka/proposals/files/{fileName}` | Táº£i/xem file Ä‘Ã£ upload |

---

## 4. ðŸ“– Mangaka â€” Sáº£n xuáº¥t (Chapter / Page / Task) â€” `MangakaProductionController`

| Method | Endpoint | MÃ´ táº£ |
|---|---|---|
| GET | `/api/mangaka/proposals/{proposalId}/chapters` | Danh sÃ¡ch chapter |
| POST | `/api/mangaka/proposals/{proposalId}/chapters` | Táº¡o chapter má»›i |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages` | Táº¡o page trong chapter |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions` | Tao region tren page |
| POST | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks` | Giao task cho region |
| PUT | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/approve` | Duyá»‡t task |
| PUT | `/api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/redo` | YÃªu cáº§u lÃ m láº¡i task |

---

## 5. âœï¸ Editor (Tantou) â€” `EditorController` (`/api/editor/proposals`)

| Method | Endpoint | MÃ´ táº£ |
|---|---|---|
| GET | `/api/editor/proposals?editorEmail={email}` | Danh sÃ¡ch proposal cáº§n duyá»‡t |
| PUT | `/api/editor/proposals/{id}/forward-board` | Chuyá»ƒn lÃªn Ban giÃ¡m Ä‘á»‘c |
| PUT | `/api/editor/proposals/{id}/request-revision` | YÃªu cáº§u mangaka chá»‰nh sá»­a |
| PUT | `/api/editor/proposals/{id}/reject` | Tá»« chá»‘i proposal |

---

## 6. ðŸ›ï¸ Board (Ban giÃ¡m Ä‘á»‘c) â€” `BoardController` (`/api/board/proposals`)

| Method | Endpoint | MÃ´ táº£ |
|---|---|---|
| GET | `/api/board/proposals?memberEmail={email}` | Danh sÃ¡ch proposal chá» duyá»‡t |
| PUT | `/api/board/proposals/{id}/approve` | PhÃª duyá»‡t proposal |
| PUT | `/api/board/proposals/{id}/reject` | Tá»« chá»‘i proposal |

---

## 7. ðŸ¤ Assistant â€” `AssistantTaskController` (`/api/assistant/tasks`)

| Method | Endpoint | MÃ´ táº£ |
|---|---|---|
| GET | `/api/assistant/tasks?assistantEmail={email}` | Danh sÃ¡ch task Ä‘Æ°á»£c giao |
| PUT | `/api/assistant/tasks/{taskId}/start` | Báº¯t Ä‘áº§u lÃ m task |
| PUT | `/api/assistant/tasks/{taskId}/submit` | Ná»™p task hoÃ n thÃ nh |

---

## 8. Tráº¡ng thÃ¡i (status) dÃ¹ng trong há»‡ thá»‘ng

### Proposal / Series status
`DRAFT` â†’ `SUBMITTED_TO_EDITOR` â†’ `REVISION_REQUESTED` / `UNDER_BOARD_REVIEW` â†’ `APPROVED` / `REJECTED`

### Task status
`ASSIGNED` â†’ `PENDING_REVIEW` â†’ `APPROVED` / `REVISION_REQUESTED`

### User status
`ACTIVE`, `INACTIVE`

---

> ðŸ“Œ **LÆ°u Ã½ chÃªnh lá»‡ch path:**
> - NhÃ³m Admin dÃ¹ng prefix `/admin` (**khÃ´ng** cÃ³ `/api`).
> - CÃ¡c nhÃ³m cÃ²n láº¡i dÃ¹ng `/api/...`.
> - Flow 2 dùng cấp `regions`: `.../pages/{pageId}/regions/{regionId}/tasks`. Region được lưu bằng `tasks.region_coordinates` JSONB.


