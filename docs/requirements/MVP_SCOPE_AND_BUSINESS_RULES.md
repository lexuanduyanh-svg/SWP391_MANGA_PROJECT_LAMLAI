# MVP Scope and Business Rules - Manga Creation Workflow and Publishing Management System

> Working document for the clone only. This document tightens the original topic/SRD/database plan into an implementation-ready SWP391 MVP.

## 1. Purpose

The original project concept is strong but too broad for a reliable SWP391 implementation if every proposed feature is treated as core. This document defines a strict MVP scope, fixed role model, business rules, state transitions, permissions, and traceability rules so development can proceed without ambiguity.

## 2. Canonical project name

**Manga Creation Workflow and Publishing Management System**

Short name used in code/docs: **Manga Workflow**

## 3. Canonical roles

Use these role names consistently in requirements, database, backend, and frontend:

| Canonical DB value | Display name | Actor type | Notes |
|---|---|---|---|
| `admin` | Admin | Supporting actor | Manages users, boards, skills, rates, system oversight. |
| `mangaka` | Mangaka | Primary actor | Creates proposals, chapters, pages, regions, tasks; reviews assistant work. |
| `assistant` | Assistant | Primary actor | Receives assigned region/page tasks and submits work. |
| `tantou_editor` | Tantou Editor | Primary actor | Reviews proposals/manuscripts, annotates, requests revision, escalates to board, monitors production. |
| `editorial_board_member` | Editorial Board Member | Primary actor | Votes on proposals, enters reader data, reviews rankings, records series decisions. |

Deprecated/alias labels that should not be used in new code: `Editor`, `BoardMember`, `Author`.

## 4. MVP scope

### 4.1 Must-have MVP

These are core for SWP391 demo and implementation.

1. Authentication and fixed role-based access.
2. Admin user/account management at basic CRUD level.
3. Admin skill/category management.
4. Admin editorial board and membership management.
5. Mangaka series proposal creation.
6. Mangaka manuscript upload/resubmission.
7. Tantou Editor manuscript review and annotation.
8. Tantou Editor request revision or escalate proposal to Editorial Board.
9. Editorial Board proposal voting.
10. Series approval/rejection with publication schedule.
11. Mangaka chapter/page creation after series approval.
12. Mangaka page-region creation for task delegation.
13. Mangaka task assignment to Assistant.
14. Assistant assigned task workspace and work submission.
15. Mangaka task submission review: approve or redo/reject.
16. Assistant task history and estimated earnings dashboard.
17. Editorial Board reader poll data entry per publication cycle.
18. System ranking generation from poll data.
19. Editorial Board active-series decision: maintain, reschedule, cancel, or change format.
20. Notification list for important status changes.
21. Basic audit logging for sensitive workflow transitions.

### 4.2 Should-have

These may be implemented if core flows are stable.

1. Editor production/deadline monitoring dashboard.
2. Basic export/download links for uploaded files.
3. Basic filtering/searching by status, owner, assignee, cycle.
4. Simple chart display for ranking trend.

### 4.3 Future work / out of MVP

Do not treat the following as required for SWP391 MVP:

1. Dynamic RBAC / custom permission builder.
2. AI auto-coloring.
3. AI segmentation running as a real service.
4. Pixel-level visual diff/version control.
5. Real-time collaborative canvas editing.
6. Real payment processing or bank transfer.
7. External reader data API ingestion.
8. Mobile native applications.
9. Physical printing/logistics integration.
10. Production-grade anti-leak/on-premise hardening beyond role-based access and local file storage.

AI remains valid as the RBL/research direction: model comparison for manga region segmentation may be documented or demoed separately, but the MVP only needs manually created regions, with optional future field support for AI suggestions.

## 4.4 V1 revised focus

For the revised V1, keep the scope smaller and make these additions explicit:

1. Mangaka manuscript file validation before upload.
   - Restrict allowed file types.
   - Enforce a size limit.
   - Show clear validation errors before submission.
2. AI content summary preview before upload/submission.
   - ~~System extracts a short content summary from the selected file.~~
   - ~~Mangaka confirms the summary before the file is finalized.~~
3. More teacher interaction checkpoints.
   - Add review checkpoints in demo/documentation.
   - Record teacher feedback and update scope before coding the next module.
4. Keep the AI summary lightweight for V1.
   - ~~Treat it as a small supported feature, not a heavy standalone AI service.~~

## 4.5 Scope restoration (2026-06-30)

**Reason:** Temporary 3-person scope reduction is reverted. The source of truth is the original 5-member plan.
Flow 2 returns to chapter/page/region/task production scope.

### Features restored to MVP

| Feature | Original section | Restored expectation |
|---------|------------------|----------------------|
| AI Summary preview | 4.4 V1 revised focus item 2, 4 | Kept as the lightweight AI-facing feature before upload/submission |
| Region drawing (VisualCanvas, pixel-level selection) | 4.1 item 12, Flow 2 step 4 | Mangaka creates/selects regions on page images |
| Annotations (Editor page markup pins) | 4.1 item 7, FE-16/FE-17 | Tantou Editor can attach markup/feedback to reviewed assets |
| Rankings screen + composite score logic | 4.1 items 17, 18; Flow 3 steps 2-3 | Board can enter reader metrics and view generated rankings |
| Active-series decision (`maintain`/`reschedule`/`cancel`/`change_format`) | 4.1 item 19; Flow 3 steps 5-6 | Board records strategic series decisions |
| Reader poll data entry UI | 4.1 item 17; Flow 3 step 1 | Board enters poll/reader metrics manually |
| Notifications system | 4.1 item 20 | Important workflow transitions create notification records/list items |
| Audit logging | 4.1 item 21 | Sensitive workflow transitions are auditable |
| Editor production/deadline dashboard | 4.2 should-have item 1 | Should-have after core Flow 2 is stable |

### 3-person simplifications no longer apply

| Old reduced behavior | Restored behavior |
|----------------------|-------------------|
| Task assigned at page level only | Task assigned to a page region |
| `region_coordinates = null` or `{"fullPage": true}` | Region stored as entity (x, y, w, h), task linked by regionId |
| Static seed earnings only | Estimated earnings derive from approved tasks and earning rates |
| File upload + text note only | Submission can include file, note, and version/annotation context |
| JS frontend validation only | JS validation plus AI summary preview where applicable |

### Features in restored scope

- Authentication and role-based access (Flow 1 done)
- Admin user/account management (Flow 1 done)
- Admin skill/category management (Flow 1 done)
- Mangaka series proposal creation (Flow 1 done)
- Mangaka manuscript upload (Flow 1 done)
- Tantou Editor proposal review (Flow 1 done)
- Tantou Editor request revision / forward to board (Flow 1 done)
- Editorial Board voting (3-vote auto-decision) (Flow 1 done)
- Mangaka chapter creation (Flow 2 - IN PROGRESS)
- Mangaka page creation (Flow 2 - IN PROGRESS)
- Mangaka region creation / VisualCanvas selection (Flow 2 - IN PROGRESS)
- Mangaka task assignment to Assistant (region-based task assignment) (Flow 2 - IN PROGRESS)
- Assistant task workspace + submission (file + note) (Flow 2 - IN PROGRESS)
- Mangaka task review: approve or redo (Flow 2 - IN PROGRESS)
- Editor annotations (Flow 2/Review - IN PROGRESS)
- Reader metrics and rankings (Flow 3 - IN PROGRESS)
- Active-series decisions (Flow 3 - IN PROGRESS)

## 5. Business flows

### 5.1 Flow 1 - Series Proposal and Approval

1. Mangaka creates a series in `draft`.
2. Mangaka uploads a manuscript draft.
3. Mangaka submits the proposal to Tantou Editor.
4. Tantou Editor reviews the manuscript.
5. Tantou Editor may:
   - request revision for Mangaka; or
   - escalate/forward to Editorial Board.
6. Editorial Board votes: `approve`, `reject`, or `abstain`.
7. Once voting closes:
   - approved proposal becomes `approved`, then the system creates/updates the series using the proposal title as the series title, and the series can enter `serializing`;
   - rejected proposal becomes `needs_revision` by default, unless explicitly archived.
8. Notifications and audit logs are created for major transitions.

### 5.2 Flow 2 - Chapter Production and Task Delegation

1. Only approved/serializing series continue to production.
2. Mangaka creates chapters for approved/serializing series.
3. Mangaka uploads pages for a chapter.
4. Mangaka creates page regions manually.
5. Mangaka assigns region-based tasks to assistants.
6. Assistant views assigned tasks and downloads needed files/references.
7. Assistant submits work as a versioned task submission.
8. Mangaka reviews the submission:
   - approve; or
   - request redo/reject with feedback.
9. Approved tasks contribute to estimated assistant earnings.
10. Page/chapter status progresses based on task completion.

### 5.3 Flow 3 - Ranking and Serialization Decision

1. Editorial Board enters reader poll data for each series and publication cycle.
2. System calculates composite score.
3. System ranks series within the cycle.
4. Low-ranked series may trigger notification/warning.
5. Editorial Board records active-series decision:
   - `maintain`
   - `reschedule`
   - `cancel`
   - `change_format`
6. Decision updates series status/schedule and notifies stakeholders.

## 6. Permission matrix

Legend: `Y` = allowed, `Own` = only records owned/assigned to the actor, `Board` = only boards where user is an active member, `-` = not allowed.

| Capability | Admin | Mangaka | Assistant | Tantou Editor | Editorial Board Member |
|---|---:|---:|---:|---:|---:|
| Manage users/accounts | Y | - | - | - | - |
| Manage skills/categories | Y | - | - | - | - |
| Manage editorial boards/membership | Y | - | - | - | - |
| View audit logs | Y | - | - | Limited | - |
| Create series proposal | - | Y | - | - | - |
| Edit draft proposal | - | Own | - | - | - |
| Upload/resubmit manuscript | - | Own | - | - | - |
| Submit proposal to editor | - | Own | - | - | - |
| Review manuscript | - | - | - | Assigned | - |
| Add editor annotation | - | - | - | Assigned | - |
| Request proposal revision | - | - | - | Assigned | - |
| Escalate proposal to board | - | - | - | Assigned | - |
| Vote on proposal submission | - | - | - | - | Board |
| Finalize proposal vote result | Admin override | - | - | Assigned editor may view | Board |
| Create chapter/page | - | Own approved series | - | - | - |
| Create page region | - | Own page | - | - | - |
| Assign task | - | Own page/region | - | - | - |
| View assigned task | - | - | Own | Monitor | - |
| Submit task work | - | - | Own | - | - |
| Review assistant submission | - | Own task | - | - | - |
| View estimated earnings | - | - | Own | - | - |
| Manage earning rates | Y | - | - | - | - |
| Enter reader poll data | - | - | - | - | Y |
| Generate/view rankings | Y | Own series view | - | Y | Y |
| Record active-series decision | - | - | - | - | Y |
| View notifications | Own | Own | Own | Own | Own |

Service-layer validation is required for ownership and board membership checks. The database enforces structural integrity, not all authorization rules.

## 7. State machines

### 7.1 Series.status

Allowed values:

`draft`, `submitted_to_editor`, `under_editor_review`, `needs_revision`, `escalated_to_board`, `approved`, `serializing`, `on_hold`, `cancelled`, `archived`

| Current | Action | Actor | Guard condition | Next | Side effects |
|---|---|---|---|---|---|
| `draft` | submit proposal | Mangaka | has at least one manuscript draft | `submitted_to_editor` | notify assigned/editor pool, audit |
| `submitted_to_editor` | start review | Tantou Editor | editor assigned | `under_editor_review` | audit |
| `under_editor_review` | request revision | Tantou Editor | annotation/report provided | `needs_revision` | unlock manuscript, notify Mangaka |
| `needs_revision` | resubmit | Mangaka | new manuscript version uploaded | `submitted_to_editor` | notify editor |
| `under_editor_review` | escalate to board | Tantou Editor | editor report present, board assigned | `escalated_to_board` | create board submission, notify board |
| `escalated_to_board` | approve vote result | Editorial Board | voting rule satisfied | `approved` | set schedule, notify Mangaka/editor |
| `approved` | start serialization | Mangaka or Editor | first chapter workspace opened | `serializing` | audit |
| `serializing` | put on hold | Editorial Board | decision recorded | `on_hold` | notify stakeholders |
| `serializing` | cancel | Editorial Board | decision recorded | `cancelled` | set cancelled_at, notify stakeholders |
| any non-final | archive | Admin or authorized workflow | no active workflow conflict | `archived` | audit |

### 7.2 Manuscript.status

Allowed values: `draft`, `submitted`, `under_review`, `needs_revision`, `approved_by_editor`, `escalated`, `archived`

| Current | Action | Actor | Guard | Next | Side effects |
|---|---|---|---|---|---|
| `draft` | submit | Mangaka | file exists | `submitted` | `submitted_at=now`, `locked=true` |
| `submitted` | start review | Tantou Editor | editor assigned | `under_review` | audit |
| `under_review` | request revision | Tantou Editor | feedback exists | `needs_revision` | `locked=false`, notify Mangaka |
| `needs_revision` | upload new version | Mangaka | new file/version | `submitted` | previous version remains locked/archived |
| `under_review` | approve for board | Tantou Editor | report exists | `approved_by_editor` | audit |
| `approved_by_editor` | escalate | Tantou Editor | board submission created | `escalated` | notify board |

### 7.3 Submission.status

Allowed values: `pending`, `voting_open`, `approved`, `rejected`, `closed`

| Current | Action | Actor | Guard | Next | Side effects |
|---|---|---|---|---|---|
| `pending` | open voting | Tantou Editor or Board | board assigned | `voting_open` | notify board members |
| `voting_open` | cast vote | Board member | active member, one vote only | `voting_open` | upsert/record vote |
| `voting_open` | close approved | Board/admin finalizer | quorum + approval rule met | `approved` | series approved, notify |
| `voting_open` | close rejected | Board/admin finalizer | quorum + rejection rule met or deadline reached | `rejected` | series needs_revision by default |
| `approved`/`rejected` | archive workflow record | System | result propagated | `closed` | audit |

### 7.4 Chapter.status

Allowed values: `draft`, `in_progress`, `pages_uploaded`, `tasks_in_progress`, `ready_for_editor`, `published`, `cancelled`

| Current | Action | Actor | Guard | Next |
|---|---|---|---|---|
| `draft` | start production | Mangaka | series approved/serializing | `in_progress` |
| `in_progress` | upload pages | Mangaka | at least one page | `pages_uploaded` |
| `pages_uploaded` | assign tasks | Mangaka | regions/tasks exist | `tasks_in_progress` |
| `tasks_in_progress` | all tasks approved | System | no pending/rejected tasks | `ready_for_editor` |
| `ready_for_editor` | publish/mark done | Editor or Mangaka | production accepted | `published` |

### 7.5 Page.status

Allowed values: `uploaded`, `segmented`, `tasks_assigned`, `tasks_in_progress`, `all_tasks_approved`, `finalized`

| Current | Action | Actor | Guard | Next |
|---|---|---|---|---|
| `uploaded` | create region | Mangaka | valid coordinates | `segmented` |
| `segmented` | assign task | Mangaka | active assistant | `tasks_assigned` |
| `tasks_assigned` | assistant starts/submits work | Assistant/System | task exists | `tasks_in_progress` |
| `tasks_in_progress` | approve all tasks | Mangaka/System | all tasks approved | `all_tasks_approved` |
| `all_tasks_approved` | finalize page | Mangaka | final file/version accepted | `finalized` |

### 7.6 Task.status

Allowed values: `pending`, `in_progress`, `submitted`, `approved`, `rejected`, `redo_requested`, `cancelled`

| Current | Action | Actor | Guard | Next | Side effects |
|---|---|---|---|---|---|
| `pending` | start task | Assistant | assigned_to current user | `in_progress` | audit optional |
| `pending`/`in_progress` | submit work | Assistant | file uploaded | `submitted` | notify Mangaka |
| `submitted` | approve | Mangaka | owns series/task | `approved` | earnings eligible |
| `submitted` | request redo | Mangaka | feedback exists | `redo_requested` | notify Assistant |
| `redo_requested` | resume work | Assistant | assigned_to current user | `in_progress` | audit optional |
| `submitted` | reject final | Mangaka | feedback exists | `rejected` | notify Assistant |
| non-final | cancel | Mangaka/Admin | workflow allowed | `cancelled` | audit |

### 7.7 SeriesDecision.decision

Allowed values: `maintain`, `reschedule`, `cancel`, `change_format`

| Decision | Required fields | Effect |
|---|---|---|
| `maintain` | rationale | series remains `serializing` |
| `reschedule` | `new_schedule`, `effective_from`, rationale | update `series.publish_schedule` |
| `cancel` | `effective_from`, rationale | `series.status=cancelled`, set `cancelled_at` |
| `change_format` | `new_schedule` if schedule changes, rationale | update schedule/format note |

## 8. Voting rules

### 8.1 Proposal submission voting

1. Only active `editorial_board_member` users who are active members of the assigned board can vote.
2. Each board member can cast only one vote per submission.
3. Vote values: `approve`, `reject`, `abstain`.
4. Minimum quorum: at least 50% of active board members must cast a vote, including abstain.
5. Approval rule: approved if `approve` votes are strictly greater than `reject` votes among non-abstain votes and quorum is met.
6. Rejection rule: rejected if `reject` votes are greater than or equal to `approve` votes among non-abstain votes when voting closes.
7. Tie handling: tie is treated as rejection for MVP, with result `needs_revision` rather than permanent archive.
8. Abstain votes count toward quorum but not toward approve/reject majority.
9. Default result after rejection: `Series.status=needs_revision`; permanent archive is a separate admin/board action.
10. Voting closes manually by an authorized board member/admin for MVP. A deadline field may be added later.

### 8.2 Active-series decisions

Active-series decisions are recorded in `series_decisions`. They are separate from proposal votes. If the team wants voting for active-series decisions too, use `series_decision_votes`; otherwise, `decided_by` is the board member recording the meeting outcome.

## 9. Ranking rules

### 9.1 Input metrics

Reader poll data is entered manually per series per publication cycle:

- `reader_votes`
- `likes`
- `shares`
- `sales_units`

All values must be non-negative integers.

### 9.2 Publication cycle format

Use one canonical string format:

- Weekly: `YYYY-Www`, e.g. `2026-W22`
- Monthly: `YYYY-MM`, e.g. `2026-05`

A series can have only one poll record per publication cycle.

### 9.3 Composite score formula

For MVP, use a deterministic weighted score:

```text
composite_score = reader_votes * 0.40
                + likes        * 0.20
                + shares       * 0.20
                + sales_units  * 0.20
```

If the team later normalizes metrics, document the new formula and migration impact.

### 9.4 Ranking and tie-breakers

Rank all series in the same publication cycle by:

1. Higher `composite_score`.
2. Higher `sales_units`.
3. Higher `reader_votes`.
4. Earlier `entered_at`.
5. Lower `series_id` as deterministic final tie-breaker.

`rank_change` definition:

```text
rank_change = previous_cycle_rank_position - current_rank_position
```

Positive means rank improved. Negative means rank dropped. Null means no previous rank exists.

### 9.5 Low-rank warning rule

For MVP:

- A series receives a low-rank warning if it is in the bottom 20% for 3 consecutive cycles.
- A series becomes eligible for board review if it is in the bottom 10% for 5 consecutive cycles.

The system may notify stakeholders but should not auto-cancel a series.

## 10. Earnings rules

1. Earnings dashboard is display-only.
2. No payment transaction, bank transfer, payroll, or royalty distribution is handled.
3. Earnings are estimated from approved task submissions.
4. `earning_rates` define rate per unit and effective date range.
5. Estimated earning for a task:

```text
approved_task_earning = applicable_rate.rate_per_page
```

6. Monthly earnings aggregate approved tasks by `reviewed_at` month.
7. If multiple rates match, choose the rate whose effective period contains `reviewed_at`.

## 11. File and version rules

1. Uploaded file paths are stored as server-controlled paths/URLs.
2. The database stores metadata and path references, not binary files.
3. Manuscripts support versioning by `(series_id, version)`.
4. Task submissions support versioning by `(task_id, version)`.
5. Page versions may represent original, compiled, final, or assistant output files.
6. The MVP does not need real image merging; `page_versions` can store manually uploaded final/compiled outputs.

## 12. Acceptance criteria for core features

### FE-06 - Series Proposal Creation and Editor Submission

- Mangaka can create a draft series.
- Only the owning Mangaka can edit a draft series.
- Mangaka can upload at least one manuscript file.
- Submit action changes series status to `submitted_to_editor`.
- Submission creates notification/audit entry.

### FE-08 - Page Region Segmentation and Task Assignment

- Mangaka can create regions only on pages belonging to own approved/serializing series.
- Region coordinates must be within page boundaries.
- Mangaka can assign a task to an active assistant.
- Task status starts as `pending`.
- Assigned assistant receives notification.

### FE-09 - Assistant Work Review and Validation

- Assistant can view only own assigned tasks.
- Assistant can upload a versioned submission.
- Mangaka can approve or request redo/reject only for own series tasks.
- Approval makes the task eligible for earnings.
- Redo/reject requires feedback.

### FE-18 - Final Verdict and Strategic Voting

- Only active board members of the assigned board can vote.
- One vote per member per submission.
- Voting result follows the quorum and majority rules in this document.
- Approved proposals receive publication schedule.
- Rejected proposals return to `needs_revision` by default.

### FE-19 - Reader Performance Data Input

- Editorial Board member can enter metrics for a series/cycle.
- Metrics must be non-negative.
- Duplicate data for same series/cycle is rejected or updates the existing record according to service design.

### FE-20 - Rankings and Analytics Dashboard

- Rankings are generated from deterministic composite score.
- Tie-breakers are deterministic.
- Rank change is computed relative to previous cycle.
- Low-rank warnings are notifications, not automatic cancellations.

## 13. Requirement-to-table traceability

| Feature | Main tables |
|---|---|
| FE-01 User Account Management | `users`, `audit_logs` |
| FE-02 Fixed Role Assignment and Access Control | `users` |
| FE-03 Editorial Board Lifecycle Management | `editorial_boards`, `board_memberships` |
| FE-04 Skill and Expertise Category Management | `skills`, `user_skills` |
| FE-05 System Oversight and Audit Logging | `audit_logs` |
| FE-06 Series Proposal Creation and Editor Submission | `series`, `manuscripts`, `notifications` |
| FE-07 Draft Iteration and Approved Content Management | `series`, `manuscripts`, `chapters`, `pages` |
| FE-08 Page Region Segmentation and Task Assignment | `chapters`, `pages`, `tasks` |
| FE-09 Assistant Work Review and Validation | `task_submissions`, `task_reviews`, `page_versions` |
| FE-10 Performance Tracking and Risk Alerts | `reader_poll_data`, `series_rankings`, `notifications` |
| FE-11 Assigned Task Workspace | `tasks`, `task_submissions` |
| FE-12 Resource Downloading and Submission Processing | `tasks`, `task_submissions`, `page_versions` |
| FE-13 Status and Approval History Tracking | `tasks`, `task_submissions`, `task_reviews`, `audit_logs` |
| FE-14 Monthly Earnings Dashboard | `tasks`, `task_reviews`, `earning_rates` |
| FE-15 Real-Time Production and Deadline Monitoring | `series`, `chapters`, `pages`, `tasks` |
| FE-16 Direct Markup and Iterative Revision System | `manuscripts`, `editor_annotations` |
| FE-17 Series Escalation and Defensive Reporting | `submissions`, `editor_annotations` |
| FE-18 Final Verdict and Strategic Voting System | `submissions`, `submission_votes`, `series_decisions`, `series_decision_votes` optional |
| FE-19 External Reader Performance Data Input | `reader_poll_data` |
| FE-20 Aggregated Rankings and Analytics Dashboard | `series_rankings`, `series_decisions` |

## 14. Non-functional requirements for MVP

| ID | Requirement |
|---|---|
| NFR-01 | System shall be deployable locally for demo/development. |
| NFR-02 | Uploaded manuscript/task files shall be referenced by server-controlled paths. |
| NFR-03 | Users shall only access data allowed by role and ownership rules. |
| NFR-04 | Sensitive workflow actions shall create audit log entries. |
| NFR-05 | Notifications shall be generated for major status changes. |
| NFR-06 | Database shall enforce primary keys, foreign keys, check constraints, uniqueness, and useful indexes. |
| NFR-07 | The MVP shall not process real financial transactions. |
| NFR-08 | The MVP shall not require native mobile apps or external integrations. |

## 15. Implementation order

1. Align role names in backend/frontend.
2. Create database using the canonical schema file: `database/schema.sql`.
3. Implement auth/fixed role access.
4. Implement proposal workflow.
5. Implement board voting.
6. Implement chapter/page/region/task workflow.
7. Implement task review and earnings estimate.
8. Implement reader poll/ranking/decision workflow.
9. Add notification and audit logging to all critical transitions.

## 16. Open decisions

These should be decided before full implementation:

1. Final DBMS: PostgreSQL is currently configured; adapt if using SQL Server/MySQL.
2. Whether active-series decisions require individual votes or only a recorded meeting outcome.
3. Whether `page_versions` will be exposed in UI during MVP or kept as backend metadata.
4. Whether editor assignment is manual by Admin or automatically selected from active editors.
5. Whether rejected board submissions always return to revision or can be archived directly by board.
