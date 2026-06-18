# CURRENT - SWP391 Manga Project Lam Lai

Last updated: 2026-06-14

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
backend
frontend
canonical schema files (`schema (1).sql`, `schema (1).sql`)
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

Important: this differs from the removed legacy persistence implementation.

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
3. Treat old `backend` and `frontend` as business-flow reference unless user asks to modify them directly.
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
com.mangastudio.workflow
|
+-- controllers/  -> API controllers grouped in one package
|
+-- services/     -> services/use cases
|
+-- dtos/         -> DTO/request/response/enums
|
+-- entities/     -> JPA entities
+-- repositories/ -> Spring Data repositories
+-- config/
```

Important architecture decision:

- Controllers now live under `controllers`.
- The root package is `com.mangastudio.workflow` to match the current project layout.
- Business logic now lives under `services`.
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

- `backend/src/main/java/com/mangastudio/workflow/controllers/`
- `backend/src/main/java/com/mangastudio/workflow/services/`
- `backend/src/main/java/com/mangastudio/workflow/dtos/`
- `backend/src/main/java/com/mangastudio/workflow/entities/`
- `backend/src/main/java/com/mangastudio/workflow/repositories/`
- `backend/src/main/java/com/mangastudio/workflow/config/`
- `backend/src/test/java/com/mangastudio/workflow/`
- removed old backend package paths under root `controller/`, `service/`, `model/`, and actor-owned `auth/`, `admin/`, `mangaka/`, `editor/`, `board/`, `assistant/`

## 11. How to know the code and business flow are correct

The project should be verified at two levels:

### A. Code-level correctness

Use automated checks to prove the code compiles and expected methods/controllers still behave correctly:

```text
cd backend
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

The user provided final schema/layout inputs and asked to compare the new schema against the older database draft before changing anything.

Schema comparison notes were created at:

```text
schema_comparison_recommendations.md
```

The final MVP schema is now synchronized in both locations:

```text
schema.sql
schema (1).sql
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

## 13. Backend schema migration status

Latest backend implementation pass aligned the Spring Boot backend with the final MVP schema while preserving the existing controller/DTO contracts used by tests and frontend demos.

Changed backend areas:

```text
backend/src/main/java/com/mangastudio/workflow/services/InMemoryAccountService.java
backend/src/main/java/com/mangastudio/workflow/services/InMemorySkillCategoryService.java
backend/src/main/java/com/mangastudio/workflow/services/InMemoryMangaProposalService.java
backend/src/main/java/com/mangastudio/workflow/services/InMemoryMangakaProductionService.java
backend/src/main/java/com/mangastudio/workflow/entities/*.java
backend/src/main/java/com/mangastudio/workflow/repositories/*.java
```

Implemented schema coverage:

- Added JPA entities/repositories for the final schema tables:
  - `roles`
  - `permissions`
  - `role_permissions`
  - `users`
  - `assistant_profiles`
  - `skills`
  - `user_skills`
  - `series`
  - `chapters`
  - `pages`
  - `tasks`
  - `submissions`
  - `annotations`
  - `reader_metrics`
  - `board_votes`
- Account/admin/auth DB path now uses schema tables:
  - `users`
  - `roles`
  - `skills`
  - `user_skills`
- Proposal workflow DB path was migrated toward schema tables:
  - `series`
  - `board_votes`
  - `users`
  - `roles`
- Production workflow was rewritten to keep the existing API working while mapping the MVP workflow to schema concepts:
  - `chapters`
  - `pages`
  - `tasks`
  - `submissions`
- Existing demo/controller DTO contracts were preserved so old tests and frontend flows do not break immediately.
- Compatibility behavior remains where the old API has concepts not directly represented in the simplified MVP schema, especially production regions. Region/task compatibility data is stored or reconstructed through task/page compatibility logic rather than reintroducing a separate table.

Important workflow rule now enforced:

```text
A task that is already APPROVED cannot be sent back for redo.
```

Verification performed:

```text
cd backend
.\mvnw.cmd test -q
```

Latest test result:

```text
Failures: 0
Errors: 0
```

Current local status at the time of this note:

- Backend code changes are present locally.
- They have not yet been committed/pushed unless the user explicitly asks to do so after this note.
- `CURRENT.md` was updated after the backend migration pass.

## 14. Session update - 2026-06-14 follow-up

Additional backend/scope-safe implementation and verification completed after the backend schema migration pass:

- Reviewed the current git status and existing backend/database-related changes.
- Fixed Spring Boot runtime startup issue in `InMemoryMangaProposalService` by ensuring only one constructor is autowired by Spring.
- Aligned manuscript storage with the target `form.txt` layout:
  - Added `storage-server/manuscripts/`
  - Added `storage-server/submissions/`
  - Added `storage-server/annotations/`
  - Added `storage-server/references/`
  - Updated `.gitignore` so generated storage files remain ignored while `.gitkeep` placeholders can be committed.
- Updated Mangaka manuscript upload/download backend behavior:
  - default storage path now uses `storage-server/manuscripts`
  - configurable with `MANUSCRIPT_STORAGE_DIR` / `app.storage.manuscripts-dir`
  - download still falls back to the older local path `~/swp391-uploads/manuscripts` for previously uploaded local demo files.
- Added target-layout placeholders:
  - `ai-subsystem/api_bridge.py`
  - `ai-subsystem/models/.gitkeep`
  - `ai-subsystem/scripts/.gitkeep`
  - `database/migrations/.gitkeep`
  - `database/seeds/.gitkeep`
- Updated Postman/Newman full-flow artifact:
  - upload now expects `201 Created`
  - statuses now match current DTO values like `Draft`, `SubmittedToEditor`, `UnderBoardReview`, `Approved`, `Pending`, `InProgress`, and `Submitted`
  - added the 3rd board member vote because the current workflow requires all three board members to vote before final decision.
- Ran `npm install` at repo root so the existing Newman dev dependencies are available locally.
  - `node_modules/` remains ignored.
  - `npm install` reported audit warnings: 19 vulnerabilities (`8 moderate`, `10 high`, `1 critical`). No forced audit fix was run because it may introduce breaking dependency changes.
- Important correction: unintended frontend edits were reverted. No frontend source files should remain modified from this follow-up.

Verification performed after these changes:

```text
cd backend
.\mvnw.cmd test -q
```

Result:

```text
Failures: 0
Errors: 0
```

```text
cd <repo-root>
npm run test:api
```

Result:

```text
Newman full manga workflow passed:
15 requests, 30 assertions, 0 failures.
```

Runtime notes:

- Backend demo server was started detached for Newman using profile `demo` and then stopped after the API flow passed.
- Postman reports were generated under `postman/reports/`, which is ignored by git.
- Latest state is ready for backend/API demo testing.
- Created `VAN_DAP_COMPONENT_API_MAPPING.csv` as an Excel-openable viva checklist mapping components/pages to frontend services, backend APIs, Java controllers/services, status checks, DTOs, DB tables, and suggested answers.
- No commit or push was performed.

Current local status summary at the time of this note:

- Existing backend schema-alignment changes are still uncommitted.
- New/updated backend storage layout, AI placeholder, database placeholder, Postman collection/environment, and config changes are also uncommitted.
- Frontend source changes from this follow-up were reverted because the user is not responsible for frontend scope.
- Do not commit/push unless the user explicitly asks.

## 15. Current next recommended tasks

Suggested next steps if the user asks to continue implementation:

1. Review the full git diff, especially the large backend schema-alignment files, before committing.
2. If accepted, commit the current stable backend/state artifacts as one or more logical commits.
3. Optionally address `npm audit` findings in a separate dependency-maintenance task, not mixed with feature/schema work.
4. If PostgreSQL is available, execute `schema.sql` against a live database to validate the final SQL outside H2/in-memory tests.
5. If the team wants a cleaner future architecture, design schema-native APIs after the current demo-compatible backend is stable.

## 16. Session update - 2026-06-15 schema/backend alignment

User clarified the canonical database file for the current work:

```text
schema (1).sql
```

Important scope rule from the user:

- Only change the user's assignment scope.
- Do not modify other members' frontend/editor/board/production/assistant areas unless explicitly requested.

Database cleanup/alignment completed:

- `schema (1).sql` was treated as the source of truth and was not modified.
- `schema (1).sql` was synced to match `schema (1).sql` content.
- The old conflicting schema draft under `docs/database` was deleted because it was not the current runtime/schema source.

Member 1 backend work started and kept API-compatible:

- Updated `backend/src/main/java/com/mangastudio/workflow/services/InMemoryAccountService.java`:
  - inactive users/accounts no longer authenticate in either memory mode or schema DB mode.
- Updated `backend/src/main/java/com/mangastudio/workflow/services/InMemoryMangaProposalService.java`:
  - Member 1 Mangaka proposal create/list/get/update/submit/delete now use canonical `series` persistence when schema repositories are active.
  - API response shape remains compatible with existing frontend/tests.
  - Controller routes were not changed.
  - No legacy proposal table or schema column was introduced.
  - Proposal-only metadata that does not exist in `schema (1).sql` is kept in service-local compatibility metadata instead of changing the DB schema.

Files intentionally avoided in this pass:

- Frontend source files.
- Editor/Board controllers.
- Production/Assistant service/controller code.
- `schema (1).sql`.

Verification performed:

```text
cd backend
.\mvnw.cmd test
```

Result:

```text
Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Notes for next session:

- Continue backend only in Member 1 scope unless user changes the scope.
- If persistence for manuscript metadata is required later, ask the database owner/teacher where it belongs because `schema (1).sql` currently has no manuscript/proposal metadata table/columns.
- Do not revive deleted legacy schema/persistence as the source of truth.

## 17. Session update - 2026-06-15 legacy DB cleanup pass

User explicitly asked to remove everything unused / unrelated to the new canonical database.

Cleanup completed in backend scope:

- Deleted legacy proposal persistence entity/repository files.
- Deleted legacy account/skill persistence entity/repository files.
- Deleted legacy mangaka production persistence entity/repository files.
- Updated services to use canonical schema entities/repositories:
  - `InMemoryAccountService.java` → `users`, `roles`, `skills`, `user_skills`
  - `InMemorySkillCategoryService.java` → `skills`
  - `InMemoryMangaProposalService.java` → `series`, `board_votes`, `users`
  - `InMemoryMangakaProductionService.java` still uses canonical entities/repositories and no longer relies on the deleted legacy mangaka-* tables in the current backend pass.

Verification after cleanup:

```text
cd backend
.\mvnw.cmd test
```

Result:

```text
Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Current rule going forward:

- Do not reintroduce deleted legacy tables/entities/repositories unless the database schema itself changes.

## 18. Session update - 2026-06-16 git push completed

The schema/backend cleanup work was committed and pushed to GitHub.

Remote:

```text
origin https://github.com/lexuanduyanh-svg/SWP391_MANGA_PROJECT_LAMLAI.git
```

Branch:

```text
main
```

Pushed commit:

```text
bb56bc4 refactor: align backend with canonical schema
```

Important rebase note:

- Initial push was rejected because remote `main` had newer commits.
- Local commit was rebased on top of remote `main`.
- During rebase, remote had already deleted the duplicate schema copy under `manga_database/manga_database/schema.sql`.
- Conflict was resolved by keeping the remote deletion and updating docs to reference only `schema (1).sql` as the canonical schema file.

Verification before final push:

```text
cd backend
.\mvnw.cmd test
```

Result:

```text
Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Files/folders intentionally left untracked and not pushed:

```text
ai-subsystem/
database/
project_layout (1).txt
```

Reason:

- They were not part of the schema/backend cleanup commit.
- Avoid pushing unrelated draft/generated/local files without explicit confirmation.

Current git state after push:

- `main` contains the pushed cleanup commit.
- Only the untracked files/folders listed above remain local.
