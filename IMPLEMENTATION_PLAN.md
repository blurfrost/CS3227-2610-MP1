# Organise CLI CRUD Implementation Plan

## Status

- [x] Step 1: Replace bare Trip-index selection with `view NUMBER`.
- [ ] Step 2: Add safe Trip and Plan CRUD foundations.
- [ ] Step 3: Add stable displayed Plan mappings.
- [ ] Step 4: Implement the remaining Organise commands.
- [ ] Step 5: Synchronize documentation and complete verification.

Update this checklist after each iterative implementation. Keep prompt history in
`logs/03 - Bare CLI Implementation.md` rather than duplicating it here.

## Locked Decisions

- The Trip list accepts `new`, `edit NUMBER`, `view NUMBER`, `delete NUMBER`, and `back`.
- A viewed Trip accepts `new`, `edit NUMBER`, `delete NUMBER`, and `back` for its Plans.
- `NUMBER` is a required positive one-based index from the currently displayed list.
- Blank input during editing preserves the current field value.
- A Trip date edit is rejected if it would exclude any existing Plan.
- Deletion requires an exact `yes` or `no`; other input causes a reprompt.
- Deleting a Trip removes the complete aggregate, including its Plans.
- Displayed Trip and Plan numbers resolve through retained UUID mappings.
- The centralized repository error boundary remains deferred until persistent storage is introduced.

## Step 2 — Safe CRUD Foundations

### Repository and Application

- [x] Add `TripRepository.delete(UUID tripId)` to remove a complete Trip aggregate.
- [x] Add the Organise `delete NUMBER` Trip flow with exact confirmation.
- Add presentation-independent service operations to edit and delete Trips and Plans by UUID.
- Report missing Trip and Plan identities as invalid user operations without changing stored data.
- Continue saving a Trip and its Plans as one aggregate operation.

### Domain

- Add copy-on-write operations that return a Trip with changed details, a replaced Plan, or a removed Plan.
- Preserve Trip and Plan UUIDs during edits.
- Validate edited Trip titles and inclusive date ranges using the existing domain rules.
- Validate every existing Plan against edited Trip dates and reject the complete edit if any Plan falls outside them.
- Validate an edited Plan's destination, date, and time, including the selected Trip's inclusive date range.
- Ensure a failed repository save leaves the previously stored aggregate unchanged.

### Acceptance Tests

- Cover successful Trip and Plan edits and deletions at the domain and service layers.
- Cover blank or invalid values, missing UUIDs, excluded Plans, and Plan dates outside the Trip range.
- Cover copy-on-write behavior and preservation of stored data after a failed save.
- Cover repository deletion of a Trip and all of its Plans.

## Step 3 — Stable Displayed Plan Mappings

- Extend `CliSession` with an immutable snapshot of displayed Plan UUIDs and one-based lookup.
- Clear displayed Plan mappings whenever navigation leaves or enters a Trip context.
- Centralize selected-Trip rendering in `CliContext`, recording the sorted Plan UUIDs from the same collection passed to `CliFormatter`.
- Resolve future Plan edit and delete targets from that snapshot rather than a freshly sorted list.
- Add tests proving later repository or ordering changes do not alter which displayed Plan an index identifies.

## Step 4 — Remaining Organise Commands

### Parsing

- Parse `edit NUMBER` and `delete NUMBER` according to the active CLI mode.
- Require exactly one positive integer argument and reject missing, extra, zero, negative, or non-numeric arguments with actionable help.
- Keep `new`, `view NUMBER`, `back`, and `exit` behavior unchanged.

### Trip Commands

- `edit NUMBER` resolves a displayed Trip UUID, prompts for title, start date, and end date, and refreshes the Trip list after success.
- Each edit prompt shows the current value; blank input keeps it, while invalid replacement input causes a reprompt.
- `delete NUMBER` resolves a displayed Trip UUID and asks for exact `yes` or `no` confirmation.
- `yes` deletes and refreshes the Trip list; `no` reports cancellation and leaves the Trip unchanged.

### Plan Commands

- `edit NUMBER` in a viewed Trip resolves a displayed Plan UUID, prompts for destination, date, and time, and refreshes the viewed Trip after success.
- Blank input preserves current Plan fields; invalid replacements and out-of-range dates cause a reprompt.
- `delete NUMBER` resolves a displayed Plan UUID and uses the same exact `yes` or `no` confirmation behavior.
- Missing or stale displayed targets produce an actionable error and refresh the appropriate view without prompting for fields or confirmation.

### Acceptance Tests

- Cover parser routing and invalid argument shapes in both Organise contexts.
- Cover successful edits, deletions, cancellation, and invalid confirmation input through the CLI.
- Cover invalid and stale indices without modifying another Trip or Plan.
- Cover refreshed ordering when an edit changes Trip or Plan sort order.

## Step 5 — Documentation and Verification

- Update CLI help text so each context lists all supported commands and syntax.
- Update `DeveloperGuide.md` implemented-feature notes after the remaining commands exist.
- Summarize each completed prompt in `logs/03 - Bare CLI Implementation.md`.
- Review all changed Java production and test code against the SE-EDU Java coding standard.
- Run `git diff --check` and the complete Gradle test suite under Java 25.0.3.
- Leave changes uncommitted and generate `_temp/visual-diff.html` from `HEAD` to `WORKTREE`.

## Future Addition — Finding 2: Application Error Boundary

- [ ] Implement before connecting `SqliteTripRepository` or another fallible external repository.
- Add a small centralized boundary at the CLI/application edge that catches `RepositoryException`.
- Convert repository failures into actionable CLI errors while keeping the application running in the current mode.
- Refresh the relevant menu or selected-Trip view after a failed operation without discarding valid in-memory data.
- Add failing-repository tests for reads, saves, and deletes at the CLI boundary.
- Preserve the wrapped infrastructure cause for diagnostics.
- Do not add retries, a broad exception hierarchy, or logging infrastructure unless concrete persistence requirements justify them.

## Deferred Work

- Dashboard, Gallery, reviews, and SQLite persistence are separate feature work.
- A separate Plan `view NUMBER` command is unnecessary while the selected-Trip view displays every current Plan field.
