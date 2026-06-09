# Test Cases

## 1. Smoke test bat buoc

| ID | Flow | Steps | Expected | Owner |
|---|---|---|---|---|
| TC-001 | Login Admin | Login `admin@manga.local` | Vao Admin dashboard | Member 1, 4 |
| TC-002 | Login Mangaka | Login `mangaka@manga.local` | Vao Mangaka dashboard | Member 1, 4 |
| TC-003 | Proposal submit | Mangaka tao proposal + upload + submit | Status sang SubmittedToEditor | Member 1 |
| TC-004 | Tantou revision | Editor request revision | Mangaka thay NeedsRevision | Member 2 |
| TC-005 | Revision resubmit | Mangaka update file + resubmit | Tantou thay proposal lai | Member 1, 2 |
| TC-006 | Forward Board | Editor forward Board | Board thay UnderBoardReview | Member 2 |
| TC-007 | Board vote | board/board2/board3 vote | Auto Approved/Rejected | Member 2, 3 |
| TC-008 | Production task | Mangaka tao chapter/page/region/task | Task Pending | Member 2, 3 |
| TC-009 | Assistant submit | Assistant start + submit | Task Submitted | Member 2, 4 |
| TC-010 | Mangaka review task | Mangaka approve/redo | Task Completed hoac RedoRequested | Member 2 |

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
