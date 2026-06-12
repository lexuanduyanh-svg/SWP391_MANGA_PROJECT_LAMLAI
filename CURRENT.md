# CURRENT - SWP391 Manga Project Lam Lai

Last updated: 2026-06-12

## 1. Active project

Local workspace:

```text
C:\Users\AD\OneDrive\Máy tính\Giao trình FPT\KY5\SWP391_NEW
```

GitHub remote:

```text
https://github.com/lexuanduyanh-svg/SWP391_MANGA_PROJECT_LAMLAI.git
```

Current local branch checked during this session:

```text
main
```

Note: GitHub page fetch returned 404 from this environment, but local git remote is configured to the repo above.

User-provided GitHub project page to check for assignments/tasks when accessible:

```text
https://github.com/lexuanduyanh-svg/SWP391_MANGA_PROJECT_LAMLAI
```

## 2. User direction for future work

The user clarified that this should be treated like a new project.

Use these files as the intended target design:

```text
form.txt     -> target project/folder architecture
database.txt -> target database design
```

Use old/current implemented files only as reference to understand the manga workflow and previous demo behavior, not as the final architecture/database standard.

Old implementation to treat as reference only:

```text
src/backend
src/frontend
docs/database/schema_postgresql_v2.sql
```

Important interpretation:

- `form.txt` is the only target architecture the user wants to follow.
- `database.txt` is the only target database model the user wants to follow.
- Use `form.txt` and `database.txt` as the main source of truth for new architecture and database.
- The old/current implemented code may still be used as reference for business flow, demo behavior, and feature ideas.
- Do NOT copy the old project's over-fragmented class structure or inconsistent module naming.
- The new project should be simpler and more unified: fewer, clearer classes per feature/module.
- The current person using this AI session is working on the **Member 1** assignment scope only.
- `Member 1` here means a **team task/ownership label**, not a user/account/role inside the application.
- Future sessions must check the GitHub project/repo above for the latest **Member 1** assignment before implementing, when GitHub is accessible.
- Only implement tasks belonging to **Member 1** unless the user explicitly asks otherwise.
- `Member 1` is not a package/class/module name in code.

## 3. Business flow understood from previous work

Main SWP391 manga workflow:

```text
Mangaka creates proposal + uploads manuscript
-> Mangaka submits proposal to Tantou Editor
-> Tantou Editor requests revision or forwards to Editorial Board
-> 3 Board members vote approve/reject
-> System auto-decides by majority
-> If approved, Mangaka creates chapter/page/region/task
-> Assistant starts/submits task
-> Mangaka approves or requests redo
```

Demo accounts from old baseline:

```text
admin@manga.local / Admin@123
mangaka@manga.local / Mangaka@123
assistant@manga.local / Assistant@123
editor@manga.local / Editor@123
board@manga.local / Board@123
board2@manga.local / Board2@123
board3@manga.local / Board3@123
```

## 4. Target architecture from form.txt

The user wants the project to follow this style:

```text
manga-workflow-system/
|
+-- backend/
|   +-- src/
|   |   +-- api/
|   |   +-- application/
|   |   +-- domain/
|   |   +-- infrastructure/
|   |   +-- config/
|   +-- tests/
|
+-- ai-subsystem/
|   +-- models/
|   +-- scripts/
|   +-- api_bridge.py
|
+-- frontend/
|   +-- src/
|   |   +-- components/
|   |   +-- views/
|
+-- storage-server/
|   +-- manuscripts/
|   +-- submissions/
|   +-- annotations/
|   +-- references/
|
+-- database/
|   +-- migrations/
|   +-- seeds/
|
+-- .env
+-- docker-compose.yml
+-- README.md
```

If implementing with Java Spring Boot, adapt the same layers conceptually:

```text
api            -> controllers
application    -> services/use cases
 domain         -> entities/dtos/core logic
infrastructure -> repositories/storage/external integrations
config         -> DB/server/security config
```

## 5. Target database from database.txt

The database design the user wants contains these tables:

### User & access management

```text
roles
permissions
role_permissions
users
assistant_profiles
skills
user_skills
```

### Content & serialization

```text
series
chapters
pages
```

### Production workflow

```text
tasks
submissions
annotations
```

### Strategy & metrics

```text
reader_metrics
board_votes
```

Important: this differs from the old implemented entities such as `accounts`, `manga_proposals`, `mangaka_chapters`, `mangaka_pages`, and `mangaka_production_tasks`.

## 6. Previous docs/team split from existing repo

Docs already available and useful:

```text
docs/AI_ASSISTANT_CONTEXT.md
docs/API_CONTRACT.md
docs/DEMO_SCRIPT.md
docs/GIT_WORKFLOW.md
docs/PROJECT_RULES.md
docs/TEAM_TASK_ASSIGNMENT.md
docs/TEST_CASES.md
docs/SESSION_HANDOFF.md
docs/IMPLEMENTATION_GUIDE.md
```

Previous 5-member split:

```text
Member 1: Backend Auth + Admin + Mangaka Proposal
Member 2: Backend Tantou Review + Board Voting + Production + Assistant
Member 3: Database + Persistence + PostgreSQL + Entity + Repository + Seed data
Member 4: Frontend UI + dashboard + API services
Member 5: PM/BA/QA/Docs/Integration/Demo
```

Keep this split as team context unless the user changes it.

## 7. Existing local git status at creation time

At the time this file was created:

```text
branch: main
untracked files:
  database.txt
  form.txt
```

No commit/push was requested or performed.

## 8. Rules for future AI sessions

Always do these before major work:

1. Read this `CURRENT.md` first.
2. Read `form.txt` and `database.txt` if architecture/database decisions are involved.
3. Treat old `src/backend` and `src/frontend` as business-flow reference unless user asks to modify them directly.
4. If API changes, update `docs/API_CONTRACT.md` or a new API doc for the new project.
5. If DB changes, update database migration/schema docs.
6. If team scope changes, update `docs/TEAM_TASK_ASSIGNMENT.md`.
7. Do not edit old/original projects unless explicitly requested.
8. Do not commit/push unless explicitly requested.
9. Do not commit secrets, local DB files, `node_modules`, `target`, `dist`, uploads, logs, or build output.
10. Before reporting done for code changes, run relevant build/test if practical.

## 9. Auto-update rule requested by user

When the user says something like:

```text
cập nhật
update current
cập nhật session
ghi nhớ lại
```

then update this file automatically with:

- latest decisions from the conversation
- work completed
- files changed
- current blockers
- next recommended tasks
- any changed project direction

This file is the working memory for the next session.

## 10. Session update - 2026-06-12

Backend work completed for the user's Member 1-related scope and supporting refactor:

- Clarified that `Member 1` is only a team assignment label, not an app user/role/package name.
- Removed misleading `member1` package structure from backend source/tests.
- Implemented Mangaka manuscript upload metadata attachment:
  - upload can optionally receive `proposalId` + `authorEmail`
  - when provided together, upload summary/file metadata is attached to the owner's mutable proposal
  - updates manuscript file name, summary, version, uploaded timestamp, and proposal update timestamp
- Added/updated backend tests for manuscript upload metadata attachment.
- Refactored Java Spring package structure away from actor-owned services into a cleaner layered/domain style:

```text
com.mangaworkflow.api
|
+-- web/          -> controllers grouped by API actor/surface
|   +-- auth/
|   +-- admin/
|   +-- mangaka/
|   +-- editor/
|   +-- board/
|   +-- assistant/
|
+-- application/  -> services/use cases grouped by business domain
|   +-- auth/
|   +-- account/
|   +-- skill/
|   +-- proposal/
|   +-- production/
|
+-- domain/       -> DTO/request/response/enums grouped by domain
|   +-- auth/
|   +-- account/
|   +-- skill/
|   +-- proposal/
|   +-- production/
|   +-- task/
|
+-- persistence/  -> JPA entities/repositories
+-- config/
```

Important architecture decision:

- Controllers can stay actor/API-facing under `web`.
- Business logic should be domain/use-case-facing under `application`.
- Shared proposal workflow logic stays in one proposal service instead of splitting into Mangaka/Editor/Board services.
- API URLs were intentionally kept unchanged.

Current backend database behavior explained:

- Default app profile points to PostgreSQL via `application.properties`:
  - `jdbc:postgresql://localhost:5432/manga_workflow`
- `local` profile uses H2 file DB via `application-local.properties`.
- `demo` profile disables DB/JPA autoconfiguration.
- Maven tests pass mainly because services support in-memory fallback/test construction; test success does not mean PostgreSQL is running.

Verification completed:

```text
.\mvnw.cmd -q test
MAVEN_TEST_PASS
```

Also verified:

```text
No Java source contains `member1`.
No imports/packages remain under old `controller`, `service`, `model` root packages.
No imports/packages remain under old actor-owned controller/service/model package paths.
```

Files/directories expected to be changed by this work:

- `src/backend/src/main/java/com/mangaworkflow/api/web/`
- `src/backend/src/main/java/com/mangaworkflow/api/application/`
- `src/backend/src/main/java/com/mangaworkflow/api/domain/`
- `src/backend/src/test/java/com/mangaworkflow/api/web/`
- `src/backend/src/test/java/com/mangaworkflow/api/application/`
- removed old backend package paths under root `controller/`, `service/`, `model/`, and actor-owned `auth/`, `admin/`, `mangaka/`, `editor/`, `board/`, `assistant/`

## 11. Current next recommended tasks

Suggested next steps if the user asks to continue implementation:

1. Decide final stack for the new version:
   - Backend: Java Spring Boot or Node.js
   - Frontend: React + TypeScript
   - DB: PostgreSQL
2. Convert `database.txt` into real PostgreSQL migrations.
3. Create seed data for roles, permissions, skills, demo users.
4. Create backend modules based on `form.txt` layers.
5. Create frontend role dashboards based on the business flow.
6. Add storage-server folders and upload/download rules.
7. Add AI subsystem placeholder only if needed for scope.
