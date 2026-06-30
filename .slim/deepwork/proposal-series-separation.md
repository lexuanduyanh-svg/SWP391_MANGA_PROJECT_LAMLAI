# Deepwork: TÃ¡ch Proposal vÃ  Series

## Goal

TÃ¡ch concept **Proposal** (báº£n Ä‘á» xuáº¥t, Flow 1) vÃ  **Series** (bá»™ truyá»‡n chÃ­nh thá»©c, Flow 2) thÃ nh 2 entity riÃªng trong DB vÃ  service.

## Current State

Hiá»‡n táº¡i `series` table lÃ  unified: lÆ°u cáº£ proposal data láº«n series data.
- `ProposalRecord` (in-memory) lÆ°u proposal + metadata (target_audience, manuscript)
- `SeriesEntity` (DB) lÆ°u proposal state + `ProposalMetadata` map in-memory cho manuscript fields
- `board_votes` â†’ FK `series_id` (vote trÃªn proposal)
- `chapters` â†’ FK `series_id` (chapter trong series)
- ProductionService.`ensureAllowed()` dÃ¹ng `proposals.getById()` check proposal status == Approved

### In-memory mode
- `InMemoryMangaProposalService.proposals` map: `ProposalRecord` (full proposal fields)
- `InMemoryMangakaProductionService.chapters` map: `ChapterRecord` with `proposalId` field
- seed: 4 proposals + 2 chapters for demo

### DB mode
- `InMemoryMangaProposalService.createDb()` táº¡o `SeriesEntity` with status DRAFT
- Manuscript metadata lÆ°u á»Ÿ `ProposalMetadata` in-memory map, KHÃ”NG xuá»‘ng DB
- `InMemoryMangakaProductionService.createChapterDb()` tÃ¬m `SeriesEntity` theo proposalId, gÃ¡n chapter vÃ o series Ä‘Ã³

## Target Design

```
proposals (Flow 1)
â”œâ”€â”€ proposal_id, mangaka_id, tantou_editor_id  
â”œâ”€â”€ title, genre, target_audience, synopsis
â”œâ”€â”€ manuscript_title, manuscript_summary, manuscript_file_name, manuscript_version, manuscript_uploaded_at
â”œâ”€â”€ status: DRAFT â†’ SUBMITTED_TO_EDITOR â†’ REVISION_REQUESTED â†’ UNDER_BOARD_REVIEW â†’ APPROVED | REJECTED
â”œâ”€â”€ editor_notes, created_at, updated_at
â”‚
â””â”€â”€ board_votes.proposal_id (FK â†’ proposals)

     â†“ Khi approved â†’ system auto-create:

series (Flow 2)
â”œâ”€â”€ series_id, proposal_id (FK â†’ proposals), mangaka_id, tantou_editor_id
â”œâ”€â”€ title, genre, synopsis (copy tá»« proposal)
â”œâ”€â”€ publishing_frequency
â”œâ”€â”€ status: ACTIVE, HIATUS, COMPLETED, CANCELLED
â”œâ”€â”€ created_at, updated_at
â”‚
â”œâ”€â”€ chapters.series_id (FK â†’ series)
â”‚   â””â”€â”€ pages.chapter_id
â”‚       â””â”€â”€ tasks.page_id
â””â”€â”€ reader_metrics.series_id (FK â†’ series)
```

## File-by-file changes

### 1. `database/schema.sql`
- New `proposals` table (all proposal fields INCLUDING manuscript metadata)
- Modify `series` table: thÃªm `proposal_id` FK, bá» proposal status enum, thay báº±ng ACTIVE/HIATUS/COMPLETED/CANCELLED, bá» `editor_notes`
- Modify `board_votes`: Ä‘á»•i `series_id` â†’ `proposal_id`
- Keep `chapters`, `pages`, `tasks`, `submissions`, `reader_metrics`, `annotations` unchanged
- Drop `chk_series_status` old constraint, add new one
- DROP order: update for new table

### 2. `ProposalEntity.java` (NEW)
```java
@Entity @Table(name="proposals")
- id (proposal_id)
- mangaka (@ManyToOne UserEntity)
- tantouEditor (@ManyToOne UserEntity, nullable)
- title, genre, targetAudience, synopsis
- manuscriptTitle, manuscriptSummary, manuscriptFileName
- manuscriptVersion, manuscriptUploadedAt
- status, editorNotes
- createdAt, updatedAt
```

### 3. `ProposalRepository.java` (NEW)
- `findByMangaka_EmailIgnoreCaseOrderByUpdatedAtDesc(String email)`
- `findByStatusInOrderByUpdatedAtDesc(List<String> statuses)`
- `findByIdAndMangaka_EmailIgnoreCase(Long id, String email)`
- `findByTantouEditor_EmailIgnoreCaseOrderByUpdatedAtDesc(String email)`

### 4. `SeriesEntity.java` (MODIFIED)
- Add `@ManyToOne ProposalEntity proposal`
- Remove `editorNotes` field
- Change `status` column: `ACTIVE` default instead of `DRAFT`

### 5. `BoardVoteEntity.java` (MODIFIED)
- Change `@ManyToOne @JoinColumn(name="series_id") SeriesEntity series` â†’ `@ManyToOne @JoinColumn(name="proposal_id") ProposalEntity proposal`
- Getters/setters: `getProposal()` / `setProposal()`

### 6. `BoardVoteRepository.java` (MODIFIED)
- `existsBySeries_IdAndBoardMember_EmailIgnoreCase` â†’ `existsByProposal_IdAndBoardMember_EmailIgnoreCase`
- `findBySeries_Id` â†’ `findByProposal_Id`
- `findBySeries_IdAndBoardMember_EmailIgnoreCase` â†’ `findByProposal_IdAndBoardMember_EmailIgnoreCase`

### 7. `SeriesRepository.java` (MODIFIED)
- Remove `findByStatusInOrderByUpdatedAtDesc` (proposal status queries move to ProposalRepository)
- Remove `findByMangaka_EmailIgnoreCaseOrderByUpdatedAtDesc` (moved to ProposalRepository)
- Remove `findByTantouEditor_EmailIgnoreCaseOrderByUpdatedAtDesc` (moved to ProposalRepository)
- Remove `findByIdInOrderByUpdatedAtDesc`
- Keep `findByIdAndMangaka_EmailIgnoreCase`
- Add `findByMangaka_EmailIgnoreCaseOrderByUpdatedAtDesc` for series listing (mangaka's series)
- Keep simple CRUD from JpaRepository

### 8. `InMemoryMangaProposalService.java` (MAJOR MODIFY)
- Add `ProposalRepository proposalRepository` field
- `schemaMode()`: also check proposalRepository != null
- `createDb()`: create ProposalEntity instead of SeriesEntity; manuscript fields go into ProposalEntity columns (not in-memory map)
- `updateDb()`: update ProposalEntity
- `submitDb()`: update ProposalEntity status
- `forwardToBoardDb()`: update ProposalEntity
- `requestRevisionByEditorDb()`: update ProposalEntity
- `rejectByEditorDb()`: update ProposalEntity
- `voteByBoardDb()`:
  - BoardVoteEntity.proposal = proposalEntity (was series)
  - Khi Ä‘á»§ vote vÃ  approved â†’ auto-create SeriesEntity:
    ```java
    SeriesEntity series = new SeriesEntity();
    series.setProposal(proposal);
    series.setMangaka(proposal.getMangaka());
    series.setTantouEditor(proposal.getTantouEditor());
    series.setTitle(proposal.getTitle());
    series.setGenre(proposal.getGenre());
    series.setSynopsis(proposal.getSynopsis());
    series.setPublishingFrequency("WEEKLY");
    series.setStatus("ACTIVE");
    series.setCreatedAt(LocalDateTime.now());
    series.setUpdatedAt(LocalDateTime.now());
    seriesRepository.save(series);
    ```
  - Return proposal DTO with seriesId included
- `deleteDb()`: delete from ProposalRepository
- `attachManuscriptMetadataDb()`: update ProposalEntity manuscript fields directly
- `listByAuthorEmailDb()`: query ProposalRepository
- `listForEditorDb()`: query ProposalRepository
- `listForBoardDb()`: query ProposalRepository by status
- `toDto(ProposalEntity)`: read manuscript fields from entity columns, not in-memory metadata
- `getRequiredProposal()`: proposalRepository.findById
- `getRequiredProposalForAuthor()`: proposalRepository.findByIdAndMangaka_EmailIgnoreCase
- `toDto(ProposalEntity)`: map all fields to MangaProposalDto
- `mapProposalStatus()` / `toProposalStatus()`: similar to current `mapSeriesStatus()` / `toSeriesStatus()`
- `seedDbIfEmpty()`: seed ProposalEntities instead of SeriesEntities (also create SeriesEntity for the Approved one equivalently)
- Remove `ProposalMetadata` class and `seriesMetadata` map (manuscript fields now in ProposalEntity columns)
- DB seed: also create series for the approved seed

### 9. `InMemoryMangakaProductionService.java` (MODIFY)
- **Bá»** `InMemoryMangaProposalService proposals` dependency
- **ThÃªm** `ProposalRepository proposalRepository` (optional, nullable)
- `ensureAllowed()`:
  - Now takes seriesId (String), looks up from SeriesRepository
  - Checks: series exists, series.mangaka email matches, series.status == "ACTIVE"
- `createChapterDb()`: 
  - Takes seriesId, finds SeriesEntity directly (no longer resolves from proposal)
  - Chapter entity's series FK = series
- `listChaptersDb()`: similar, use seriesId
- Controller path changes: `/api/mangaka/proposals/{proposalId}` â†’ `/api/mangaka/series/{seriesId}` (or keep both? Keep clean: change to series)
- Memory mode: `seedMemory()` adjust chapter's proposalId â†’ seriesId concept
- In-memory chapters still use internal IDs, but `ensureAllowedMemory()` checks if there's a matching series or approved proposal

Wait, actually for memory mode, we should keep backward compatibility:
- After proposal approval in memory mode â†’ auto-create a virtual "series" (maybe just a flag or a mapping)
- Chapters continue to use proposalId internally
- `ensureAllowedMemory()` checks if proposal is approved AND a series was created

Actually, let me simplify. In memory mode:
1. Proposal creates ProposalRecord
2. After approval â†’ system creates SeriesRecord (new class or just mark the ProposalRecord as "hasSeries")
3. Chapters still reference the proposalId (as a series identifier)
4. `ensureAllowedMemory()` checks the proposal is approved and belongs to the mangaka (same as before)

In DB mode: full separation.

Hmm, but this creates confusion. Let me think...

Actually, the simplest approach for memory mode: just keep it as-is but add a `seriesCreated` flag or mapping. When a proposal is approved, the approval method returns a seriesId (which could be the same as the proposalId in memory mode, or a new ID).

For API consistency, in memory mode:
- After proposal approved â†’ create a SeriesRecord in a new `seriesRecords` map
- SeriesRecord has: id, proposalId, title, status=ACTIVE, mangakaEmail
- `createChapterMemory()` takes seriesId, finds SeriesRecord
- Chapters store seriesId (not proposalId)

This makes memory mode consistent with DB mode.

### 10. `MangakaProductionController.java` (MODIFY)
- Change `@RequestMapping("/api/mangaka/proposals/{proposalId}")` â†’ `@RequestMapping("/api/mangaka/series/{seriesId}")`
- All methods: `proposalId` â†’ `seriesId`

### 11. Tests (NO CHANGE EXPECTED)
- All tests use memory mode (no DB)
- Memory mode behavior should remain backward compatible
- Run `mvn test` â†’ 29/29 expected

## Key Design Decisions

1. **Manuscript metadata in ProposalEntity columns**: instead of separate in-memory map, store target_audience, manuscript_* directly in `proposals` table. This resolves the long-standing bug where DB mode lost manuscript metadata.

2. **Series auto-creation at approval**: happens in `voteByBoardDb()` inner logic when APPROVED status is set. No new API endpoint needed.

3. **ProductionService path change**: `/api/mangaka/proposals/{proposalId}` â†’ `/api/mangaka/series/{seriesId}`. Breaking change but architecturally correct.

4. **Memory mode consistency**: Add `SeriesRecord` mapping in memory mode for consistency with DB mode flow.

## Risks & Mitigations

| Risk | Mitigation |
|---|---|
| Memory mode seed data (4 proposals, 2 chapters) still works | Keep seed logic, adjust chapter's parent to use seriesId post-seed |
| Tests may check proposalId in production flow | Tests use memory mode; adjust test seed to create series after approval |
| Frontend API path change | Deliberate; frontend owner adapts |
| Manuscript metadata lost in DB mode | Now stored in ProposalEntity columns â€” permanent fix |


