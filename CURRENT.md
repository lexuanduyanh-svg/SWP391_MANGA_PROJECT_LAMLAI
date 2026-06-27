# CURRENT - SWP391 Manga Project Lam Lai

Last updated: 2026-06-23

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
Flow 1:
Mangaka creates proposal/draft + uploads manuscript
-> Mangaka submits proposal to Tantou Editor
-> Tantou Editor requests revision or forwards to Editorial Board
-> 3 Board members vote approve/reject
-> System auto-decides by majority
-> If approved, system creates/updates series from proposal title

Flow 2:
Approved series -> Mangaka creates chapter/page/region/task
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

## 7. Current session status

### Done
- FE/BE demo has been started multiple times; ports used were:
  - FE `http://localhost:5173`
  - BE `http://localhost:8080`
- Mangaka proposal flow UI was refactored into pages/components.
- Added `Create Draft` entry from Proposal List into `DraftForm`.
- Restored a more complete Mangaka dashboard UI in `Build as requested/src/app/pages/mangaka/MangakaDashboard.tsx`.
- `npm run build` in `Build as requested` passed successfully.
- `CURRENT.md` was updated to capture current state.
- Cleanup this session completed:
  - removed `logs/*.log`
  - removed `postman/reports/`
  - removed `manga_database.zip`
  - removed `project_layout (1).txt`
  - removed `FLOW_1_MANGAKA_EDITOR_DETAILED.md`
  - removed generated manuscript files under `backend/storage-server/manuscripts/`
- `Build as requested/` reviewed and confirmed as current frontend, not legacy demo.
- `Series 1/` kept as screenshot/reference assets.
- `backend/storage-server/` kept as runtime storage folder.

### In progress / known issues
- `DraftForm` submit flow still needs clearer backend error display.
- File picker / Chrome "not responding" appears to be environment/browser-side, not app logic.
- Some pages still contain placeholder content and may need end-to-end polish.

### Notes
- Main frontend being worked on now is `Build as requested/`.
- Old `frontend/` folder is reference-only unless explicitly needed for migration.
- `Build as requested/` UI pages are complete for demo; no legacy remnants found.
- `Series 1/` contains only screenshot references; safe to keep local.
- `backend/storage-server/` is runtime folder for manuscript uploads; kept but cleaned of generated `.txt` files.
- Next session should skip re-checking demo remnants as cleanup is complete.

## 12. Background Job Board

```text
SENTINEL: background-job-board-v2
Do not poll running jobs. Wait for hook-driven completion, or use cancel_task only for explicit cancellation. Reconcile terminal jobs before final response. Reuse only completed sessions for the same specialist/context; never reuse cancelled or errored sessions.
```

### Active / Unreconciled
- none

### Reusable Sessions
- `exp-1` / `ses_10e1ab955ffeSF5Xs4PFlVpNGk` / explorer / completed, reconciled
  - Objective: Check GitHub repo current code
  - Context: `InMemoryMangaProposalService.java`, `InMemoryAccountService.java`, `MangakaController.java`, `InMemorySkillCategoryService.java`, `AdminController.java`, `InMemoryMangakaProductionService.java`, `MangakaProductionController.java`, `FullMangaWorkflowFlowTest.java`
- `exp-2` / `ses_10e1ab986ffe1dD7pkahwgN4mH` / explorer / completed, reconciled
  - Objective: Check Member 2 code at Downloads path

At the time this file was created:

```text
branch: main
untracked files:
  database.txt
  form.txt
```

No commit/push was requested or performed.

## 8. Session update - 2026-06-21

**Completed work**:

- Deleted legacy files:
  - `JwtUtil.java`
  - `MangakaProposalController.java`
  - `AdminAccountController.java`
  - `AdminSkillController.java`
  - `TantouEditorProposalController.java`
  - `EditorialBoardProposalController.java`

- Renamed & refactored controllers:
  - `AdminController` now contains both account and skill endpoints.
  - `MangakaController` replaces `MangakaProposalController`.
  - `EditorController` and `BoardController` replace the old editor/board controllers.

- Updated corresponding tests:
  - `AdminControllerTest`, `AdminSkillsControllerTest`, `MangakaControllerTest`, `EditorControllerTest`, `BoardControllerTest`.
  - Adjusted constructors and imports to match new class names and signatures.

- Build & tests:
  - Ran `mvnw.cmd test`; all tests pass (`exit code 0`).

- No compile errors or stray legacy references remain.

**Next steps**:

- Align remaining services and DTOs to the architecture described in `form.txt`.
- Update API contracts (`docs/API_CONTRACT.md`) if endpoint signatures changed.
- Prepare frontend stubs or API specs for upcoming UI work.

**Blockers**: none.

## 9. Rules for future AI sessions

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
- Runtime artifacts cleaned this session:
  - `logs/backend-postgres.out.log`
  - `logs/backend-postgres.err.log`
  - `logs/backend-local.out.log`
  - `logs/backend-local.err.log`
  - `postman/reports/`
  - `project_layout (1).txt`
- `manga_database.zip` still exists locally and has not been removed.

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

## 19. Session update - 2026-06-18 layout refactor pushed

The project layout refactor was completed, rebased on top of the latest remote `main`, and pushed to GitHub.

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
9e39112 refactor: align project layout
```

Remote verification:

```text
origin/main -> 9e39112b438a00c8531994ce57f26fadfb1370c3
```

Important remote sync note:

- Initial push was rejected because remote `main` had newer commits.
- Remote had added these commits before the push:
  - `5b973c8 Delete schema (1).sql`
  - `c77dc77 Delete VAN_DAP_COMPONENT_API_MAPPING.csv`
- The local layout commit was rebased on top of `origin/main`.
- No force push was used.

Layout now pushed:

```text
backend/
frontend/
database/
ai-subsystem/
```

Backend package/layout now pushed:

```text
backend/src/main/java/com/mangastudio/workflow/
|-- config/
|-- controllers/
|-- services/
|-- repositories/
|-- entities/
|-- dtos/
`-- MangaWorkflowApiApplication.java
```

Major changes included in the pushed layout commit:

- Moved backend from `src/backend` to `backend`.
- Moved frontend from `src/frontend` to `frontend`.
- Moved backend Java package from `com.mangaworkflow` to `com.mangastudio.workflow`.
- Reorganized backend Java packages from the older `api/application/domain/persistence` style into `controllers/services/dtos/entities/repositories/config`.
- Added `database/` with schema/template/placeholder folders.
- Added `ai-subsystem/` with placeholder structure.
- Updated confirmed documentation/rule files for the new layout:
  - `README.md`
  - `CURRENT.md`
  - `docs/IMPLEMENTATION_GUIDE.md`
  - `docs/TEAM_TASK_ASSIGNMENT.md`
  - `rule.md`
  - `skill.md`
- Replaced old tracked `project_layout.txt` with `project_layout (1).txt`.

Verification before push:

```text
cd backend
.\mvnw.cmd test
```

Result:

```text
27 tests, 0 failures, 0 errors, 0 skipped
BUILD SUCCESS
```

```text
cd frontend
npm run build
```

Result:

```text
Vite build succeeded
```

Files intentionally not pushed in that commit because they were pre-existing or outside the confirmed scope:

```text
docs/AI_ASSISTANT_CONTEXT.md
docs/DEMO_SCRIPT.md
docs/GIT_WORKFLOW.md
docs/PROJECT_RULES.md
docs/SESSION_HANDOFF.md
docs/TESTING_GUIDE.md
docs/TEST_CASES.md
package.json
backend/storage-server/
```

Current user request after this update:

```text
Update CURRENT.md first, then install/configure:
https://github.com/shanraisshan/claude-code-best-practice
```

Installation caution for the next step:

- This repository currently has no `.claude/`, `CLAUDE.md`, or `.mcp.json` files.
- Inspect the upstream install/use instructions before copying files.
- Do not overwrite project-specific rules or current work memory without merging deliberately.

## 20. Session update - 2026-06-18 Claude Code best-practice installed

The upstream Claude Code best-practice repository was installed locally for this project.

Upstream repository:

```text
https://github.com/shanraisshan/claude-code-best-practice
```

Installed locally as a git submodule/reference checkout:

```text
claude-code-best-practice/
```

Submodule revision at install time:

```text
994c6612991152d4cc8dcf09d02ade46e4ec6163
```

Project-local Claude Code files created:

```text
CLAUDE.md
.claude/.gitignore
.claude/settings.json
.claude/commands/claude-best-practice.md
.claude/agents/claude-best-practice-researcher.md
.claude/skills/claude-best-practice/SKILL.md
```

Installation approach:

- The upstream repository has no package/script installer.
- The safe install method used here is a project-local reference submodule plus minimal project-specific Claude Code configuration.
- Upstream `.claude/settings.json` was not copied wholesale because it contains personal spinner/status-line text and hook configuration that is not specific to this SWP391 project.
- Upstream hooks/sounds were not enabled.
- Project MCP servers were not auto-enabled.

How to use in Claude Code:

```text
/claude-best-practice <topic or question>
```

or ask questions about Claude Code agents, commands, skills, settings, hooks, MCP, memory, or workflows. The local config tells Claude Code to search `claude-code-best-practice/` first.

Verification performed:

```text
git submodule status
node -e "JSON.parse(require('fs').readFileSync('.claude/settings.json','utf8')); console.log('settings json ok')"
```

Result:

```text
994c6612991152d4cc8dcf09d02ade46e4ec6163 claude-code-best-practice (heads/main)
settings json ok
```

Important git note:

- This install is local-only tooling/reference material and is not part of the SWP391 application source.
- Do not commit or push these local tool/config paths unless the user explicitly asks to share Claude Code tooling config:
  - `.claude/`
  - `CLAUDE.md`
  - `claude-code-best-practice/`
  - `.gitmodules`
- These paths were added to local-only `.git/info/exclude`, not project `.gitignore`, so they stay hidden from local `git status` without affecting teammates.
- Keep the pre-existing unrelated modified files separate from this local tooling install.

User rule added on 2026-06-18:

```text
Tool installs, AI assistant configs, local helper repos, and other setup files that are not directly part of the project must not be pushed to Git.
```

## 21. Session update - 2026-06-18 schema-aligned docs cleanup and OpenCode model request

User requested:

```text
đẩy lên git hub và cập nhật current cho t sau đó cài codex/gpt5.5-pro và set thành default cho tất cả cho t
```

Project cleanup/documentation work prepared for GitHub push:

- Runtime/generated artifacts were cleaned locally where safe:
  - `frontend/tsconfig.tsbuildinfo` removed as generated build metadata.
  - Old runtime upload `.txt` files under `storage-server/manuscripts` removed while keeping `.gitkeep` style placeholders intact where present.
- Docs were aligned to the current canonical persistence source of truth:
  - `database/schema.sql` is the canonical schema file.
  - DB roles are `Admin`, `Mangaka`, `Assistant`, `Editor`, `Board`.
  - UI/code maps `Editor` to Tantou Editor and `Board` to Editorial Board Member.
  - Unified proposal/series persistence is `series`.
  - Proposal/series DB statuses are `DRAFT`, `SUBMITTED_TO_EDITOR`, `REVISION_REQUESTED`, `UNDER_BOARD_REVIEW`, `APPROVED`, `REJECTED`.
  - Task DB statuses are `ASSIGNED`, `PENDING_REVIEW`, `APPROVED`, `REVISION_REQUESTED`.
  - `tasks.region_coordinates` JSONB is the persisted region representation; there is no separate DB `regions` table.
- Stale endpoint/status wording was updated:
  - Old `/api/editor/*` and `/api/board/*` docs replaced with `/api/tantou-editor/*` and `/api/editorial-board/*`.
  - Old short Mangaka task endpoints replaced with nested production task approve/redo endpoints.
  - Old proposal status examples like `PENDING` / `InProduction` are retained only as explicit "do not use" warnings.
  - Old task status `Completed` is retained only as an explicit "do not use" warning; use DB `APPROVED` / UI `Approved`.

Files included in the project push scope:

```text
CURRENT.md
README.md
docs/AI_ASSISTANT_CONTEXT.md
docs/API_CONTRACT.md
docs/DEMO_SCRIPT.md
docs/GIT_WORKFLOW.md
docs/IMPLEMENTATION_GUIDE.md
docs/PROJECT_RULES.md
docs/SESSION_HANDOFF.md
docs/TEAM_TASK_ASSIGNMENT.md
docs/TESTING_GUIDE.md
docs/TEST_CASES.md
docs/requirements/MVP_SCOPE_AND_BUSINESS_RULES.md
form.txt
package.json
```

Files intentionally not included in the project push scope:

```text
backend/storage-server/
```

Reason:

- It contains runtime upload files and should not be committed as project source.

Local OpenCode/oh-my-opencode-slim configuration requested after project push:

```text
codex/gpt5.5-pro
```

Rules for that setup:

- Configure it in the user's local OpenCode config, not in project Git.
- Do not commit API keys or local AI assistant config to the SWP391 repository.
- Set the core OpenCode default model and all oh-my-opencode-slim agent models to the requested model if the provider/model validates locally.
- Restart OpenCode after config changes if the running session does not pick them up automatically.

Completion result:

- Project documentation cleanup was committed and pushed first:

```text
fef6965 docs: align workflow documentation with schema
```

- Local OpenCode core config was updated at:

```text
C:\Users\AD\.config\opencode\opencode.jsonc
```

- Core default model is now:

```text
codex/gpt5.5-pro
```

- Local oh-my-opencode-slim config was updated at:

```text
C:\Users\AD\.config\opencode\oh-my-opencode-slim.json
```

- Active oh-my-opencode-slim preset is now:

```text
codex
```

- The `codex` preset sets these agents to `codex/gpt5.5-pro`:

```text
orchestrator
oracle
librarian
explorer
designer
fixer
council
observer
```

- Config validation performed:

```text
node -e "JSON.parse(...opencode.jsonc...); JSON.parse(...oh-my-opencode-slim.json...); console.log('json ok')"
bunx oh-my-opencode-slim@latest doctor
```

- Validation result:

```text
json ok
[user] C:\Users\AD\.config\opencode\oh-my-opencode-slim.json ✓
[preset] codex ✓
```

Activation note:

- Restart OpenCode to make sure the new core model and plugin preset are active in new sessions.
- The local config files contain personal/provider configuration and must stay local-only unless the user explicitly asks to share them.

## 22. Session update - 2026-06-21 flow correction + code review

### Flow rules corrected

Luồng nghiệp vụ chính giờ tách rõ 2 flow:

```text
Flow 1: Mangaka tạo proposal/draft + upload manuscript
-> Submit cho Tantou Editor
-> Tantou Editor request revision hoặc forward Board
-> Board vote approve/reject
-> System auto-decides by majority
-> Nếu approved: hệ thống tạo/update series từ proposal, dùng title của proposal làm series title

Flow 2: Chỉ khi approved mới tiếp tục
-> Mangaka tạo chapter/page/region/task
-> Assign task cho Assistant
-> Assistant start/submit task
-> Mangaka approve hoặc request redo
```

Quy tắc quan trọng:
- Flow 2 **chỉ bắt đầu khi Flow 1 approved**.
- Khi approved, hệ thống tự động **tạo hoặc update series** từ proposal, lấy **title của proposal** làm **series title**.
- Task đã `APPROVED` **không thể redo**.
- Revision loop chỉ áp dụng cho **proposal**, không áp dụng cho task (task redo có endpoint riêng).
- Không dùng proposal status cũ như `PENDING` hoặc `InProduction`.

### Docs đã sửa để khớp flow mới

| File | Thay đổi |
|------|---------|
| `rule.md` | Thêm rule tiếng Việt mặc định + flow 1/flow 2 rules |
| `README.md` | Cập nhật mô tả flow + thêm rule "approved → tạo series từ proposal title" |
| `docs/DEMO_SCRIPT.md` | Tách flow thành "Flow 1 / Flow 2" rõ ràng + thêm bước "hệ thống tạo/update series từ proposal" |
| `docs/TEAM_TASK_ASSIGNMENT.md" | Sắp xếp lại các bước theo đúng Flow 1 → Flow 2 |
| `docs/API_CONTRACT.md` | Tách endpoint Member 2 thành 2 section: Review/Board và Production/Assistant |
| `docs/requirements/MVP_SCOPE_AND_BUSINESS_RULES.md` | Viết lại Flow 1 và Flow 2 + thêm rule "approved → tạo series từ proposal title" |

### Code review Member 2

**Repo code member2 gửi** (`Manga-Creation-Workflow-and-Publishing-Management-System-main`): **TRỐNG** — chỉ có 1 file README 1 dòng.

**Mã thực tế** nằm trong dự án chính:
```
C:\Users\AD\OneDrive\Máy tính\Giao trình FPT\KY5\SWP391_NEW\backend\
    src\main\java\com\angastudio\workflow\
        controllers\
        services\
        entities\
        repositories\
        dtos\
```

**Các file thuộc scope Member 2:**

| Feature | File chính |
|---------|-----------|
| Tantou Editor (review/revision/forward) | `InMemoryMangaProposalService.java` (chung với Member 1) |
| Editorial Board (vote) | `InMemoryMangaProposalService.java` (chung với Member 1) |
| Production (chapter/page/region/task) | `InMemoryMangakaProductionService.java` |
| Assistant task (start/submit) | `InMemoryMangakaProductionService.java` hoặc controller tương ứng |
| Entity JPA (series, chapters, pages, tasks, board_votes, submissions, annotations) | `entities/*.java` ✅ đã có |
| Repository | `repositories/*.java` ✅ đã có |

**Kiểm tra code:**
- Entity + Repository: ✅ đã có, khớp canonical schema
- Newman full-flow: ✅ 15 requests, 30 assertions, 0 failures
- 27 unit tests: ✅ BUILD SUCCESS

**Cần xác nhận trong code:**
- Logic tự động quyết định sau 3 board votes (auto-approve/auto-reject) — chưa thấy rõ code
- Logic "approved → tạo/update series từ proposal title" — chưa thấy rõ code
- Region shim: `{regionId}` chỉ là key trong `tasks.region_coordinates` JSONB, không có bảng regions riêng

**Đề xuất tiếp theo:**
1. Kiểm tra/Thêm logic "approve → tạo series" trong `InMemoryMangaProposalService`
2. Kiểm tra/Thêm logic "auto-decision sau 3 votes" trong service liên quan
3. Thêm unit test cho 2 logic trên
4. Chạy Newman lại sau khi thay đổi

### Ngôn ngữ làm việc

- Mặc định trả lời **tiếng Việt**.
- Chỉ dùng tiếng Anh khi user hỏi bằng tiếng Anh hoặc yêu cầu rõ ràng.
- Code/component/endpoint giữ nguyên tên tiếng Anh.

### Background Job Board

```text
SENTINEL: background-job-board-v2
Không poll job đang chạy. Chờ hook tự báo xong.
Dùng cancel_task chỉ khi cần hủy rõ ràng.
Tổng hợp (reconcile) job đã kết thúc trước khi trả lời cuối.
Chỉ tái dùng session đã completed + reconciled; không tái dùng session cancelled/errored.
```

#### Đang chạy / Chưa tổng hợp
- none

#### Session có thể tái dùng
- `exp-1 / ses_115ba937bffeIyqTm80J04hQAf / explorer / completed, reconciled`
  - Mục tiêu: tìm assignment Member 1
  - Context đã đọc: `CURRENT.md` (1100 dòng), `TEAM_TASK_ASSIGNMENT.md` (300 dòng), `API_CONTRACT.md` (151 dòng), `DEMO_SCRIPT.md` (80 dòng)

## 11. Session update - 2026-06-21 (runtime DB verify)

Completed this round:

- Verified PostgreSQL service is running.
- Found psql.exe at C:\Program Files\PostgreSQL\18\bin\psql.exe.
- Confirmed database manga_workflow exists.
- Ran database/schema.sql successfully on PostgreSQL.
- Confirmed seed insert count INSERT 0 5.
- Verified backend application.properties targets PostgreSQL.
- Ran backend tests successfully.
- Stopped the process listening on port 8080.
- Restarted backend successfully on port 8080.
- Confirmed startup logs show PostgreSQL connection success:
  - HikariPool-1 - Start completed
  - Using dialect: org.hibernate.dialect.PostgreSQLDialect
  - Tomcat started on port(s): 8080 (http)
  - Started MangaWorkflowApiApplication

Current status:

- DB real: OK
- Schema: OK
- Spring to PostgreSQL connect: OK
- Backend runtime start: OK
- Port 8080: active again with app running

No blocker right now.

## 12. Session update - 2026-06-21 (git push)

## 13. Session update - 2026-06-22 (frontend split)

Completed frontend refactor for **Build as requested**:
- Created layout components `Sidebar.tsx` & `TopNav.tsx`.
- Implemented thin router in `App.tsx`.
- Extracted shared types, constants, data, `Badge` & `StatCard` to `shared.tsx`.
- Added page shells: `LoginPage`, `Notifications`, `Profile`.
- Added role page routers and minimal UI for Mangaka, Assistant, Editor, Board.
- Updated imports to use shared helpers.
- Build succeeded (`npm run build`).

**Blocker**: UI pages are placeholders; full UI still to be populated from original monolith if needed.

**Next steps**:
- Populate detailed UI for each role page from original `App.tsx`.
- Align frontend routes with API contracts if changes.
- Optionally add tests for UI components.


Completed this round:

- Verified working tree state and reviewed diff before push.
- Staged code/docs changes but kept local reference/content folders out of the push.
- Committed changes on main with commit:
  - e3dc836 refactor: cleanup legacy controllers, rename/simplify Member 1+2 endpoints, update docs and tests, verify PostgreSQL DB connection
- Pushed commit to remote:
  - origin/main

Important note:

- Series 1/ is still kept local as UI reference and was not pushed.
- ackend/storage-server/ contains local/generated runtime files and was not pushed.
- No blocker right now.

## 14. Session update - 2026-06-27 (scope reduction for 3-member team)

### Team change context

Team reduced from 5 members to 3 members (2 members dropped out).

Current team structure:
- 2 backend developers
- 1 frontend developer
- Timeline: 3 weeks remaining

Flow 1 (Proposal workflow) is COMPLETE and working ✅

Need to reduce Flow 2 scope to fit 3 people / 3 weeks.

### Scope reduction decisions

**Features CUT entirely:**

| Feature | Why cut | Effort saved |
|---------|---------|-------------|
| ❌ AI Summary preview (`api_bridge.py`, `/preview-upload` endpoint) | Requires Python service + AI model + preview UI modal | 1-2 weeks full-time |
| ❌ Region drawing (VisualCanvas component) | Requires HTML5 canvas + mouse event handling + region CRUD UI | 3-4 days frontend |
| ❌ Annotations (Editor page markup pins) | Requires canvas click positioning + comment modal + annotations API | 2-3 days FE+BE |
| ❌ Rankings screen (`reader_metrics` UI + `RankingService`) | Stretch goal, not core workflow demo | 2 days FE+BE |

**Features SIMPLIFIED:**

| Feature | Simplification | Implementation |
|---------|---------------|----------------|
| ⚠️ Earnings calculation | Show static seed value only, no dynamic calculation logic | Display `assistant_profiles.monthly_earnings` from DB, skip `EarningsService` |
| ✅ Region coordinates | Task assigned at **page level**, not pixel-level region | `tasks.region_coordinates = null` or `{"fullPage": true}`, no VisualCanvas UI |
| ✅ File validation | Frontend-only (JS check file type/size) | Keep simple validation, no AI preview |
| ✅ Manuscript upload | Keep existing backend implementation | Already working ✅ |
| ✅ Submission upload | Keep file upload (clone manuscript pattern) | Backend pattern exists ✅ |

### Flow 2 scope after reduction

```
Mangaka (after proposal approved):
1. Create chapter (title, chapter_number)
2. Create page (page_number, optionally upload page manuscript image)
3. Create task on page (description, payment, assign assistant_id)
   → No region drawing, task = work on full page
4. Assistant: Start task → Submit (file + note)
5. Mangaka: Approve or Request Redo
```

**Database tables used:**
- `chapters`, `pages`, `tasks`, `submissions` ✅
- `tasks.region_coordinates` column kept in schema but not used in UI (set null or fullPage JSONB)

**Database tables NOT implemented in UI:**
- `annotations` (schema kept, no UI/API)
- `reader_metrics` (schema kept, no UI/API)

### 3-week task split (3 members)

| Week | Backend Dev 1 | Backend Dev 2 | Frontend Dev |
|------|--------------|---------------|-------------|
| **Week 1** | Complete `MangakaProductionService` (chapter/page/task CRUD, no regions) | Complete `AssistantTaskController` (start/submit with file+note) | `ProductionDashboard`: Mangaka create chapter/page/task, assign assistant |
| **Week 2** | Unit tests for production flow + PostgreSQL integration | API task approve/redo + tests | `AssistantTaskBoard`: start/submit UI, polish 3-column kanban |
| **Week 3** | Bug fixes, ensure Newman API tests pass, seed demo data | Bug fixes, endpoint polish | FE-BE integration, polish UI, demo prep |

### What stays in schema but not implemented

Schema tables/columns preserved for future but not implemented now:
- `annotations` table (editor page markup)
- `reader_metrics` table (rankings/leaderboard)
- `tasks.region_coordinates` (spatial region data, will use null or `{"fullPage": true}`)
- AI subsystem (`ai-subsystem/api_bridge.py` remains placeholder)

### Verification targets

After scope reduction implementation:

**Backend:**
- `mvnw.cmd test` — all tests pass ✅
- Newman full-flow test — Flow 1 + Flow 2 basic production workflow pass

**Frontend:**
- `npm run build` — clean build ✅
- Manual smoke test: Mangaka create chapter/page/task → Assistant submit → Mangaka approve

### Next steps for implementation

1. Backend devs: implement simplified production endpoints (no region logic)
2. Frontend dev: build ProductionDashboard + AssistantTaskBoard (page-level task assignment)
3. Integration: connect FE-BE for Flow 2 workflow
4. Testing: Newman API + manual smoke test
5. Demo prep: seed data + demo script

**Blockers:** None currently. Scope reduction approved by user.
