# Team Task Assignment - SWP391 Manga Project Lam Lai

> Muc tieu: chia viec cho nhom 5 nguoi de moi nguoi hoc/code mot phan ro rang, van merge duoc ve mot project chung.

## 1. Nguyen tac chia viec

- Chia theo module chuc nang, khong de mot nguoi phai hieu toan bo code.
- Moi module co owner ro rang.
- Khong doi API/status/database schema neu chua bao nhom va cap nhat tai lieu.
- Truoc khi merge phai build/test phan lien quan.
- V1 nay co them scope nho hon: file validation cho Mangaka va AI summary preview truoc upload.
- Project moi lam tai: `C:\Users\AD\OneDrive\May tinh\Giao trinh FPT\KY5\SWP391_NEW`.
- GitHub moi: `https://github.com/lexuanduyanh-svg/SWP391_MANGA_PROJECT_LAMLAI`.
- Nhom can interact voi giao vien thuong xuyen: noi ro V1 scope, lay feedback som, va cap nhat tai lieu sau moi checkpoint.

## 2. Bang chia viec tong quan

| Thanh vien | Vai tro | Pham vi chinh | Ket qua can ban giao |
|---|---|---|---|
| Member 1 | Backend 1 | Auth, Admin, Mangaka Proposal | API login/admin/proposal chay on |
| Member 2 | Backend 2 | Tantou Review, Board Voting, Production, Assistant | API review/vote/task chay on |
| Member 3 | Database + Persistence | PostgreSQL, schema, entity, repository, seed data | Du lieu luu duoc va restart khong mat |
| Member 4 | Frontend | UI, dashboard theo role, form, API service | Demo UI chay duoc full flow |
| Member 5 | PM/BA/QA/Docs/Integration | Requirement, report, test case, demo script, merge checklist | Tai lieu + QA + dieu phoi merge |

## 3. Member 1 - Backend Auth/Admin/Proposal

### Phu trach

- Login theo role.
- Admin quan ly account.
- Admin quan ly skill/category.
- Mangaka tao/sua/xoa proposal.
- Upload/download manuscript.
- Ràng buộc file đầu vào cua Mangaka truoc upload: type/size/format hop le.
- Tao AI summary preview cho file duoc chon truoc khi upload/final submit.
- Submit proposal to Tantou.
- Update/resubmit revision khi Tantou yeu cau sua.
- Ghi lai summary metadata cho proposal de giao vien/nhom de demo.

### API so huu

```text
POST /api/auth/login

GET /api/admin/accounts
POST /api/admin/accounts
PUT /api/admin/accounts/{id}
DELETE /api/admin/accounts/{id}

GET /api/admin/skills
POST /api/admin/skills
PUT /api/admin/skills/{id}
DELETE /api/admin/skills/{id}

GET /api/mangaka/proposals
POST /api/mangaka/proposals
PUT /api/mangaka/proposals/{id}
DELETE /api/mangaka/proposals/{id}

POST /api/mangaka/proposals/upload
GET /api/mangaka/proposals/files/{fileName}

PUT /api/mangaka/proposals/{id}/submit
```

### Deliverable check

- Login duoc bang account demo.
- Admin tao/sua account va skill duoc.
- Mangaka upload file dung dinh dang va bi chan neu file sai.
- Mangaka thay AI summary preview cua file truoc khi submit.
- Mangaka tao proposal va upload file duoc.
- Mangaka submit proposal duoc.
- Mangaka resubmit revision duoc.
- Demo co checkpoint tra loi giao vien ve cach summary hoat dong.

### Branch goi y

```text
feature/backend-auth-admin-proposal
```

## 4. Member 2 - Backend Review/Production/Assistant

### Phu trach

- Tantou Editor xem proposal.
- Tantou request revision.
- Tantou forward proposal to Board.
- Editorial Board vote Approve/Reject.
- Tu dong quyet dinh Approved/Rejected khi du 3 phieu.
- Mangaka tao chapter/page/region/task.
- Assistant start/submit task.
- Mangaka approve/redo task.

### API so huu

```text
GET /api/editor/proposals
PUT /api/editor/proposals/{id}/request-revision
PUT /api/editor/proposals/{id}/forward-board

GET /api/board/proposals
PUT /api/board/proposals/{id}/approve
PUT /api/board/proposals/{id}/reject

GET /api/mangaka/proposals/{proposalId}/chapters
POST /api/mangaka/proposals/{proposalId}/chapters
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions
POST /api/mangaka/proposals/{proposalId}/chapters/{chapterId}/pages/{pageId}/regions/{regionId}/tasks

GET /api/assistant/tasks
PUT /api/assistant/tasks/{taskId}/start
PUT /api/assistant/tasks/{taskId}/submit

PUT /api/mangaka/tasks/{taskId}/approve
PUT /api/mangaka/tasks/{taskId}/redo
```

### Deliverable check

- Tantou thay proposal da submit.
- Tantou request revision/forward Board duoc.
- `board`, `board2`, `board3` vote duoc.
- Sau 3 vote, status tu chuyen Approved/Rejected.
- Mangaka tao task duoc.
- Assistant start/submit duoc.
- Mangaka approve/redo duoc.

### Branch goi y

```text
feature/backend-review-production-assistant
```

## 5. Member 3 - Database + Persistence

### Phu trach

- PostgreSQL schema.
- Entity JPA.
- Repository.
- Seed data demo.
- Cau hinh H2 local/PostgreSQL.
- Kiem tra du lieu sau moi flow.
- Dam bao restart backend khong mat du lieu khi dung DB profile.

### Bang du lieu chinh

```text
users
roles
permissions
role_permissions
skills
user_skills
assistant_profiles
series
board_votes
chapters
pages
tasks
submissions
annotations
reader_metrics
```

### File so huu chinh

```text
schema (1).sql
schema (1).sql
src/backend/src/main/resources/application.properties
src/backend/src/main/resources/application-local.properties
src/backend/src/main/resources/application-demo.properties
src/backend/src/main/java/com/mangaworkflow/persistence/entity/
src/backend/src/main/java/com/mangaworkflow/persistence/repository/
```

### Deliverable check

- Backend connect PostgreSQL duoc.
- Account demo co san.
- Proposal luu DB duoc.
- Board vote luu DB duoc.
- Task luu DB duoc.
- Restart backend khong mat data.

### Branch goi y

```text
feature/database-persistence
```

## 6. Member 4 - Frontend

### Phu trach

- Login page.
- Role dashboard routing.
- Admin dashboard.
- Mangaka dashboard.
- Tantou Editor dashboard.
- Editorial Board dashboard.
- Assistant dashboard.
- Goi API service.
- Hien thi loading/error/success.
- UI de demo cho giao vien.

### Folder muc tieu

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

### Deliverable check

- `npm run build` pass.
- Login vao tung role duoc.
- UI goi API dung contract.
- Demo full flow khong ket o UI.

### Branch goi y

```text
feature/frontend-workspaces
```

## 7. Member 5 - PM/BA/QA/Docs/Integration

### Phu trach

- Chot requirement.
- Viet SRS/report.
- Viet use case/activity/sequence neu can.
- Viet demo script.
- Viet test case.
- Quan ly GitHub issue/branch.
- Kiem tra truoc khi merge.
- Manual test full flow.
- Chuan bi slide thuyet trinh.
- Ghi bug va assign dung nguoi.

### File so huu chinh

```text
docs/API_CONTRACT.md
docs/DATABASE_DESIGN.md
docs/DEMO_SCRIPT.md
docs/TEST_CASES.md
docs/TEAM_TASK_ASSIGNMENT.md
docs/GIT_WORKFLOW.md
docs/PROJECT_RULES.md
README.md
```

### Deliverable check

- Co API contract moi nhat.
- Co database design moi nhat.
- Co test case.
- Co demo script.
- Co checklist truoc khi nop.

### Branch goi y

```text
feature/docs-demo-plan
```

## 8. Flow demo chinh

```text
Mangaka tao proposal + upload manuscript
-> Submit to Tantou
-> Tantou request revision hoac forward Board
-> Board 3 thanh vien vote
-> He thong auto Approved/Rejected
-> Neu Approved, Mangaka tao chapter/page/region/task
-> Assistant start/submit task
-> Mangaka approve hoac redo task
```

## 9. Ai chiu trach nhiem khi co loi

| Loi | Assign |
|---|---|
| Login/Admin/Proposal loi | Member 1 |
| Tantou/Board/Production/Assistant API loi | Member 2 |
| Data khong luu/restart mat data/schema loi | Member 3 |
| UI khong goi duoc API/hien thi sai | Member 4 |
| Requirement/test/demo/docs thieu | Member 5 |
