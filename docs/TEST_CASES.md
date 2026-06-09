# Test Cases

## 1. Smoke test bat buoc

| ID | Flow | Steps | Expected | Owner |
|---|---|---|---|---|
| TC-001 | Login Admin | Login `admin@manga.local` | Vao Admin dashboard | Member 1, 4 |
| TC-002 | Login Mangaka | Login `mangaka@manga.local` | Vao Mangaka dashboard | Member 1, 4 |
| TC-003 | Manuscript validation | Chon file sai dinh dang hoac vuot size | File bi chan, hien loi ro rang | Member 1, 4 |
| TC-004 | AI summary preview | Chon file hop le truoc upload | Hien summary noi dung ngan gon de confirm | Member 1, 4, 5 |
| TC-005 | Proposal submit | Mangaka tao proposal + upload + submit | Status sang SubmittedToEditor | Member 1 |
| TC-006 | Tantou revision | Editor request revision | Mangaka thay NeedsRevision | Member 2 |
| TC-007 | Revision resubmit | Mangaka update file + resubmit | Tantou thay proposal lai | Member 1, 2 |
| TC-008 | Forward Board | Editor forward Board | Board thay UnderBoardReview | Member 2 |
| TC-009 | Board vote | board/board2/board3 vote | Auto Approved/Rejected | Member 2, 3 |
| TC-010 | Production task | Mangaka tao chapter/page/region/task | Task Pending | Member 2, 3 |
| TC-011 | Assistant submit | Assistant start + submit | Task Submitted | Member 2, 4 |
| TC-012 | Mangaka review task | Mangaka approve/redo | Task Completed hoac RedoRequested | Member 2 |

## 2. Build/test command

Backend:

```bash
cd src/backend
mvnw.cmd test
```

Frontend:

```bash
cd src/frontend
npm run build
```

## 3. Bug report template

```text
Title:
Environment:
Account:
Steps:
Expected:
Actual:
Screenshot/log:
Owner:
Priority:
```
