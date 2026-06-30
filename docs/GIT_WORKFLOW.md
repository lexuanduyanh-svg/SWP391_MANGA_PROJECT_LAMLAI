# Git Workflow

## 1. Repository

```text
https://github.com/lexuanduyanh-svg/SWP391_MANGA_PROJECT_LAMLAI
```

## 2. Branch model de xuat

```text
main        : code on dinh de demo/nop bai
develop     : code tong hop truoc khi len main
feature/*   : branch tung thanh vien/tung module
```

Neu nhom muon don gian hon, co the dung `main` + `feature/*`, nhung van khong nen tat ca cung push vao `main`.

## 3. Branch cho tung nguoi

```text
feature/backend-auth-admin-proposal
feature/backend-review-production-assistant
feature/database-persistence
feature/frontend-workspaces
feature/docs-demo-plan
```

## 4. Quy trinh lam viec hang ngay

```bash
git checkout main
git pull origin main
git checkout -b feature/ten-branch
# code
git status
git diff
git add <file can commit>
git commit -m "feat: mo ta ngan gon"
git push -u origin feature/ten-branch
```

## 5. Truoc khi merge

Backend neu co sua Java:

```bash
cd backend
mvnw.cmd test
```

Frontend neu co sua React/CSS/TS:

```bash
cd frontend
npm run build
```

Sau do:

- Xem diff.
- Kiem tra khong co secret/local file.
- Chay demo flow lien quan.
- Tao pull request hoac bao leader merge.

## 6. Commit message goi y

```text
feat: add proposal submit api
fix: persist board votes
style: polish assistant dashboard
docs: update demo script
refactor: split mangaka proposal form
```

## 7. Xu ly conflict

- Doc conflict ky, khong bam accept all neu chua hieu.
- Neu conflict o file shared/API/status, hoi owner module lien quan.
- Sau khi resolve conflict phai build/test lai.


