---
name: Codex-best-practice
description: Use when answering questions or making changes related to Codex agents, commands, skills, MCP, hooks, settings, memory, workflows, or best-practice configuration in this project.
allowed-tools: Read, Glob, Grep, WebFetch
---

# Codex Best Practice Skill

The upstream best-practice repository is installed as a submodule at:

```text
Codex-best-practice/
```

Use it as the first source for Codex configuration and workflow guidance.

## Search Order

1. `Codex-best-practice/README.md`
2. `Codex-best-practice/best-practice/`
3. `Codex-best-practice/implementation/`
4. `Codex-best-practice/reports/`
5. `Codex-best-practice/tips/`
6. `Codex-best-practice/.Codex/`

## Project Policy

- Keep this project's `.Codex/` configuration minimal and project-specific.
- Do not copy upstream `.Codex/settings.json` wholesale.
- Do not enable upstream hooks unless the user explicitly asks.
- Do not overwrite `AGENTS.md` with upstream content.
- Prefer commands/agents/skills that reference the submodule rather than duplicating large upstream docs.


