# Bare CLI Implementation

## Prompt 1 — Plan Trip and Plan Creation

Plan a simple chatbot-style CLI in iterative feature sets. Support creating and listing Trips through Organise, selecting a Trip, and creating and listing Plans. Trips contain a title and inclusive dates; Plans contain a destination, date, and time. Dates must use `DD/MM/YYYY`, times must use `HH:mm`, and invalid logical ranges should be rejected.

## Prompt 2 — Implement Feature Set 1

Implement the confirmed administrative changes and the first reviewable feature set: the Java 25 Gradle application scaffold, chatbot shell, strict input parsing, in-memory Trip domain and repository, Trip creation, and Trip listing. Stop for review before implementing Plan creation.

## Prompt 3 — Refine Feature Set 1 Scope

(Personal note: I found the output generated to a bit too overzealous as it came up with additional code based on the specification discussed earlier. It might be my fault though as the example I provided was pretty long and involved both the feature sets of creating a new Trip and creating a new Plan within a Trip)
Fix Trip creation input so `back` is saved as a title and treated as an invalid date that shows an error and reprompts. Remove Plan-related groundwork and all currently unreachable methods, retain the command pattern, and remove redundant imports. Defer broader test-case discussion to the next prompt.

## Original Prompt 2 — Bare CLI Implementation Specification

```text
All plans and prompt logs now should be stored in logs/03 - Bare CLI Implementation.md
Let's plan a simple CLI implementation based on the simple architecture previously discussed, focusing on building up new features
iteratively:
1. Creating a new Trip using the "organise" command. Specify its title, its start date and end date. (From `DeveloperGuide.md`, you
may edit User Story 2 to remove the destination).
Trip dates should ONLY accept the DD/MM/YYYY format for now.
2. In an existing Trip, create a new Plan. A Plan should specify its destination, date and time.
Plan dates should ONLY accept the DD/MM/YYYY format for now.
Plan times should ONLY accept the HH:mm format for now.
3. View all existing Trip(s) using the "organise" command.

For this CLI implementation, you can create a simple chatbot interface.

Example: (all inputs are prepended with a >, for example "> organise" represents that "organise" was typed)
---doggo v0.1---
Welcome! Available commands are: "organise", "exit"

> organise
---
[MODE: ORGANISE]
There are no Trips planned.
Type "new" to create a new Trip.
Type "back" to go back to the Main Menu.

> new
---
Enter trip name:

> Japan trip
---
Enter trip start date:

> 01/01/2027
---
Enter trip end date:

> 09/01/2027
---
Trip successfully added!
Here are your trips:
1. Japan trip (from 01/01/2027 to 09/01/2027)

View a trip by entering its index number.
Type "new" to create a new Trip.
Type "back" to go back to the Main Menu.

> 1
---
Viewing: Japan trip (from 01/01/2027 to 09/01/2027)
There are no plans!

Type "new" to create a new Plan.
Type "back" to go back to the Organise Menu.

> new
---
Enter plan destination:

> Mount Fuji
---
Enter plan date:

> 05/01/2027
---
Enter plan time:

> 09:00
---
Plan created!
Viewing: Japan trip (from 01/01/2027 to 09/01/2027)
Plans:
1. Mount Fuji (05/01/2027 at 09:00)

Type "new" to create a new Plan.
Type "back" to go back to the Organise Menu.

> back
---
Here are your trips:
1. Japan trip (from 01/01/2027 to 09/01/2027)

View a trip by entering its index number.
Type "new" to create a new Trip.
Type "back" to go back to the Main Menu.

> back
---
[MODE: MAIN MENU]
Welcome! Available commands are: "organise", "exit"

> exit
---
Bye!
(program closes)
```

## Prompt 4 — Preserve Original Prompt 2

Add the complete original Feature Set 1 prompt, including its chatbot interaction example, to this log for reference.

## Prompt 5 — Evaluate Trip Overlaps and Date Tests

Evaluate whether Trips should be allowed to have overlapping date ranges and whether strict date parsing needs additional representative invalid-date tests beyond relying on `DateTimeFormatter` and `DateTimeParseException`.

## Prompt 6 — Group and Commit Uncommitted Changes

Bundle the uncommitted project changes into sensible groups of local commits using the `seedu-git-standard` skill. Propose the grouping and commit messages before implementing them.

## Prompt 7 — Correct Documentation Commit Message Spacing

Correct the spacing and formatting of the documentation commit message, using the explicitly supplied `git commit -m` command and message text.

## Prompt 8 — Implement Feature Set 2

Implement Feature Set 2: select a Trip by its displayed index, view its Plans, and create Plans with a destination, strict `DD/MM/YYYY` date, and strict `HH:mm` time. Require Plan dates to stay within the selected Trip's inclusive range, display Plans chronologically, preserve `back` as a destination, and treat `back` as invalid for date and time prompts. Retain the command pattern, add representative tests, and leave changes uncommitted for review.

## Prompt 9 — Review Feature Set 2

Review the Feature Set 2 implementation, focusing on methods or return values that are not used and whether `TripTest` tests Trip and Plan as appropriate individual units.

## Prompt 10 — Apply Feature Set 2 Review Improvements

Move chronological Plan ordering into `DoggoService`, add standalone Plan unit tests, add an explicit selected-Trip clearing method, and guarantee `HH:mm` input and display behavior. Keep created-object return values and other currently used methods unchanged.

## Prompt 11 — Commit Feature Set 2 Changes

Group the Feature Set 2 changes into logical local commits using the `seedu-git-standard` skill. Propose the commit groups and messages before implementing them, then create them after approval.

## Prompt 12 — Review the Current Architecture

Read `AGENTS.md` and `DeveloperGuide.md`, inspect the current implementation, and review its overall architecture without modifying the Java code. The review found that the current Feature Set 1–2 layering is sound, while identifying four scaling concerns: mutation of repository-owned Trip aggregates before saves succeed, unhandled persistence failures at the CLI boundary, recomputation of displayed indices instead of retaining UUID mappings, and navigation state that can retain a stale selected Trip. The freshly rerun Gradle test suite passed under Java 25.0.3.

### Architecture Findings

- [x] Finding 1: Protect stored Trip aggregates from partial mutation when a repository save fails. Define an unchecked `RepositoryException`, make Trip updates copy-on-write, and later make SQLite aggregate writes transactional.
- [ ] Finding 2: Add an application error boundary so repository failures produce actionable CLI errors instead of terminating the application.
- [x] Finding 3: Store displayed list-number-to-UUID mappings in `CliSession` instead of resolving indices from a freshly fetched and sorted Trip list.
- [ ] Finding 4: Replace independent session mode and selected-Trip mutations with navigation transitions that preserve session invariants and clear stale selections.

## Prompt 13 — Discuss Finding 1

Evaluate the persistence exception and aggregate-update design needed to address failed saves. Prefer an unchecked `RepositoryException` that wraps infrastructure failures and preserves their causes. Make Trip updates copy-on-write now so a failed save cannot mutate the previously stored aggregate, retain `TripRepository.save(Trip)` as the repository API, and make the future SQLite implementation transactional across the whole aggregate. The decision was made to define the exception contract now, but implementation remains deferred until explicitly authorized.

## Prompt 14 — Track Architecture Review Work

Add accessible TODOs for all four architecture-review findings and backfill the recent review and design conversations in this log. Do not implement Finding 1 yet; wait for explicit permission in a later prompt.

## Prompt 15 — Recap Finding 1 Updates

Recap the agreed Finding 1 implementation before coding. Define an unchecked `RepositoryException` that wraps infrastructure failures and preserves causes, while keeping `IllegalArgumentException` for invalid input and domain state. Make `Trip` copy-on-write so service updates create a new aggregate before calling `TripRepository.save(Trip)`. Keep transactional behavior as a future `SqliteTripRepository.save` responsibility covering the complete aggregate. Add failure-preservation tests, but defer all Java implementation until explicit permission.

## Prompt 16 — Implement Finding 1

Implement Finding 1 after receiving authorization. Add the unchecked `RepositoryException` repository contract, make `Trip` updates copy-on-write through `withAddedPlan`, update `DoggoService` and affected tests, and add a failing-save regression test proving the previously stored Trip remains unchanged. Keep SQLite transaction handling deferred until persistent storage is introduced. The full Gradle test suite passed under Java 25.0.3.

## Prompt 17 — Review Finding 1 Copy-on-Write

Review the Finding 1 changes to confirm copy-on-write is implemented correctly. Verify that `Trip.withAddedPlan` returns a new aggregate backed by an immutable copied Plan list, that `DoggoService` saves the new aggregate rather than mutating the repository-owned instance, and that a failed save leaves the previously stored Trip unchanged. The review found no correctness issues, and the freshly rerun Gradle test suite passed under Java 25.0.3.

## Prompt 18 — Clarify Log Guidance

Clarify that commits containing only changes under `logs/` should use concise commit messages without enumerating the logged content. Prompt summaries in Markdown logs should remain explanatory, preserve the original prompt meaning, and include the AI model's response. Update `AGENTS.md` with this guidance because it improves the repository's instructions for maintaining prompt logs.

## Prompt 19 — Commit Documentation Changes

Proceed with the two suggested documentation commits: commit the updated `AGENTS.md` guidance separately from the implementation log, using concise Git-standard messages for the log-only commit.

## Prompt 20 — Assess Finding 2 Urgency

Assess whether an application error boundary is urgent before persistent storage exists and whether implementing it now would be overengineering. The AI reviewed the current exception flow and found that `Doggo` only uses `InMemoryTripRepository`, so no production repository operation presently performs fallible external I/O. Finding 2 is therefore low urgency for the current feature set but becomes mandatory before `SqliteTripRepository` is connected, because `Cli.run` otherwise allows `RepositoryException` to terminate the application. Keep the finding open and implement it with the persistence feature using a small centralized boundary handler and failing-repository tests. Defer broader exception hierarchies, retries, and logging infrastructure unless concrete persistence requirements justify them.

## Prompt 21 — Implement Finding 3

Implement Finding 3 by storing the displayed Trip UUID order in `CliSession`, centralizing Organise-menu rendering so the UUID snapshot and displayed list use the same Trip collection, and resolving `ViewTripCommand` indices through that snapshot. Report a refreshed-menu error if a mapped Trip no longer exists. Add a test proving that a repository reorder after display does not change which Trip index 1 selects. The full Gradle test suite passed under Java 25.0.3.

## Prompt 22 — Commit Finding 3 Changes

Authorize the approved two-commit plan: commit the Finding 3 implementation and regression test together, then commit the implementation-log updates separately with a concise log-only message. Run the full test suite and verify the final worktree is clean.

## Prompt 23 — Implement Finding 4 Navigation Transitions

Proceed with implementing the proposed Finding 4 navigation design. Add intent-specific transition operations to `CliSession` for entering Main, Organise, and Trip states; update navigation commands to use those operations instead of independently mutating `CliMode` and the selected Trip; clear stale selection and displayed Trip mappings during transitions; and add tests covering the resulting session invariants, including null Trip IDs. The implementation was completed, but the Gradle test run was blocked because the wrapper could not create its lock file under `/Users/keith/.gradle`.

## Prompt 24 — Revise CLI Architecture Documentation

Review the `DeveloperGuide.md` CLI and CLI Behaviour wording, specifically whether Organise commands should describe the Trip-and-Plan workflow explicitly and whether “Navigation commands” should be renamed to “Main Menu commands.” The AI recommended describing Organise commands by their selected-Trip workflow, while retaining Trip editing and using “Navigation and global commands” because back and exit are available outside the Main Menu. The guide was updated accordingly; no Java code was changed.

## Prompt 25 — Finalize Organise Command Names and Behaviour

Clarify that Trip and Plan creation should use the `new` command rather than `add`, then authorize the previously agreed documentation update. The AI updated the CLI architecture and CLI Behaviour sections to use `new`, documented explicit Trip-list and viewed-Trip command contexts, retained global command handling, and left the current implemented-feature notes unchanged. No Java code was changed.

## Prompt 26 — Implement Organise View Command Migration

Implement only the first Organise command migration: replace bare Trip-index selection with `view NUMBER`. The parser, Organise help text, Feature Set 2 documentation, and CLI tests were updated; bare numeric input is now rejected while `view NUMBER` selects the displayed Trip. Remaining Organise edit and delete commands remain deferred.

## Prompt 27 — Record the Remaining Organise Implementation Plan

Decide where to retain the detailed remaining Organise implementation steps so future work can reference them before continuing. The AI recommended a dedicated root-level `IMPLEMENTATION_PLAN.md`, with a concise pointer in `MEMORY.md`, while keeping prompt history in this log and requirements in `DeveloperGuide.md`. The plan was created with the completed `view NUMBER` migration marked done and the remaining CRUD foundations, stable Plan mappings, commands, policies, tests, documentation, and verification recorded as an actionable checklist.

## Prompt 28 — Record Finding 2 as Future Work

Review Finding 2 in this log and record it in `IMPLEMENTATION_PLAN.md` as a future addition. The AI added a persistence-boundary checklist covering when to implement it, centralized handling of `RepositoryException`, actionable CLI recovery, failing-repository tests, cause preservation, and the decision to defer retries, broader exception hierarchies, and logging infrastructure. No Java code was changed.

## Prompt 29 — Rename Trip Viewing Command

Rename the previous Trip-selection command and its test to `ViewTripCommand`, update all production and test references and related test names, and preserve the existing `view NUMBER` behavior. The command class, parser dependency, test class, and historical architecture reference were renamed; no command behavior changed.

## Prompt 30 — Implement Trip Deletion

Implement the first deletion slice from `IMPLEMENTATION_PLAN.md`: support `delete NUMBER` in the displayed Organise Trip list, require exact `yes` or `no` confirmation, and delete the selected Trip together with all Plans stored in its aggregate. The repository contract, in-memory repository, service, parser, formatter, and CLI command were updated. Service and CLI tests cover aggregate removal and cancellation. The full Gradle test suite passed, and `git diff --check` passed. Plan deletion and stable displayed Plan mappings remain deferred.

## Prompt 31 — Implement Plan Deletion

Implement Plan deletion so `delete NUMBER` in a viewed Trip removes only the selected Plan. The implementation added stable displayed Plan UUID mappings, centralized selected-Trip rendering, copy-on-write Plan removal in `Trip`, service-level Plan deletion, Trip-mode parsing, and `DeletePlanCommand` confirmation behavior. Tests cover domain copy-on-write behavior, service isolation, displayed Plan lookup, and CLI deletion. The full Gradle test suite passed, and `git diff --check` passed. Trip and Plan editing remain deferred.

## Prompt 32 — Review Remaining Step 2 Work

Walk through the work still required for Step 2 of `IMPLEMENTATION_PLAN.md`. The review found that Trip and Plan deletion foundations are implemented, while Trip editing, Plan editing, edit validation, missing-identity coverage, and failed-save preservation tests remain. Step 2 should stay incomplete until those domain and service foundations and acceptance tests are finished. Step 3's mapping infrastructure is mostly present, but its completion status should be reconsidered separately because the dedicated stable-mapping regression test is still missing.

## Prompt 33 — Distinguish Steps 2 and 4

Explain whether Steps 2 and 4 duplicate each other. The distinction is architectural: Step 2 implements and tests presentation-independent domain, service, and repository behavior for editing and deletion, while Step 4 exposes those operations through mode-specific CLI parsing, prompts, confirmation, index resolution, messages, and refreshed views. They deliver the same user stories in separate layers, allowing Step 2 rules to be reused by the future JavaFX interface.

## Prompt 34 — Implement the Remaining Step 2 Foundations

Implement the remaining presentation-independent Step 2 behavior without adding CLI editing interactions. Add copy-on-write Trip detail updates and Plan replacement, service methods returning updated objects, aggregate date validation, UUID preservation, missing-identity handling, and failed-write regression tests. The full Gradle test suite passed with 54 tests, line-length checks found no violations in changed domain/service/test files, and `git diff --check` passed. Step 2 is complete; Step 3 remains open pending its stable-mapping regression test.

## Prompt 35 — Implement Step 3 Stable Plan Mappings

Implement Step 3 by completing regression coverage for the existing stable displayed Plan UUID mappings. Verify immutable snapshots, clearing during navigation transitions, and deletion of the originally displayed Plan after repository ordering changes. The full Gradle test suite passed with 58 tests, and `git diff --check` passed. Step 3 is complete; CLI editing remains deferred to Step 4.

## Prompt 36 — Commit CRUD and Mapping Changes

Commit the completed CRUD and stable Plan-mapping work in logical groups using the `seedu-git-standard` skill. The changes were split into safe CRUD foundations, stable Plan mappings, Plan deletion command behavior, and implementation tracking. The first three commits were created with imperative subjects and explanatory bodies; the tracking commit remains to be created after this log update.
