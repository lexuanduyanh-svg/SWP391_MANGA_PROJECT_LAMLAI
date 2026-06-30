# AGENTS.md

This file provides guidance to Codex when working in this SWP391 manga workflow project.

## Project Context

- Main workspace: `SWP391_NEW`
- Remote: `https://github.com/lexuanduyanh-svg/SWP391_MANGA_PROJECT_LAMLAI.git`
- Main branch: `main`
- Current pushed layout commit: `9e39112 refactor: align project layout`
- Working memory: read `CURRENT.md` first before doing meaningful work.

## Current Layout

```text
backend/        Java Spring Boot backend
frontend/       React/Vite frontend
database/       schema/template/migrations/seeds
ai-subsystem/   AI subsystem placeholder
docs/           project documentation
Codex-best-practice/  upstream Codex best-practice reference submodule
```

Backend package root:

```text
backend/src/main/java/com/mangastudio/workflow
```

Backend package groups:

```text
config/
controllers/
services/
repositories/
entities/
dtos/
```

## Working Rules

- Read `CURRENT.md` before architecture, database, or workflow changes.
- Preserve the current backend package root `com.mangastudio.workflow`.
- Do not reintroduce old `com.mangaworkflow` packages.
- Do not move code back under `src/backend` or `src/frontend`.
- Do not edit runtime uploads or generated files under storage folders unless explicitly requested.
- Do not commit or push unless the user explicitly asks.
- If the work affects backend behavior, run `cd backend && .\mvnw.cmd test` when practical.
- If the work affects frontend behavior, run `cd frontend && npm run build` when practical.

## Codex Best Practice Reference

The upstream reference repo is installed as a git submodule at:

```text
Codex-best-practice/
```

Use it as the source of truth for Codex best-practice questions, especially:

```text
Codex-best-practice/README.md
Codex-best-practice/best-practice/
Codex-best-practice/implementation/
Codex-best-practice/reports/
Codex-best-practice/tips/
Codex-best-practice/.Codex/
```

This project intentionally does not copy the upstream repository's full `.Codex/settings.json`, because it contains personal spinner/status-line text and hook commands that are not project-specific.

## Git Safety

- The worktree may contain user or previous-agent changes.
- Never revert unrelated changes unless explicitly requested.
- Keep commits scoped to the requested task.
- Do not commit secrets, local DB files, `node_modules`, `target`, `dist`, uploads, logs, or build output.


