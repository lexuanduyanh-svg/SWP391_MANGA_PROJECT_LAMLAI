# Deployment Guide — SWP391 Manga Workflow API

## Trạng thái hiện tại

| Thành phần | Trạng thái | URL / Host |
|---|---|---|
| **Database** | ✅ Live | Supabase PostgreSQL (cloud) |
| **Backend API** | ✅ Live | `https://swp391-manga-api.onrender.com` |
| **Frontend** | ⏳ Chưa deploy | — |

---

## Thông tin cho Frontend Developer

### API Base URL

```
https://swp391-manga-api.onrender.com
```

### Cách cấu hình Frontend

Trong project frontend (Vite + React), tạo file `.env.production`:

```env
VITE_API_BASE_URL=https://swp391-manga-api.onrender.com
```

Hoặc set biến môi trường trên hosting (Vercel/Netlify):

| Key | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://swp391-manga-api.onrender.com` |

### API Endpoints (giữ nguyên)

Tất cả endpoint đều bắt đầu bằng `/api/...`:

```
POST   /api/auth/login
GET    /api/admin/accounts
POST   /api/mangaka/proposals
GET    /api/mangaka/proposals
PUT    /api/mangaka/proposals/{id}
POST   /api/mangaka/proposals/{id}/submit
POST   /api/mangaka/proposals/{id}/manuscript
GET    /api/mangaka/proposals/{id}/manuscript
POST   /api/tantou-editor/proposals/{id}/forward-to-board
POST   /api/tantou-editor/proposals/{id}/request-revision
POST   /api/editorial-board/proposals/{id}/vote
GET    /api/mangaka/production/series
POST   /api/mangaka/production/series/{id}/chapters
...
```

Chi tiết: xem `docs/API_CONTRACT.md`.

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

### Lưu ý quan trọng

1. **Cold start:** Backend dùng Render Free tier — nếu không ai truy cập 15 phút, server "ngủ". Lần request đầu sau đó sẽ chậm ~50 giây để server khởi động lại. Các request sau chạy bình thường.

2. **CORS:** Backend hiện cho phép origin `http://localhost:5173`. Khi frontend deploy lên Vercel/Netlify, **báo Backend team URL frontend** (ví dụ `https://swp391-frontend.vercel.app`) để cập nhật CORS.

3. **File upload:** Manuscript upload hoạt động nhưng file lưu trên Render ephemeral filesystem — sẽ mất khi server restart. Đủ cho demo, không phải production storage.

---

## Thông tin kỹ thuật Deploy

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

### Environment Variables (trên Render)

| Key | Mô tả |
|---|---|
| `DB_URL` | JDBC connection string tới Supabase PostgreSQL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password (KHÔNG commit vào code) |
| `CORS_ORIGINS` | Danh sách origin cho phép gọi API (comma-separated) |

### Deploy Frontend lên Vercel (khi sẵn sàng)

1. Import GitHub repo vào Vercel
2. Set **Root Directory** = `frontend` (hoặc `Build as requested` tùy thư mục)
3. Set **Framework Preset** = Vite
4. Thêm Environment Variable:
   - `VITE_API_BASE_URL` = `https://swp391-manga-api.onrender.com`
5. Deploy → lấy URL Vercel
6. Báo Backend team URL đó để cập nhật `CORS_ORIGINS` trên Render

---

## Cách build/test locally vẫn hoạt động

```bash
# Backend (local PostgreSQL hoặc H2)
cd backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local

# Frontend (trỏ về localhost)
cd frontend
npm run dev
```

Mặc định frontend fallback `http://localhost:8080` nếu không set `VITE_API_BASE_URL`.

---

## File deploy đã thêm vào repo

```
backend/Dockerfile          — Multi-stage Docker build cho Render
backend/.dockerignore       — Exclude target/ và storage
backend/Procfile            — Backup deploy option (non-Docker)
frontend/vercel.json        — SPA rewrite config cho Vercel
```
