# MVP Planning

## Prompt 1 — Plan the CLI Architecture

Define a package-free CLI MVP architecture under `src/main/java/` that separates domain objects, application services, persistence repositories, and mode-specific CLI commands. Use feature modes for Dashboard, Organise, and Gallery, retain UUIDs internally while displaying list numbers, develop against an in-memory repository before adding SQLite, and update project documentation and workflow guidance after approval.

## Prompt 2 — Confirm the Architecture

Confirm the proposed CLI architecture and perform its administrative follow-up: document the design in `DeveloperGuide.md`, direct future prompt summaries to this log, prohibit automatic Git staging and commits, and identify the iterative feature sets that should be implemented and reviewed next.

## Prompt 3 — Require the Git Standard for Commits

Update `AGENTS.md` so that agents use the `/seedu-git-standard` skill whenever explicitly instructed to commit changes, then commit the completed CLI architecture and workflow documentation changes.
