# AI Assistant Context

> File nay dung de lan sau AI/nhom mo project len biet dang lam o dau, rule nao can theo.

## 1. Project moi

```text
Local path:
C:\Users\AD\OneDrive\May tinh\Giao trinh FPT\KY5\SWP391_NEW

GitHub:
https://github.com/lexuanduyanh-svg/SWP391_MANGA_PROJECT_LAMLAI
```

## 2. Nguon ban dau

Project moi duoc tao tu ban demo da chay duoc o clone cu:

```text
C:\Users\AD\OneDrive\May tinh\Giao trinh FPT\KY5\SWP391_clone\SWP391_Manga_Project
```

Tinh than ban moi:

- Dung ban demo cu lam baseline.
- Tu day tro di lam lai theo module de nhom 5 nguoi co the hoc va code rieng.
- Khong tiep tuc code mot cuc lon kho hieu.
- Uu tien tach module, tai lieu hoa API, database va task owner.

## 3. Rule AI/nguoi code phai theo

- Lam trong `SWP391_NEW`.
- Khong sua project cu neu user khong yeu cau.
- Khong commit/push file local/secret/build output.
- Neu thay doi code quan trong, cap nhat docs lien quan.
- Neu doi API, cap nhat `docs/API_CONTRACT.md`.
- Neu doi DB/schema, cap nhat `docs/DATABASE_DESIGN.md` hoac schema SQL.
- Neu doi task/team scope, cap nhat `docs/TEAM_TASK_ASSIGNMENT.md`.
- V1 moi co them yeu cau: validate file dau vao cua Mangaka va AI summary preview truoc upload.
- Moi thay doi lon nen co checkpoint giao vien va ghi feedback vao docs.
- Truoc khi bao xong nen chay test/build lien quan.

## 4. Tai lieu can doc truoc khi code

```text
docs/TEAM_TASK_ASSIGNMENT.md
docs/PROJECT_RULES.md
docs/API_CONTRACT.md
docs/GIT_WORKFLOW.md
docs/DEMO_SCRIPT.md
docs/TEST_CASES.md
README.md
```

## 5. Huong chia module muc tieu

Frontend muc tieu:

```text
src/frontend/src/features/auth/
src/frontend/src/features/admin/
src/frontend/src/features/proposal-authoring/
src/frontend/src/features/editor-review/
src/frontend/src/features/board-review/
src/frontend/src/features/production/
src/frontend/src/features/assistant-tasks/
src/frontend/src/shared/
```

Backend muc tieu:

```text
identity/
proposal/
review/
production/
shared/
```

## 6. Accounts demo

```text
admin@manga.local / Admin@123
mangaka@manga.local / Mangaka@123
assistant@manga.local / Assistant@123
editor@manga.local / Editor@123
board@manga.local / Board@123
board2@manga.local / Board2@123
board3@manga.local / Board3@123
```
