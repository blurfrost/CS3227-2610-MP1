# Organise CLI CRUD Implementation Plan

## Status

- [x] Step 1: Replace bare Trip-index selection with `view NUMBER`.
- [x] Step 2: Add safe Trip and Plan CRUD foundations.
- [x] Step 3: Add stable displayed Plan mappings.
- [x] Step 4: Implement the remaining Organise commands.
- [x] Step 5: Stabilize the completed Organise slice, synchronize documentation, and complete verification.

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
- [x] Add presentation-independent Plan deletion by UUID.
- [x] Report missing Trip and Plan identities as invalid user operations without changing stored data.
- [x] Continue saving a Trip and its Plans as one aggregate operation.

### Domain

- [x] Add the copy-on-write operation that returns a Trip without a selected Plan.
- [x] Preserve Trip and Plan UUIDs during edits.
- [x] Validate edited Trip titles and inclusive date ranges using the existing domain rules.
- [x] Validate every existing Plan against edited Trip dates and reject the complete edit if any Plan falls outside them.
- [x] Validate an edited Plan's destination, date, and time, including the selected Trip's inclusive date range.
- [x] Ensure a failed repository save leaves the previously stored aggregate unchanged.

### Acceptance Tests

- [x] Cover successful Trip and Plan edits and deletions at the domain and service layers.
- [x] Cover blank or invalid values, missing UUIDs, excluded Plans, and Plan dates outside the Trip range.
- [x] Cover copy-on-write behavior and preservation of stored data after a failed save.
- [x] Cover repository deletion of a Trip and all of its Plans.

## Step 3 — Stable Displayed Plan Mappings

- [x] Extend `CliSession` with an immutable snapshot of displayed Plan UUIDs and one-based lookup.
- [x] Clear displayed Plan mappings whenever navigation leaves or enters a Trip context.
- [x] Centralize selected-Trip rendering in `CliContext`, recording the sorted Plan UUIDs from the same collection passed to `CliFormatter`.
- [x] Resolve future Plan edit and delete targets from that snapshot rather than a freshly sorted list.
- [x] Add tests proving later repository or ordering changes do not alter which displayed Plan an index identifies.

## Step 4 — Remaining Organise Commands

### Parsing

- [x] Parse `edit NUMBER` and `delete NUMBER` according to the active CLI mode.
- [x] Require exactly one positive integer argument and reject missing, extra, zero, negative, or non-numeric arguments with actionable help.
- [x] Keep `new`, `view NUMBER`, `back`, and `exit` behavior unchanged.

### Trip Commands

- [x] `edit NUMBER` resolves a displayed Trip UUID, prompts for title, start date, and end date, and refreshes the Trip list after success.
- [x] Each edit prompt shows the current value; blank input keeps it, while invalid replacement input causes a reprompt.
- [x] `delete NUMBER` resolves a displayed Trip UUID and asks for exact `yes` or `no` confirmation.
- [x] `yes` deletes and refreshes the Trip list; `no` reports cancellation and leaves the Trip unchanged.

### Plan Commands

- [x] `edit NUMBER` in a viewed Trip resolves a displayed Plan UUID, prompts for destination, date, and time, and refreshes the viewed Trip after success.
- [x] Blank input preserves current Plan fields; invalid replacements and out-of-range dates cause a reprompt.
- [x] `delete NUMBER` resolves a displayed Plan UUID and uses the same exact `yes` or `no` confirmation behavior.
- [x] Missing or stale displayed targets produce an actionable error and refresh the appropriate view without prompting for fields or confirmation.

### Acceptance Tests

- [x] Cover parser routing and invalid argument shapes in both Organise contexts.
- [x] Cover successful edits, deletions, cancellation, and invalid confirmation input through the CLI.
- [x] Cover invalid and stale indices without modifying another Trip or Plan.
- [x] Cover refreshed ordering when an edit changes Trip or Plan sort order.

## Near-term execution checklist

Execute these tasks in order. Do not mark Step 5 complete until every item and
its acceptance coverage is complete.

### 1. Unblock verification with the stale assertion

- [x] Update the failing `CliTest` end-date assertion to expect the already-
  implemented precise message: `Trip end date cannot be before an existing Plan date.`
- [x] Do not change production validation behavior for this task.

### 2. Harden stale-target and refresh behavior

- [x] Verify that a mapped Trip still exists before requesting Trip deletion
  confirmation; stale targets must report an actionable error without prompting.
- [x] Check that the selected Trip still exists before prompting for a new Plan.
- [x] After a late Plan edit or delete failure, refresh the selected-Trip view and
  its displayed Plan mappings; return to Organise if the Trip disappeared.
- [x] Keep stale or missing targets from modifying another Trip or Plan.

### 3. Complete CLI acceptance coverage

- [x] Cover invalid confirmation input followed by a valid `yes` or `no` answer.
- [x] Cover cancelled Plan deletion.
- [x] Cover invalid and stale edit/delete targets without changing another record.
- [x] Assert Trip and Plan reordering after edits, including subsequent displayed
  index resolution.
- [x] Cover missing Trip and Plan identities for service deletion operations.
- [x] Keep parser-routing and malformed-argument assertions in both Organise
  contexts.

### 4. Synchronize documentation and verify the milestone

- [x] Update CLI help text so each context lists all supported commands and syntax;
  current Organise and viewed-Trip help already satisfies this requirement.
- [x] Update `DeveloperGuide.md` implemented-feature notes: Trip and Plan edit/delete
  are implemented; `TripStatus`, reviews, `Clock`, Dashboard, Gallery, and SQLite
  remain planned.
- [x] Summarize each completed prompt in `logs/03 - Bare CLI Implementation.md`.
- [x] Review all changed Java production and test code against the SE-EDU Java
  coding standard.
- [x] Run `git diff --check` and `./gradlew clean test` under Java 25.0.3.
- [x] Generate `_temp/visual-diff.html` from `HEAD` to `WORKTREE`; leave changes
  uncommitted.
- [x] Mark Step 5 complete only after the full suite is green and the acceptance
  checklist above is satisfied.

## Later architectural roadmap

These items follow the near-term Organise stabilization and are deliberately
separate from Step 5.

1. [x] Migrate the default-package code into `doggo.domain`,
   `doggo.application`, `doggo.storage`, and `doggo.ui.cli` before adding more
   feature classes. Adjust cross-package visibility and Javadoc as needed.
2. [ ] When adding the next CLI mode, decompose `Parser` into mode-specific
   parsers so mode/command conditionals do not accumulate prematurely.
3. [ ] Add `TripStatus` with an injected `Clock`, then implement future/current/
   past Organise grouping and its tests.
4. [ ] Add Dashboard queries and its CLI mode over the application services.
5. [ ] Add reviews and Gallery, including completed-Trip filtering and rating/
   review rules.
6. [ ] Add SQLite persistence after the aggregate schema is stable. At that
   point implement the centralized `RepositoryException` boundary before wiring
   the fallible repository into the application, with transactional aggregate
   saves and failing-repository tests.
7. [ ] Build the JavaFX presentation over the established application services.

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
