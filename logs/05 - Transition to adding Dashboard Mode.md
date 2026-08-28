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
