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
