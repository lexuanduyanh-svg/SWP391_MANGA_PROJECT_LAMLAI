# Session Handoff - SWP391 Manga Project Lam Lai

## 1. Active project

```text
Local path:
C:\Users\AD\OneDrive\May tinh\Giao trinh FPT\KY5\SWP391_NEW

GitHub:
https://github.com/lexuanduyanh-svg/SWP391_MANGA_PROJECT_LAMLAI
```

This is now the active project workspace. Future work should happen here unless the user explicitly asks otherwise.

## 2. Source baseline

This project was initialized from the previous working demo project:

```text
C:\Users\AD\OneDrive\May tinh\Giao trinh FPT\KY5\SWP391_clone\SWP391_Manga_Project
```

The baseline already contains a runnable SWP391 demo:

- Spring Boot backend under `backend`.
- React/Vite frontend under `frontend`.
- Login by fixed demo roles.
- Admin account/skill management.
- Mangaka proposal create/update/upload/submit/revision.
- Tantou Editor review/download/revision/forward.
- Editorial Board 3-member voting and majority decision.
- Production chapter/page/region/task flow.
- Assistant task start/submit.
- Mangaka task approve/redo.

## 3. New direction

The previous code works as a demo but is too monolithic for a team to learn and maintain. The new direction is:

- Keep the working demo baseline.
- Gradually split by feature module.
- Let each team member own a clear part.
- Keep API/database/docs synchronized.
- Add V1 scope reduction: file validation for Mangaka and AI summary preview before upload.
- Keep more checkpoints with the teacher so scope can be adjusted early.
- Avoid one huge AI-generated code block that everyone must learn at once.

## 4. Required docs added for team workflow

```text
docs/AI_ASSISTANT_CONTEXT.md
docs/API_CONTRACT.md
docs/DEMO_SCRIPT.md
docs/GIT_WORKFLOW.md
docs/PROJECT_RULES.md
docs/TEAM_TASK_ASSIGNMENT.md
docs/TEST_CASES.md
```

Existing docs kept:

```text
docs/IMPLEMENTATION_GUIDE.md
docs/requirements/MVP_SCOPE_AND_BUSINESS_RULES.md
```

## 5. Team split

```text
Member 1: Backend Auth + Admin + Mangaka Proposal
Member 2: Backend Tantou Review + Board Voting + Production + Assistant
Member 3: Database + Persistence + PostgreSQL + Entity + Repository + Seed data
Member 4: Frontend UI + dashboard + API services
Member 5: PM/BA/QA/Docs/Integration/Demo
```

See `docs/TEAM_TASK_ASSIGNMENT.md` for detailed responsibilities.

## 6. Rules for future sessions

- Work inside `SWP391_NEW`.
- Do not edit old/original projects unless explicitly asked.
- If API changes, update `docs/API_CONTRACT.md`.
- If team responsibilities change, update `docs/TEAM_TASK_ASSIGNMENT.md`.
- If DB/schema changes, update database docs/schema.
- Before commit/push, inspect `git status`, `git diff`, and recent log.
- Do not commit secrets, local DB files, `node_modules`, `target`, or build outputs.

## 7. Demo accounts

```text
admin@manga.local / Admin@123
mangaka@manga.local / Mangaka@123
assistant@manga.local / Assistant@123
editor@manga.local / Editor@123
board@manga.local / Board@123
board2@manga.local / Board2@123
board3@manga.local / Board3@123
```
