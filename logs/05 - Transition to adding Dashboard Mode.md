# Transition to Adding Dashboard Mode

This log records the transition from the completed Organise CLI slice toward
the future Dashboard mode. Parser decomposition is the first architectural
subtask; TripStatus, injected Clock, Dashboard application queries, and the
Dashboard CLI mode remain later roadmap work.

## Prompt 1 — Specify the Parser-Decomposition Plan

The user requested that `IMPLEMENTATION_PLAN.md` first specify the parser
decomposition requirements and ordered subtasks, followed by iterative
implementation beginning with `ModeCommandParser`. The plan now defines the
parser contract, coordinator responsibilities, mode-specific parsers, shared
indexed parsing, session-aware feedback, acceptance criteria, and verification
requirements. The active prompt log was changed to this file, and `ModeCommandParser`
is the first implementation subtask.

The agreed indexed-command behavior is that zero or multiple arguments produce
`Usage: <command> NUMBER`; one malformed, non-positive, overflowing, or
out-of-range index produces context-aware Trip/Plan feedback; and an in-range
stale UUID retains the existing stale-target error behavior. A one-item range
uses exact-number wording, while larger ranges use `from 1 to N` wording.

## Prompt 1 Outcome — Introduce `ModeCommandParser`

The package-private functional interface was added under `doggo.ui.cli` with
the shared `Command parse(String command)` contract. The existing `Parser` and
all command behavior were intentionally left unchanged so the next iteration
can add concrete mode parsers behind the contract. The implementation plan now
marks this first subtask complete. `./gradlew clean test` passed under Java
25.0.3 and `git diff --check` passed.

## Prompt 2 — Add Mode-Specific Parsers

The user asked to proceed with the next implementation subtask. Three
package-private parsers—`MainCommandParser`, `OrganiseCommandParser`, and
`TripCommandParser`—were added behind `ModeCommandParser`. `Parser` now keeps
input normalization, global `exit`/`back` handling, null-mode compatibility,
and exhaustive mode delegation, while the existing mode-specific command
grammar was moved into the concrete parsers. Indexed-command feedback was not
changed; shared indexed parsing remains the next subtask.

Focused routing tests and the complete `./gradlew clean test` suite passed under
Java 25.0.3, and `git diff --check` passed.

## Prompt 4 — Centralize Positive Out-of-Range Feedback

The user asked to proceed with the next parser subtask. The five indexed
commands now use the centralized Trip/Plan range and no-items messages when a
positive index is outside the displayed snapshot. Their stale UUID checks remain
separate and continue to report disappeared records without prompting or
modifying another record. The empty-list view test was updated to the new
action-specific message. `./gradlew clean test` and `git diff --check` passed
under Java 25.0.3.

## Prompt 5 — Synchronize Parser-Decomposition Documentation

The user asked to proceed after the indexed command updates. The parser
decomposition is now complete: all five indexed commands use centralized
out-of-range feedback, stale-target handling remains intact, and the full Step
6 acceptance criteria are satisfied. `IMPLEMENTATION_PLAN.md` and the roadmap
now mark Step 6 complete; `DeveloperGuide.md` documents the coordinator and
mode-parser architecture while keeping Dashboard and Gallery planned;
`MEMORY.md` records the durable parser design. No further Java behavior was
changed. The full test suite and diff checks passed, and the visual diff was
regenerated.

## Prompt 3 — Share Indexed Parsing and Feedback

The user asked to proceed with the shared indexed-parser subtask. The
implementation added `IndexedEntity`, `IndexedCommandParser`, and
`InvalidIndexCommand`; added displayed Trip/Plan count accessors and
centralized formatter messages; and updated the Organise and Trip parsers to
use the shared helper. Missing or extra arguments still produce usage errors,
while one malformed, non-positive, overflowing, or malformed `view` index now
uses snapshot-aware feedback. Positive but out-of-range command handling and
stale-target branches remain separate follow-up work.

Focused helper and CLI tests cover valid construction, argument shape errors,
empty snapshots, one-item wording, and multi-item Trip/Plan ranges. The clean
Java 25.0.3 test suite and `git diff --check` passed.

## Prompt 6 — Expand the TripStatus and Clock Plan

The user asked to expand `IMPLEMENTATION_PLAN.md` with the requirements before
implementing the next stage. The plan now defines TripStatus boundary rules,
the injected Clock API, deterministic status queries, Main `new` behavior, and
acceptance coverage. Organise filtering is explicitly deferred until Gallery
exists so past Trips remain accessible through the current CLI. No Java code
has been changed; implementation remains the next deferred task.

## Prompt 7 — Add the TripStatus Enum

The user asked to begin Step 7 iteratively by adding the public status type.
The `doggo.domain.TripStatus` enum now defines `PAST`, `CURRENT`, and `FUTURE`,
with focused coverage for its declared values and order. The implementation
plan marks only the enum subtask complete; Trip date classification, Clock
injection, application queries, and CLI changes remain deferred.

## Prompt 8 — Add Domain Trip Status Classification

The user asked to implement the first Step 7 increment. `Trip.statusOn` now
derives `PAST`, `CURRENT`, or `FUTURE` from an inclusive date range and a
supplied reference date, rejecting null input. Domain tests cover past, future,
spanning, boundary, single-day, and null cases. The implementation plan marks
domain classification complete; Clock injection, application queries, and CLI
changes remain deferred.

## Prompt 9 — Inject a Clock into the Application Service

The user asked to proceed with the next Step 7 increment. `DoggoService` now
requires a `Clock`, the composition root supplies the system-default Clock, and
all existing tests use a shared fixed Clock fixture. A null Clock is rejected.
Existing service behavior is unchanged; status-based application queries and
CLI changes remain deferred. The implementation plan marks Clock wiring
complete.

## Prompt 10 — Add Status-Based Application Queries

The user asked to proceed with the next Step 7 increment. `DoggoService` now
provides `getCurrentAndFutureTrips()` and `getPastTrips()`, deriving the
reference date once from its injected Clock and preserving the existing Trip
ordering. Fixed-Clock tests cover filtering and deterministic ordering. Main
mode creation remains the next deferred CLI increment.

## Prompt 11 — Add Main-Mode Trip Creation

The user asked to move to the next Step 7 increment. Main mode now routes the
`new` command to the existing Trip creation command, the Main help text lists
the command, and successful creation explicitly enters Organise before
rendering the refreshed Trip list. Parser and end-to-end CLI tests cover the
new route and mode transition. The TripStatus boundary rules were made
explicit in `DeveloperGuide.md`; Dashboard and Gallery remain deferred.

## Prompt 12 — Audit Step 7 Before Dashboard

The user asked whether the Step 7 plan matches the codebase and whether
Dashboard implementation can begin. The audit confirmed that Trip status
classification, Clock injection, status queries, and Main-mode Trip creation
are implemented and the full test suite passes. Dashboard is not blocked, but
two close-out discrepancies were identified: the existing visual diff is still
unchecked in the plan, and the `getTrips()` acceptance claim is broader than
its focused test coverage. No Java or plan changes were made during this
inspection.

## Prompt 13 — Complete Step 7 Application Coverage

The user asked to implement the remaining Step 7 iteration. Application tests
now explicitly verify that `getTrips()` retains past, current, and future
Trips in deterministic order, and that current-and-future queries include
Trips ending today, starting today, and occurring on a single day. The full
test suite and diff checks pass, and the Step 7 visual-diff checklist item was
completed. Dashboard implementation is the next planned stage.

## Prompt 14 — Revise the Log Commit Message

The user asked for a more concrete commit message than `Record Step 7
completion`. The proposed subject now describes the documentation action as
`Update implementation logs`, with a body quantifying the prompt summaries
being recorded. The log commit remains staged but uncommitted until the user
approves the revised message.

## Prompt 15 — Re-audit Readiness for Dashboard

The user asked to compare the implementation tracker with the codebase and
decide whether Dashboard should be next. No `IMPLEMENTATION_LOG.md` exists;
`IMPLEMENTATION_PLAN.md` is the active tracker. The audit confirmed that Steps
1–7 are implemented and the full test suite passes. Dashboard queries, mode,
session state, formatting, and CLI coverage are not yet implemented, making
them the appropriate next stage. An old Step 5 note and the composite roadmap
item for deferred Organise grouping are stale bookkeeping but do not block
Dashboard planning or implementation.

## Prompt 16 — Plan Dashboard Mode

The user specified a Dashboard that shows today's itinerary, supports
top-level Trip creation, and edits or deletes Plans through the shared data
model. Step 8 was added to `IMPLEMENTATION_PLAN.md`. The design uses a flat
chronological Plan list with the owning Trip shown on each row, a Clock-backed
application query, deterministic cross-Trip ordering, composite Trip/Plan UUID
snapshots, repository-backed cross-mode updates, and iterative CLI delivery.
Trip grouping, Dashboard Plan creation, `view NUMBER`, reviews, persistence,
and JavaFX presentation remain deferred.

## Prompt 17 — Plan the Dashboard Application Query

The user asked for an implementation-ready plan for Step 8 item 1 before any
Java changes, followed by a handoff to the explicitly requested
`gpt-5.6-luna medium` model. The detailed plan specifies a public immutable
`DashboardEntry`, `DoggoService.getDashboardEntries()`, one Clock-derived date
per query, deterministic cross-Trip ordering, four affected Java/test files,
and a focused test matrix. No Java was changed. This agent cannot directly
change the conversation model, and official OpenAI documentation search did
not establish the exact requested model identifier, so implementation remains
deferred pending the external model selection or further instruction.

## Prompt 18 — Implement Dashboard Query Item 1

The user asked to implement the first Step 8 task after finalizing its plan.
Added public immutable `DashboardEntry` and the Clock-backed
`DoggoService.getDashboardEntries()` query. The query filters today's Plans
across all Trips and orders them by time, Trip title, destination, Trip UUID,
and Plan UUID. Focused tests cover validation, empty results, date filtering,
owning context, deterministic tie-breaking, and non-mutation. CLI Dashboard
work remains deferred to later subtasks.

## Prompt 19 — Condense the Implementation Plan

The user asked whether completed work could be cleaned up to reduce repeated
context usage, then requested the cleanup. Condensed `IMPLEMENTATION_PLAN.md`
by replacing detailed completed-step checklists with milestone summaries while
retaining durable decisions, the complete active Step 8 requirements and
subtasks, acceptance criteria, and deferred roadmap items. The current
Dashboard Java changes were left untouched.

## Prompt 20 — Implement Shared Plan Target Snapshot

The user asked to implement the planned second Dashboard subtask. Added the
package-private immutable `PlanTarget` composite identity and migrated CLI
Plan snapshots, selected-Trip rendering, and Plan edit/delete commands to use
Trip UUID plus Plan UUID resolution. Dashboard mode and rendering remain
deferred to later Step 8 subtasks.

## Prompt 21 — Plan Dashboard Mode Navigation

The user asked to plan Step 8's third iteration before implementation. The
planned increment adds Dashboard session state, a Main-to-Dashboard command,
explicit parser delegation, global back/exit behavior, and minimal Dashboard
landing/help text. It also makes unknown-command view selection mode-aware so
Dashboard errors cannot accidentally render or snapshot the Organise Trip
list. Today's itinerary rows and Dashboard `new`, `edit`, and `delete` remain
deferred to their existing later subtasks.

## Prompt 22 — Implement Dashboard Navigation

The user asked to implement the third Dashboard iteration. Added the
`DASHBOARD` CLI mode, Dashboard session reset, Main `dashboard` routing,
explicit parser delegation, Dashboard landing/help text, exhaustive back
navigation, and mode-aware unknown-command rendering. Focused navigation and
parser tests pass; itinerary rendering and Dashboard mutations remain
deferred to later subtasks.

## Prompt 23 — Commit Dashboard Iterations

The user asked to commit the implementation changes while excluding
`IMPLEMENTATION_PLAN.md` and all `logs/` files, using the SE-EDU Git standard.
Committed the Dashboard application query, shared composite Plan targets, and
Dashboard navigation as three focused commits. Documentation remains
unstaged and uncommitted.

## Prompt 24 — Plan Dashboard Rendering

The user asked for an implementation plan for Step 8's fourth iteration.
The planned increment makes `CliContext.dashboardMenu()` query today's ordered
`DashboardEntry` list once, records composite Plan targets from that same
list, and passes it to `CliFormatter` for a numbered time, destination, and
owning-Trip display. It covers empty output, deterministic row formatting,
snapshot alignment, Dashboard entry rendering, and unknown-command refreshes.
Dashboard creation and mutation commands remain deferred.

## Prompt 25 — Implement Dashboard Rendering

The user asked to implement the fourth Dashboard iteration. Wired
`CliContext.dashboardMenu()` to query today's `DashboardEntry` values once,
record composite Trip/Plan targets from that ordered result, and pass the same
entries to `CliFormatter`. Added numbered time, destination, and owning-Trip
output with an explicit empty state, plus formatter, context, and CLI tests.
Dashboard creation and mutation commands remain deferred.

## Prompt 26 — Plan Dashboard Trip Creation

The user asked to plan Step 8 subtask 5 before implementation. The finalized
increment routes exact case-insensitive Dashboard `new` input to the existing
`NewTripCommand`, advertises the command in the Dashboard footer, and verifies
the successful transition to a refreshed Organise view. No new command or
service operation is needed because `NewTripCommand` already persists the Trip,
enters Organise, and clears Dashboard targets. Dashboard Plan creation and Plan
edit/delete commands remain deferred to their later subtasks.

## Prompt 27 — Implement Dashboard Trip Creation

The user asked to implement the planned fifth Dashboard subtask. Dashboard
`new` now delegates to the existing `NewTripCommand`, and the Dashboard footer
advertises Trip creation. Added parser, formatter, and end-to-end CLI coverage
for the successful transition into a refreshed Organise view. Focused tests and
the full `./gradlew clean test` suite pass. Dashboard Plan editing and deletion
remain deferred to subtasks 6 and 7.

## Prompt 28 — Plan Dashboard Plan Editing

The user asked for a plan for the next Dashboard iteration. The proposed sixth
subtask routes Dashboard `edit NUMBER` to the existing `EditPlanCommand`, makes
that command aware of whether Trip or Dashboard initiated it, and centralizes
active-view refresh behavior in `CliContext`. It retains composite UUID target
resolution, existing field prompts, owning-Trip date validation, and the
repository-backed service operation. The test plan covers malformed indexes,
cross-Trip edits, no-op and invalid values, stale targets before and after
prompts, chronological reordering, removal when moved off today, and visibility
through Organise. Dashboard deletion remains deferred to subtask 7.

## Prompt 29 — Implement Dashboard Plan Editing

The user asked to implement the planned sixth Dashboard subtask. Dashboard `edit NUMBER` now
delegates through `DashboardCommandParser` to the shared `EditPlanCommand`, which resolves
composite Trip/Plan targets in either Dashboard or selected-Trip mode. Added
centralized active-view refresh behavior for malformed, invalid, stale,
successful, no-op, reordered, off-today, and late-failing outcomes.
Dashboard edits are visible through Organise, and selected-Trip behavior
remains green. Focused and full clean test suites pass; Dashboard deletion
remains deferred to subtask 7.

## Prompt 30 — Decide Navigation after Dashboard Trip Creation

The user asked whether creating a Trip from Dashboard should keep Dashboard
active or enter Organise. The recommendation is to retain the existing
Organise transition: a new Trip has no Plans and is therefore invisible in
the Plan-centric Dashboard, while Organise confirms the new Trip and provides
the path to open it and add Plans. Dashboard Plan edits remain in Dashboard
because they operate on entries already visible there. Opening the new Trip
directly remains a possible later refinement.

## Prompt 31 — Plan Dashboard Plan Deletion

The user asked to plan the next Dashboard subtask. The proposed seventh
increment routes `delete NUMBER` to the existing `DeletePlanCommand`, makes
that command mode-aware, and reuses the centralized current-view refresh.
The plan preserves exact lowercase `yes`/`no` confirmation, composite UUID
targeting, selected-Trip behavior, and the existing service operation. Tests
cover malformed and empty indexes, deletion, cancellation, reprompting,
sequential renumbering, cross-mode visibility, stale targets before prompts,
and targets that disappear after confirmation.

## Prompt 32 — Implement Dashboard Plan Deletion

The user asked to implement the planned seventh Dashboard subtask. Dashboard `delete NUMBER` now
delegates to the shared `DeletePlanCommand`, which resolves composite Trip/Plan
targets in Dashboard or selected-Trip mode. Exact lowercase confirmation,
cancellation, invalid input, EOF, refreshed numbering, cross-mode visibility,
and stale targets before and after confirmation are covered. The full clean
test suite passes. Dashboard implementation is complete; documentation and
final review remain as the last Step 8 tasks.

## Prompt 33 — Close Dashboard Step 8

The user asked to review `IMPLEMENTATION_PLAN.md` and suggest the next
iteration. The review found that all Dashboard behavior and acceptance tests
were implemented, leaving documentation synchronization and final quality
verification as the next iteration. The proposed work was to update the
Developer Guide and project memory, close Step 8, review Dashboard Java
against the SE-EDU standard, run the full test suite and diff checks, and
generate a visual diff.

## Prompt 34 — Implement Dashboard Step 8 Closure

The user asked to implement the planned Dashboard Step 8 closure. The
implementation updated the Developer Guide, project memory, implementation
plan, and this Dashboard transition log to reflect the completed Dashboard
CLI behavior. The SE-EDU Java review found no additional code changes were
needed; `./gradlew clean test` passed all 82 tests, both whitespace checks
passed, and `_temp/visual-diff.html` was generated successfully.
