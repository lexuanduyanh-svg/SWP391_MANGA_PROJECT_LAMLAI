# Deployment Guide â€” SWP391 Manga Workflow API

## Tráº¡ng thÃ¡i hiá»‡n táº¡i

| ThÃ nh pháº§n | Tráº¡ng thÃ¡i | URL / Host |
|---|---|---|
| **Database** | âœ… Live | Supabase PostgreSQL (cloud) |
| **Backend API** | âœ… Live | `https://swp391-manga-api.onrender.com` |
| **Frontend** | â³ ChÆ°a deploy | â€” |

---

## ThÃ´ng tin cho Frontend Developer

### API Base URL

```
https://swp391-manga-api.onrender.com
```

### CÃ¡ch cáº¥u hÃ¬nh Frontend

Trong project frontend (Vite + React), táº¡o file `.env.production`:

```env
VITE_API_BASE_URL=https://swp391-manga-api.onrender.com
```

Hoáº·c set biáº¿n mÃ´i trÆ°á»ng trÃªn hosting (Vercel/Netlify):

| Key | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://swp391-manga-api.onrender.com` |

### API Endpoints hiá»‡n táº¡i

> LÆ°u Ã½: Admin dÃ¹ng prefix `/admin` (**khÃ´ng cÃ³ `/api`**). CÃ¡c nhÃ³m cÃ²n láº¡i dÃ¹ng `/api/...`.

```text
# Auth
POST   /api/auth/login

# Admin
GET    /admin/accounts
POST   /admin/accounts
PUT    /admin/accounts/{id}
PUT    /admin/accounts/{id}/status
PUT    /admin/accounts/{id}/skills
DELETE /admin/accounts/{id}
GET    /admin/skills
POST   /admin/skills
PUT    /admin/skills/{id}
PUT    /admin/skills/{id}/status
DELETE /admin/skills/{id}

# Mangaka - Proposal / Flow 1
GET    /api/mangaka/proposals?authorEmail={email}
POST   /api/mangaka/proposals
PUT    /api/mangaka/proposals/{id}
PUT    /api/mangaka/proposals/{id}/submit
DELETE /api/mangaka/proposals/{id}?authorEmail={email}
POST   /api/mangaka/proposals/preview-upload
POST   /api/mangaka/proposals/upload
GET    /api/mangaka/proposals/files/{fileName}

# Editor / Board - Flow 1
GET    /api/editor/proposals?editorEmail={email}
PUT    /api/editor/proposals/{id}/forward-board
PUT    /api/editor/proposals/{id}/request-revision
PUT    /api/editor/proposals/{id}/reject
GET    /api/board/proposals?memberEmail={email}
PUT    /api/board/proposals/{id}/approve
PUT    /api/board/proposals/{id}/reject

# Production / Assistant - Flow 2
GET    /api/mangaka/proposals/{proposalId}/chapters
POST   /api/mangaka/proposals/{proposalId}/chapters
POST   /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages
POST   /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions
POST   /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks
PUT    /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/approve
PUT    /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks/{taskId}/redo
GET    /api/assistant/tasks?assistantEmail={email}
PUT    /api/assistant/tasks/{taskId}/start
PUT    /api/assistant/tasks/{taskId}/submit
```

Chi tiáº¿t Ä‘áº§y Ä‘á»§: xem `docs/API_LIST.md` vÃ  `docs/API_CONTRACT.md`.

### Demo Accounts (seed data)

```
admin@manga.local / Admin@123
mangaka@manga.local / Mangaka@123
assistant@manga.local / Assistant@123
editor@manga.local / Editor@123
board@manga.local / Board@123
board2@manga.local / Board2@123
board3@manga.local / Board3@123
```

### LÆ°u Ã½ quan trá»ng

1. **Cold start:** Backend dÃ¹ng Render Free tier â€” náº¿u khÃ´ng ai truy cáº­p 15 phÃºt, server "ngá»§". Láº§n request Ä‘áº§u sau Ä‘Ã³ sáº½ cháº­m ~50 giÃ¢y Ä‘á»ƒ server khá»Ÿi Ä‘á»™ng láº¡i. CÃ¡c request sau cháº¡y bÃ¬nh thÆ°á»ng.

2. **CORS:** Backend hiá»‡n cho phÃ©p origin `http://localhost:5173`. Khi frontend deploy lÃªn Vercel/Netlify, **bÃ¡o Backend team URL frontend** (vÃ­ dá»¥ `https://swp391-frontend.vercel.app`) Ä‘á»ƒ cáº­p nháº­t CORS.

3. **File upload:** Manuscript upload hoáº¡t Ä‘á»™ng nhÆ°ng file lÆ°u trÃªn Render ephemeral filesystem â€” sáº½ máº¥t khi server restart. Äá»§ cho demo, khÃ´ng pháº£i production storage.

---

## ThÃ´ng tin ká»¹ thuáº­t Deploy

### Hosting Stack

| Service | Provider | Tier |
|---|---|---|
| Database | Supabase (PostgreSQL) | Free |
| Backend | Render (Docker) | Free |
| Frontend | Vercel (planned) | Free |

### Backend Deploy Config

- **Runtime:** Docker (multi-stage build)
- **Root Directory:** `backend`
- **Dockerfile:** `backend/Dockerfile`
- **Port:** `8080` (Render maps to `PORT` env var)

### Environment Variables (trÃªn Render)

| Key | MÃ´ táº£ |
|---|---|
| `DB_URL` | JDBC connection string tá»›i Supabase PostgreSQL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password (KHÃ”NG commit vÃ o code) |
| `CORS_ORIGINS` | Danh sÃ¡ch origin cho phÃ©p gá»i API (comma-separated) |

### Deploy Frontend lÃªn Vercel (khi sáºµn sÃ ng)

1. Import GitHub repo vÃ o Vercel
2. Set **Root Directory** = `frontend` (hoáº·c `Build as requested` tÃ¹y thÆ° má»¥c)
3. Set **Framework Preset** = Vite
4. ThÃªm Environment Variable:
   - `VITE_API_BASE_URL` = `https://swp391-manga-api.onrender.com`
5. Deploy â†’ láº¥y URL Vercel
6. BÃ¡o Backend team URL Ä‘Ã³ Ä‘á»ƒ cáº­p nháº­t `CORS_ORIGINS` trÃªn Render

---

## CÃ¡ch build/test locally váº«n hoáº¡t Ä‘á»™ng

```bash
# Backend (local PostgreSQL hoáº·c H2)
cd backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local

# Frontend (trá» vá» localhost)
cd frontend
npm run dev
```

Máº·c Ä‘á»‹nh frontend fallback `http://localhost:8080` náº¿u khÃ´ng set `VITE_API_BASE_URL`.

---

## File deploy Ä‘Ã£ thÃªm vÃ o repo

```
backend/Dockerfile          â€” Multi-stage Docker build cho Render
backend/.dockerignore       â€” Exclude target/ vÃ  storage
backend/Procfile            â€” Backup deploy option (non-Docker)
frontend/vercel.json        â€” SPA rewrite config cho Vercel
```


