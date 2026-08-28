# Organise CLI CRUD and Dashboard Implementation Plan

## Status

- [x] Steps 1–7: Organise CRUD, parser decomposition, and Trip status foundation
- [x] Step 8: Add Dashboard queries and its CLI mode

Keep completed implementation detail in the prompt logs and Git history. Keep
this file focused on durable decisions and future milestones.

## Completed Milestones

### Steps 1–5 — Organise CRUD and Stabilization

- Trip and Plan creation, editing, deletion, confirmation, validation, and
  copy-on-write aggregate updates are implemented.
- Trip and Plan indexes resolve through retained UUID snapshots, including
  stale-target handling and refreshed ordering after mutations.
- Main, Organise, and selected-Trip CLI flows have parser, command, help, and
  acceptance coverage.
- Failed edits preserve the stored aggregate; Trip deletion removes its Plans.
- Documentation, Java-standard review, full tests, diff checks, and visual
  review were completed for the milestone.

### Step 6 — Decompose the CLI Parser

- `Parser` retains input normalization and global `exit`/`back` handling while
  delegating mode-specific commands through `ModeCommandParser`.
- Main, Organise, and selected-Trip parsing are separated into package-private
  parsers; indexed parsing and invalid-index feedback are shared.
- Existing valid behavior and malformed, empty, out-of-range, and stale-index
  behavior are covered by the full test suite.

### Step 7 — Trip Status and Injected Clock

- Public `TripStatus` has `PAST`, `CURRENT`, and `FUTURE` values.
- `Trip.statusOn(LocalDate)` applies inclusive date boundaries without
  persisting derived status.
- `DoggoService` receives an injected `Clock`; date-sensitive queries use it.
- All-Trips, current-and-future, and past queries have deterministic ordering.
- Main `new` creates a Trip and enters Organise; Organise and selected-Trip
  `new` behavior remains unchanged.
- Organise still shows all Trips until Gallery is available.

## Durable Decisions

- A Trip is the aggregate root and owns zero or more Plans.
- Dashboard is Plan-centric: it shows one flat chronological list of today's
  Plans, with the owning Trip title on each row. It is not grouped by Trip.
- `NUMBER` is a positive one-based index into the currently displayed snapshot.
- Displayed Trip and Plan numbers resolve through retained UUID mappings.
- Blank edit input preserves the current field value; invalid replacements
  reprompt.
- Trip dates are inclusive. A Trip edit cannot exclude an existing Plan.
- Deletion confirmation accepts only exact `yes` or `no`; other input reprompts.
- Application operations remain repository-backed. SQLite persistence and a
  centralized repository error boundary are deferred until the persistence
  milestone.

## Step 8 — Add Dashboard Queries and CLI Mode — Complete

Dashboard presents today's Plans from every Trip and permits Plan mutations
that update the owning Trip aggregate. The query, CLI integration, and
acceptance coverage are complete.

### Requirements

- `DashboardEntry` is a public immutable result containing `UUID tripId`,
  `String tripTitle`, and the complete `Plan`.
- `DoggoService.getDashboardEntries()` derives today's date once from the
  injected Clock, filters all Trip Plans, and returns deterministic ordering:
  Plan time, Trip title, destination, Trip UUID, then Plan UUID.
- Render a flat numbered list showing each Plan's time, destination, and Trip
  title, with an explicit empty state.
- Add `DASHBOARD`, `DashboardCommandParser`, parser delegation, Main
  `dashboard` routing, and help text. Dashboard `back` returns to Main and
  global `exit` remains available.
- Top-level `new` in Dashboard creates a Trip through `NewTripCommand` and
  enters Organise. Dashboard does not create a Plan without a selected Trip.
- Dashboard `edit NUMBER` and `delete NUMBER` operate on the selected Plan's
  owning Trip aggregate through existing service APIs.
- Dashboard Plan edits may use any valid date inside the owning Trip. If the
  new date is not today, the Plan disappears after refresh.
- Use one immutable composite Trip UUID/Plan UUID target snapshot for selected
  Trip and Dashboard Plan indexes.
- Missing Trip or Plan identities are stale targets: give actionable feedback,
  do not prompt or mutate another record, and refresh the Dashboard.
- Re-render after successful, cancelled, invalid, or late-failing mutations so
  numbering matches the latest snapshot.
- Defer Dashboard `view`, Dashboard Plan creation, grouping, reviews, and
  JavaFX presentation.

### Ordered Subtasks

1. [x] Add `DashboardEntry` and the Clock-backed today's-Plans query with
   filtering and deterministic-order tests across multiple Trips.
2. [x] Introduce a shared composite Plan target snapshot and migrate existing
   selected-Trip Plan index resolution without changing Organise behavior.
3. [x] Add Dashboard session state, navigation, parser delegation, Main
   `dashboard` routing, and help text.
4. [x] Add centralized Dashboard rendering in `CliContext` and `CliFormatter`,
   recording targets from the same ordered entries that are displayed.
5. [x] Route Dashboard `new` to `NewTripCommand` and test the transition to
   Organise after successful Trip creation.
6. [x] Add Dashboard Plan editing with shared validation, stale-target and
   refresh behavior, reordering, and removal when moved off today.
7. [x] Add Dashboard Plan deletion with exact confirmation, cancellation,
   stale-target handling, and refreshed numbering.
8. [x] Add parser, session, formatter, command, cross-mode, and end-to-end CLI
   tests, including empty and equal-time ordering cases.
9. [x] Update `DeveloperGuide.md`, `MEMORY.md`, and the active prompt log after
   Dashboard behavior is complete.
10. [x] Review changed Java against the SE-EDU standard, run
    `./gradlew clean test` and `git diff --check`, and regenerate the visual
    diff.

### Completed Subtask 7 — Dashboard Plan Deletion

#### Design

- Route Dashboard `delete NUMBER` through `IndexedCommandParser` to the
  existing `DeletePlanCommand`; do not add a Dashboard-specific command
  or another application service operation.
- Make `DeletePlanCommand` mode-aware using the same composite target
  rules as editing. Dashboard resolves the owning Trip from `PlanTarget`;
  selected-Trip mode additionally requires `selectedTripId` to match.
- Verify the target Trip and Plan still exist before prompting. Keep the
  existing trimmed, case-sensitive confirmation contract: only lowercase
  `yes` deletes, lowercase `no` cancels, and every other value reprompts.
- Preserve end-of-input behavior during confirmation: return `Bye!` and exit
  without deleting.
- Refresh the initiating mode through `CliContext.refreshCurrentView()` after
  deletion, cancellation, invalid indexes, stale targets, and late service
  rejection. Dashboard stays active; selected-Trip mode retains its
  Organise fallback if the selected Trip disappears.
- Advertise `delete NUMBER` only when Dashboard contains Plans. Keep
  Dashboard Plan creation, view, grouping, and reviews deferred.

#### Target and Refresh Behavior

- Resolve the displayed number once through the retained Trip/Plan UUID pair;
  never re-resolve against repository order before deletion.
- On confirmed deletion, call `DoggoService.deletePlan(tripId, planId)`.
  Refresh from the repository so the removed row disappears and all remaining
  rows and target numbers are rebuilt in deterministic Dashboard order.
- If the Trip or Plan is stale before confirmation, do not prompt or mutate.
  If it disappears after confirmation, show the service error and refresh
  Dashboard without deleting another record.
- Cancellation must retain the selected Plan and still refresh numbering from
  the latest repository state.

#### Tests

- Cover valid and mixed-case command keywords plus malformed, non-numeric,
  zero, negative, overflow, out-of-range, and empty-Dashboard indexes.
- Cover exact `yes` deletion, exact `no` cancellation, invalid and
  mixed-case confirmations that reprompt, and end-of-input exit behavior.
- Delete one Plan from a multi-Trip Dashboard and verify the owning Trip alone
  changes, the row disappears, and Organise observes the same aggregate.
- Perform sequential indexed deletions after a refresh to prove renumbered
  rows retain the correct composite targets.
- Cover missing Trip and missing Plan targets before prompting, plus a target
  that becomes stale after confirmation but before the service operation.
- Preserve existing selected-Trip deletion and confirmation behavior.
- Run focused CLI tests followed by `./gradlew clean test` and
  `git diff --check` during implementation.

#### Completion Criteria

- Dashboard deletion reuses the existing command and service API.
- Every non-exit outcome refreshes the initiating view and target snapshot.
- Confirmation, stale-target, and sequential-numbering behavior cannot delete
  a different Plan or Trip.
- Existing Main, Organise, and selected-Trip behavior remains unchanged.

### Completed Subtask 6 — Dashboard Plan Editing

#### Design

- Route Dashboard `edit NUMBER` through `IndexedCommandParser` to the existing
  `EditPlanCommand`; do not introduce a Dashboard-specific edit command.
- Add one `CliContext.refreshCurrentView()` helper that renders Main, Organise,
  Dashboard, or the selected Trip and refreshes the corresponding UUID
  snapshot. Migrate unknown, malformed, and invalid-index feedback to this
  helper so Dashboard errors cannot render Organise or retain stale numbering.
- Make `EditPlanCommand` mode-aware. Both Trip and Dashboard modes resolve the
  displayed `PlanTarget` to its owning Trip and Plan before prompting; only
  Trip mode additionally requires the target Trip to match `selectedTripId`.
- Keep the shared destination, date, and time prompts and
  `DoggoService.editPlan(...)`. Dashboard dates are validated against the
  owning Trip's inclusive dates, not restricted to today.
- Refresh the initiating view after no changes, success, validation failure,
  stale targets, and late service rejection. Dashboard remains active; Trip
  mode retains its existing fallback to Organise if its selected Trip vanishes.
- Advertise `edit NUMBER` in populated Dashboard output. Keep Dashboard
  deletion deferred to subtask 7.

#### Target and Refresh Behavior

- Resolve the one-based number only through the retained composite target; do
  not re-resolve it against a newly sorted Dashboard list before mutation.
- If the target Trip or Plan is missing before prompting, report an actionable
  stale-target error, perform no prompts or mutation, and refresh Dashboard.
- If the Trip or Plan disappears after prompting, surface the service error and
  refresh Dashboard without mutating another aggregate.
- If an edit changes ordering fields, rebuild the Dashboard snapshot from the
  newly sorted query. If its date moves off today, omit it from the refreshed
  Dashboard while retaining the update in its owning Trip.

#### Tests

- Cover valid, mixed-case, malformed, non-numeric, zero, negative, overflow,
  and out-of-range Dashboard edit input, including Dashboard-preserving error
  rendering.
- Cover destination/date/time updates, blank no-op input, syntactically invalid
  values, and dates outside the owning Trip that reprompt before saving.
- Cover cross-Trip targeting, chronological reordering with refreshed indexes,
  and removal from Dashboard when a Plan moves off today.
- Verify the owning Trip aggregate is updated and that the same change is
  visible after navigating to Organise and opening that Trip.
- Cover missing Trip and missing Plan targets without prompts, plus a target
  that becomes stale after prompts but before the service update.
- Preserve existing selected-Trip edit behavior and its stale-target tests.
- Run focused CLI tests followed by `./gradlew clean test` and
  `git diff --check` during implementation.

#### Completion Criteria

- Dashboard editing reuses the existing command, validation, and service API.
- Every outcome refreshes the initiating view and its target snapshot.
- Reordering or removal never causes a subsequent number to target the wrong
  Plan or Trip.
- Existing Main, Organise, and selected-Trip behavior remains unchanged.

### Completed Subtask 1 — Application Query

- Added `DashboardEntry` with null checks, trimmed non-blank Trip titles, and
  immutable record semantics.
- Added `DoggoService.getDashboardEntries()` using the injected Clock and a
  deterministic cross-Trip comparator.
- Covered empty repositories, Trips without Plans, date filtering, ordering,
  owning context, identity retention, and non-mutation behavior.

### Completed Subtask 5 — Dashboard Trip Creation

- Dashboard `new` delegates to the existing `NewTripCommand`, preserving the
  shared validation and repository-backed Trip creation flow.
- Successful creation enters Organise and refreshes its Trip list; Dashboard
  advertises the command in its footer.
- Parser, formatter, and end-to-end CLI transition tests pass.

### Acceptance Criteria

- [x] Dashboard displays only today's Plans in deterministic chronological
  order and identifies each owning Trip.
- [x] Main enters Dashboard, Dashboard `back` returns to Main, and global
  `exit` remains available.
- [x] Dashboard `new` creates a Trip and enters Organise.
- [x] Dashboard `edit NUMBER` updates the owning aggregate and refreshes or
  removes the row according to its edited date.
- [x] Dashboard `delete NUMBER` changes only the selected Plan after exact
  confirmation and refreshes the list.
- [x] Organise observes Dashboard edits and deletions through the shared
  repository without synchronization code between modes.
- [x] Empty, malformed, out-of-range, stale, cancelled, and reordered cases
  retain actionable feedback and stable UUID targeting.
- [x] Existing Main, Organise, Trip, CRUD, parser, and navigation behavior
  remains green.
- [x] The complete test suite and diff checks pass under Java 25.0.3.

## Future Architectural Roadmap

1. [x] Complete Dashboard queries and CLI mode (Step 8).
2. [ ] Add reviews and Gallery, including completed-Trip filtering and rating
   and review rules.
3. [ ] Add SQLite persistence after the aggregate schema is stable. Before
   wiring a fallible repository, add a centralized `RepositoryException`
   boundary with CLI error handling and failing-repository tests.
4. [ ] Build the JavaFX presentation over the established application services.

## Deferred Work — Application Error Boundary

- Implement before connecting `SqliteTripRepository` or another fallible
  external repository.
- Catch `RepositoryException` at the CLI/application edge, preserve its cause
  for diagnostics, and refresh the current mode or selected-Trip view.
- Add failing-repository tests for reads, saves, and deletes.
- Do not add retries, a broad exception hierarchy, or logging infrastructure
  without concrete persistence requirements.

## Deferred Work — Product Features

- Gallery is responsible for completed Trips and their reviews.
- A separate Plan `view NUMBER` command is unnecessary while the selected-Trip
  view displays all current Plan fields.
- Dashboard Plan creation is deferred because Dashboard has no selected Trip.
