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
com.mangaworkflow
|
+-- api/          -> controllers grouped by API actor/surface
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

- Controllers can stay actor/API-facing under `api`.
- The package previously named `web` was renamed to `api` because the user found `web` confusing and wanted it closer to `form.txt`.
- To avoid awkward Java packages like `com.mangaworkflow.api.api`, the root package was changed from `com.mangaworkflow.api` to `com.mangaworkflow`.
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

- `src/backend/src/main/java/com/mangaworkflow/api/`
- `src/backend/src/main/java/com/mangaworkflow/application/`
- `src/backend/src/main/java/com/mangaworkflow/domain/`
- `src/backend/src/main/java/com/mangaworkflow/persistence/`
- `src/backend/src/main/java/com/mangaworkflow/config/`
- `src/backend/src/test/java/com/mangaworkflow/api/`
- `src/backend/src/test/java/com/mangaworkflow/application/`
- removed old backend package paths under root `controller/`, `service/`, `model/`, and actor-owned `auth/`, `admin/`, `mangaka/`, `editor/`, `board/`, `assistant/`

## 11. How to know the code and business flow are correct

The project should be verified at two levels:

### A. Code-level correctness

Use automated checks to prove the code compiles and expected methods/controllers still behave correctly:

```text
cd src/backend
.\mvnw.cmd -q test
```

Current latest result:

```text
MAVEN_TEST_PASS
```

This proves:

- Java code compiles.
- Spring/controller tests pass.
- Service unit tests pass.
- Refactors did not break the currently covered API behavior.

But this alone does not prove the whole manga workflow is correct end-to-end.

### B. Flow-level correctness

Verify the actual business journey with API calls/Postman/manual test cases:

```text
1. Login as Mangaka
2. Mangaka creates proposal
3. Mangaka previews/uploads manuscript
4. Manuscript metadata attaches to the draft proposal
5. Mangaka submits proposal
6. Editor requests revision or forwards to Board
7. Board members vote approve/reject
8. System decides by majority
9. If approved, Mangaka creates chapter/page/region/task
10. Assistant starts/submits task
11. Mangaka approves or requests redo
```

A flow is considered correct only when:

- every step returns the expected HTTP status
- response body contains expected data
- invalid actions are blocked
- status transitions happen in the right order
- ownership/role-related email checks work
- database or in-memory state changes match the expected result

Recommended next verification artifact:

- Create a Postman collection or API test script for the full manga workflow above.
- Add integration-style tests for the full proposal lifecycle, not only individual service/controller tests.

## 12. Database schema update from final review

The user provided final schema/layout inputs and asked to compare the new schema against the older `docs/database/schema_postgresql_v2.sql` before changing anything.

Schema comparison notes were created at:

```text
schema_comparison_recommendations.md
```

The final MVP schema is now synchronized in both locations:

```text
schema.sql
manga_database/manga_database/schema.sql
```

Important schema decisions applied:

- Use the shorter MVP schema as the implementation base instead of merging the whole older v2 schema.
- Keep RBAC tables: `roles`, `permissions`, `role_permissions`.
- Make `users.role_id` required with `NOT NULL`.
- Keep `REVISION_REQUESTED` for both series review and task review flows.
- Keep `tasks.feedback_notes` so Mangaka/Editor revision feedback has a storage field.
- Rename task submission time from `timestamp` to `submitted_at`.
- Add default seed data for roles, permissions, role-permission mappings, and assistant skills.
- Add `created_at`/`updated_at` timestamps to core tables.
- Add PostgreSQL `set_updated_at()` trigger support for tables with `updated_at`.
- Add workflow statuses for `series`, `chapters`, `pages`, and `tasks` to support MVP screens.
- Add `annotations.resolved` for comment resolution.
- Add indexes for common role/status/workflow lookups.

Deliberately not included in the MVP schema yet:

```text
audit_logs
notifications
editorial_boards
board_memberships
manuscripts
page_versions
task_reviews
task_submissions
series_rankings
earning_rates
```

Reason: those older v2 tables are useful later but would expand scope and complexity before there are matching MVP screens/use cases.

Verification notes:

- The two active schema files were compared with `git diff --no-index`; no content differences were reported.
- Grep confirmed the new schema files use `submitted_at` instead of `timestamp TIMESTAMP`.
- `psql` was not available in PATH in this environment, so the schema was not executed against a live PostgreSQL database here.

## 13. Current next recommended tasks

Suggested next steps if the user asks to continue implementation:

1. Decide final stack for the new version:
   - Backend: Java Spring Boot or Node.js
   - Frontend: React + TypeScript
   - DB: PostgreSQL
2. Use `schema.sql` as the current PostgreSQL schema baseline.
3. Align backend entities/repositories/services with the final MVP schema.
4. Create demo seed users after deciding password hashing strategy.
5. Create backend modules based on `form.txt`/`project_layout.txt` layers.
6. Create frontend role dashboards based on the business flow.
7. Add storage-server folders and upload/download rules.
8. Add AI subsystem placeholder only if needed for scope.
