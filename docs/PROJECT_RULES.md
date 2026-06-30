# Project Rules - SWP391 Manga Project Lam Lai

## 1. Muc tieu rule

File nay la rule lam viec chung de nhom 5 nguoi co the code rieng tung phan va merge lai khong vo project.

## 2. Nguyen tac bat buoc

1. Khong sua module cua nguoi khac neu chua bao.
2. Khong doi API endpoint/request/response neu chua cap nhat `docs/API_CONTRACT.md`.
3. Khong doi status enum tuy tien.
4. Khong commit file local/secret/build output.
5. Truoc khi merge phai build/test phan lien quan.
6. Moi loi phai assign dung owner theo `docs/TEAM_TASK_ASSIGNMENT.md`.
7. Neu chua chac, hoi nhom truoc khi refactor lon.

## 3. Git rules

- Lam viec tren branch rieng.
- Khong push truc tiep len `main` tru khi leader cho phep.
- Commit message ngan gon, ro y.
- Pull latest truoc khi bat dau code.
- Truoc khi merge phai xem lai diff.

Branch goi y:

```text
main
develop
feature/backend-auth-admin-proposal
feature/backend-review-production-assistant
feature/database-persistence
feature/frontend-workspaces
feature/docs-demo-plan
```

## 4. Backend rules

- Controller chi nen nhan request va tra response.
- Business logic nam trong service/application layer.
- Persistence logic nam trong repository/entity/adapter.
- Khong nhan password/token that vao source code.
- Neu them endpoint moi, cap nhat API contract.
- Neu them status moi, cap nhat ca backend enum, frontend type va docs.

## 5. Frontend rules

- Component khong nen goi `fetch` truc tiep lung tung.
- API call nen dat trong service rieng.
- UI phai co loading/error/success neu flow quan trong.
- Khong viet them CSS vao mot cuc neu co the tach theo feature.
- Khong tu doan response API; phai theo `API_CONTRACT.md`.

## 6. Database rules

- Moi bang phai co primary key.
- Quan he quan trong phai co foreign key neu dung PostgreSQL.
- Status nen co constraint/check neu co the.
- Co seed data cho demo account.
- DB owner phai review moi thay doi entity/repository/schema.

## 7. Files khong duoc commit

```text
node_modules/
dist/
target/
.env
.env.*
uploads/
*.db
*.mv.db
*.trace.db
logs/
```

## 8. Kiem tra truoc khi bao xong

Backend:

```bash
cd backend
mvnw.cmd test
```

Frontend:

```bash
cd frontend
npm run build
```

Manual smoke test:

```text
Login -> Mangaka submit proposal -> Tantou review -> Board vote -> Assistant task flow
```


