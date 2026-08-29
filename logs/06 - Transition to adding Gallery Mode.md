# Transition to Adding Gallery Mode

This log records the transition from the completed Dashboard CLI to Gallery
and review functionality.

## Prompt 1 — Audit Readiness and Plan Gallery

The user asked to compare `IMPLEMENTATION_PLAN.md` with the codebase, compact
completed plans, and assess whether Gallery should be next. The audit confirmed
that Steps 1–8 are implemented and the full suite passes all 141 tests. Gallery
was recommended as the next feature because `TripStatus`, the injected Clock,
and `DoggoService.getPastTrips()` already provide its filtering foundation.
The proposed first slice was a read-only Gallery list and selected past-Trip
view, followed by review domain and CRUD work.

## Prompt 2 — Implement the Gallery Foundation

The user asked to implement the agreed plan. The implementation compacts
`IMPLEMENTATION_PLAN.md` into milestone summaries, adds Gallery and selected
Gallery Trip CLI modes, routes Main `gallery`, lists only past Trips, retains
UUID targets for `view NUMBER`, and displays selected past Trip Plans without
mutation commands. Organise now lists only current and future Trips. Parser,
session, formatter, context, stale-target, boundary, navigation, and end-to-end
tests were added or updated. Review data and commands remain the next Step 9
iterations. The SE-EDU Java review and `git diff --check` found no remaining
issues, and the final clean Gradle suite passes all 154 tests.

## Prompt 3 — Decide Gallery Maintenance and Navigation

The user asked whether Gallery should permit editing and deleting past Trips
and Plans. The design decision is to support maintenance of existing historical
records and to allow Trip creation from Gallery. Creation and successful Trip
date edits route according to derived status: past Trips belong to Gallery,
while current and future Trips belong to Organise. Both destinations show the
Trip list rather than opening the Trip automatically. From a selected Gallery
Trip, `new` creates a Plan, matching the selected Organise Trip workflow;
Trip and Plan review work remains deferred.

## Prompt 4 — Specify Gallery Maintenance Iterations

The user asked to record the agreed behavior in `IMPLEMENTATION_PLAN.md` before
implementation and then define iterative delivery steps. Step 9 now places a
Gallery maintenance phase before reviews and decomposes it into Clock-backed
status routing, status-aware Trip creation, Gallery Trip editing and deletion,
selected Gallery Plan creation, editing and deletion, and final acceptance and
documentation closure. The plan explicitly defines list-first navigation,
cross-status edit routing, active-list and composite-target validation, command
reuse, failure refresh behavior, and acceptance criteria. No Java code was
changed in this planning task.

## Prompt 5 — Commit the Current Gallery Changes

The user asked to commit the current changes in relevant logical groups using
the SE-EDU Git standard. The implementation and tests were committed as
`d7835ff Add read-only Gallery mode`; the Developer Guide, implementation plan,
project memory, and Gallery transition log were committed as
`ad0a172 Document Gallery maintenance plan`. The worktree is clean.

## Prompt 6 — Implement Gallery Maintenance Autonomously

The user asked to implement the remaining Gallery maintenance subtasks
iteratively and continue through completion when each increment is verified.
Gallery maintenance was completed across seven implementation commits:
`8f0496b Add status-aware Trip list routing`, `804e757 Route new Trips by
status`, `a3a5eb4 Enable Gallery Trip editing`, `c7fa615 Enable Gallery Trip
deletion`, `d4cd511 Create Plans from Gallery Trips`, `f08ddce Enable Gallery
Plan editing`, and `84ecf95 Enable Gallery Plan deletion`. Two real-REPL
acceptance tests cover complete historical maintenance and cross-status
routing. The implementation plan, Developer Guide, memory, and this log record
Gallery maintenance as complete while reviews, persistence, and JavaFX remain
future work. The full suite passes all 204 tests.

## Prompt 7 — Design the Review System

The user asked for a plan to add Trip and Plan reviews through contextual
`review NUMBER` commands. The finalized model makes rating and text
independently optional while requiring at least one: ratings are whole numbers
from 1 to 5, text is normalized and single-line, and no timestamps are stored.
Gallery list numbers review Trips; Dashboard and either selected-Trip view
numbers review Plans. Trips become eligible when past, while Plans become
eligible at their scheduled Clock-derived date-time. Blank edit fields preserve
their values, exact `-` clears one field, and clearing both removes the review.
Reviewed entities cannot be rescheduled into an incomplete state, and present
review fields render as indented lines.

## Prompt 8 — Implement Reviews Iteratively

The user asked to implement each Review iteration with a `gpt-5.6-luna` xhigh
subagent, parent-agent review and testing, and the same subagent performing the
SE-EDU-standard commit after approval. Code and regular documentation may be
committed in logical increments, but `IMPLEMENTATION_PLAN.md` and files under
`logs/` must remain uncommitted.

## Prompt 9 — Check the Interrupted Process

The user interrupted the parent review and asked for process status. Review
iteration 3 had already completed: the subagent reported 51 application tests
and all 236 tests passing. Its DoggoService and application-test changes remain
unstaged and uncommitted for parent review. No Codex tool session remains
active; operating-system process inspection was unavailable in the sandbox.

## Prompt 10 — Keep Commit Descriptions Brief

The user asked for shorter progress updates and commit descriptions. Future
commits will retain the required rationale in a compact body without detailed
test or implementation inventories.

## Prompt 11 — Complete the Iterative Review Loop

The user asked to resume with parent review of iteration 3, then continue each
Review iteration through subagent implementation, parent review and tests, and
a same-agent SE-EDU commit. The Review milestone was completed in six commits:
`aacd2e3` defines flexible Review values, `6a8eb3e` preserves reviews through
aggregate copies, `568e48f` adds Clock-backed operations and edit invariants,
`ae25102` adds Gallery Trip reviews, `5af90b1` adds contextual Plan reviews,
and `e103409` completes real-CLI acceptance coverage and documentation. Parent
review caught and corrected stale Dashboard membership handling and ambiguous
memory wording. The clean final suite passes all 265 tests; the implementation
plan and logs remain uncommitted as requested.

## Prompt 12 — Assess Database Integration as the Next Milestone

The user asked whether database integration should be tackled next. The
codebase review found that the Trip aggregate schema, repository port,
copy-on-write updates, and `RepositoryException` contract are stable, making
SQLite persistence the appropriate next milestone before JavaFX. The
application still needs safe aggregate rehydration, transactional SQLite
storage, restart-persistence tests, and a centralized CLI repository-failure
boundary. The failure boundary should be implemented before the production
entry point switches from the in-memory repository to SQLite, correcting the
current roadmap ordering.

## Prompt 13 — Standardize JUnit Test Curation

Before database integration, the user asked to create a project-local
`/seedu-junit-test` skill based on SE-EDU's JUnit naming and location
conventions and the CS2103/T textbook's testing types. The new skill requires
test packages to mirror production packages, unit-test classes to match their
subjects, and test methods to describe operation, scenario, and expected
behavior. Its reference guides the selection and curation of unit,
integration, system, acceptance, and regression coverage, including isolated
and deterministic tests, boundary and invalid cases, SQLite test databases,
and observable assertions. The skill structure was checked manually because
the bundled validator's optional PyYAML dependency is unavailable.

## Prompt 14 — Plan Package-by-Package Test Standardization

The user asked for a plan to update test cases package by package before
database integration. The review mapped every production class to its current
direct or indirect coverage and confirmed all 265 tests pass. The proposed
iterations start with domain value and aggregate tests, then application and
repository contracts, followed by CLI support classes, mode-specific parsers,
commands, and a reduced representative `CliTest` acceptance suite. Thin
interfaces and enums will not receive artificial accessor or declaration-order
tests; their contracts will be exercised through implementations. Broad CLI
suites will be redistributed to matching command classes without reducing
stale-target, review, Gallery, Dashboard, or end-to-end behavior coverage.

## Prompt 15 — Implement Domain Test Standardization

The user authorized the first package-by-package test iteration. The domain
suite was expanded in `PlanTest` and `TripTest` with constructor validation,
null handling, identity and review preservation, copy-on-write behavior,
unmodifiable Plan collections, and missing or invalid Plan targets. The
declaration-order-only `TripStatusTest` was removed because Trip status
behavior is covered by `TripTest` and application tests. `ReviewTest` already
covered its public contract and was left unchanged. The focused domain tests
and full Gradle suite pass, with no production-code changes.

## Prompt 16 — Implement Application Test Standardization

The next package iteration added application-level coverage. `DoggoServiceTest`
now verifies null dependency construction, failed Trip creation saves, stored
and missing Trip lookups, repository read failures, null review inputs, Plan
creation with a missing Trip, and null Plan-list queries. A new
`RepositoryExceptionTest` verifies message and cause preservation. Existing
service failure and behavior tests were retained, and the repository stub now
supports independent read-failure simulation. The application package tests
and full Gradle suite pass; no production code was changed. A test name was
refined during review so it describes only the behavior it asserts.

## Prompt 17 — Implement Storage Test Standardization

The storage package iteration added `InMemoryTripRepositoryTest` under the
matching `doggo.storage` package. It covers empty state, save and lookup,
missing IDs, UUID replacement, retrieval of all records, aggregate Plan and
Review preservation, and deletion behavior. During parent review, object
identity assertions were replaced with persisted identity and field
assertions so the cases remain suitable as a repository contract reference for
future rehydrating implementations. The storage package tests and full Gradle
suite pass; no production code was changed.

## Prompt 18 — Implement CLI Support Test Standardization

The next iteration expanded the `doggo.ui.cli` support suite. Existing
`CliSessionTest`, `CliFormatterTest`, and `InputParserTest` now cover invalid
indexes, copied Trip mappings, formatting helpers, whitespace, and null input.
New matching tests cover `PlanTarget`, `CliPrompter` input and I/O failure,
and `ReviewInputHelper` review creation, preservation, clearing, invalid
ratings, and end-of-input behavior. All CLI support tests and the full Gradle
suite pass; no production code was changed.

## Prompt 19 — Implement Mode Parser Test Standardization

The parser iteration split broad parser coverage into matching tests for
`MainCommandParser`, `OrganiseCommandParser`, `TripCommandParser`,
`DashboardCommandParser`, `GalleryCommandParser`, and
`GalleryTripCommandParser`. `ParserTest` now focuses on global commands,
normalization, and null-mode handling, while `IndexedCommandParserTest` uses
the standard behavior-oriented method names. The former broad
`ModeCommandParserTest` was removed. Parser-focused tests and the full Gradle
suite pass; no production code was changed.

## Prompt 20 — Implement Navigation and Feedback Command Tests

The next test-standardization iteration added matching unit tests for the
simple CLI commands: Dashboard, Organise, Gallery, Back, Exit, Invalid,
InvalidIndex, and Unknown. The tests verify observable mode transitions,
current-view refreshes, index validation messages, and exit results using
deterministic in-memory fixtures. A narrow shared test helper avoids repeated
service setup. The focused command tests and full Gradle suite pass; no
production-code changes were required. CRUD and review command tests remain
for subsequent iterations.

## Prompt 21 — Implement CRUD Command Tests

The next test-standardization iteration added matching unit tests for Trip and
Plan creation, editing, and deletion commands. The tests verify persisted
aggregate changes, retained identity, confirmation-driven deletion, refreshed
views, status-aware Trip routing, and staying in the selected Trip for Plan
operations. Shared deterministic CLI fixtures now provide in-memory services
and scripted prompts. The focused CRUD command tests and full Gradle suite
pass; no production-code changes were required. Review command tests remain
for a subsequent iteration.

## Prompt 22 — Commit CRUD Command Tests

The user asked to commit the completed CRUD command test iteration. The 15
focused CLI test files were committed together as `2fcd273` with the subject
`Add CLI CRUD command test coverage`. `IMPLEMENTATION_PLAN.md` and this log
remain uncommitted, preserving the established documentation workflow.

## Prompt 23 — Implement Review Command Tests

The next test-standardization iteration added matching unit tests for
`ReviewTripCommand` and `ReviewPlanCommand`. The tests verify persisted Trip
and Dashboard Plan reviews, rendered feedback, preservation of an existing
rating when text changes, and rejection of Plans scheduled later than the
Clock time. Existing broader review suites remain in place for removal,
stale-target, invalid-input, and cross-mode scenarios. Focused review tests
and the full Gradle suite pass; no production-code changes were required.

## Prompt 24 — Curate CLI Acceptance Coverage

The next test-standardization iteration reduced the broad `CliTest` suite to
12 representative end-to-end scenarios. Retained workflows cover Gallery
navigation and status partitioning, Trip and Plan lifecycle operations,
Dashboard rendering and synchronization, review persistence, and focused
user-facing error handling. Detailed command, parser, support, Gallery, and
review behavior remains covered by their matching or cohesive suites. The
curated acceptance suite and full Gradle test suite pass; no production-code
changes were required.

## Prompt 25 — Commit Acceptance and Review Tests

The user asked to commit the curated CLI acceptance suite and focused review
command tests. The three test files were committed together as `f12d06c` with
the subject `Curate CLI acceptance and review tests`. `IMPLEMENTATION_PLAN.md`
and this log remain uncommitted, preserving the established documentation
workflow.

## Prompt 26 — Check for Remaining Test Iterations

The user asked whether more iterations remain. The package-by-package JUnit
standardization is complete: domain, application, storage, CLI support,
parsers, commands, and curated acceptance coverage now have matching or
cohesive tests. The implementation plan's next product milestone is SQLite
persistence, followed by a centralized repository-failure boundary and then
the JavaFX presentation. No additional test-standardization iteration is
required before starting persistence work.

## Prompt 27 — Plan SQLite Persistence

The user asked how SQLite persistence should be structured. The agreed design
uses `data/doggo.db`, schema versioning through `PRAGMA user_version`, a
`trips` aggregate-root table, and a foreign-keyed `plans` table with cascading
deletion. Optional Trip and Plan Review fields remain inline. Repository reads
rehydrate validated aggregates, saves replace an aggregate transactionally,
and production wiring follows a centralized `RepositoryException` boundary.
Implementation is divided into failure handling, domain restoration, schema
initialization, reads, transactional writes, and production wiring.

## Prompt 28 — Start Iterative SQLite Implementation

The user requested that every SQLite iteration be implemented by a
`gpt-5.6-luna` high subagent, reviewed and tested by the parent agent, and then
committed by a `gpt-5.6-luna` medium subagent using the SE-EDU Git standard.
Iteration 1 starts with the CLI repository-failure boundary and focused read,
save, and delete failure tests. Startup database initialization handling is
deferred until the SQLite repository exists.

The implementation catches repository failures at the CLI command boundary,
shows generic guidance without refreshing through the failing repository, and
keeps the current mode usable. Parent review requested and verified an
explicit retained-Organise-mode regression after a one-shot read failure.
Focused read, save, and confirmed-delete tests and the full Gradle suite pass.
The verified Java changes were committed as `214886e` with the subject
`Handle repository failures in CLI`; plan and log updates remain uncommitted.

## Prompt 29 — Complete SQLite Restoration Factories

With authorization to continue autonomously, Iteration 2 added validated
`Trip.restore` and `Plan.restore` factories for complete stored aggregate
state. The factories reuse canonical constructors, retain reviews and Plans,
and preserve validation, trimming, and defensive copying. Parent review added
complete public exception documentation and date assertions. Focused domain
tests and the full Gradle suite pass; documentation remains uncommitted.
The verified Java changes were committed as `a612729` with the subject
`Add validated domain restoration`.

## Prompt 30 — Reuse SQLite Iteration Subagents

The user clarified that the same `gpt-5.6-luna` high implementation subagent
and `gpt-5.6-luna` medium commit subagent should be reused across remaining
iterations. The active high subagent will continue through Iterations 3–6, and
the existing medium subagent will be resumed for each approved commit instead
of closing and replacing either agent.

## Prompt 31 — Complete SQLite Schema Initialization

Iteration 3 added the SQLite JDBC dependency and a versioned database
initializer. New databases receive strict Trip and Plan tables, cascading
foreign keys, review constraints, an index, and schema version 1; existing
version-1 databases reopen safely while newer or incompatible schemas fail
with `RepositoryException`. Java 25 native access is enabled for Gradle runs
and packaged execution. Parent review verified the focused database tests,
full test suite, shaded JAR, and manifest before handing the changes to the
reused commit subagent. The verified build and Java changes were committed as
`67e1981` with the subject `Add versioned SQLite schema`; documentation remains
uncommitted. During verification, a status check confirmed that no Gradle
process remained active, and the Java 25 native-access refinement was then
applied to Gradle tests, application defaults, and the shaded JAR manifest.

## Prompt 32 — Complete SQLite Aggregate Reads

Iteration 4 added a package-private SQLite aggregate reader so the read path
could be completed without temporarily exposing a repository with unsupported
write methods. It performs one joined query per operation, groups rows in
linear time, restores Trip, Plan, and Review state through validated domain
factories, and translates malformed persisted state while retaining its cause.
Parent review verified the SQL, resource handling, seven focused integration
cases, and the full test suite. The changes were committed as `924f765` with
the subject `Load Trip aggregates from SQLite`; documentation remains
uncommitted.

## Prompt 33 — Complete Transactional SQLite Writes

Iteration 5 added the public SQLite repository adapter. Aggregate saves upsert
the Trip, replace its Plans, and persist optional reviews in one transaction;
deletion relies on the verified foreign-key cascade. A forced trigger failure
proves rollback preserves the complete prior aggregate immediately and after a
repository restart. Parent review inspected the failure path and verified nine
focused repository tests plus the full 327-test suite before approving the
changes. They were committed as `524fb53` with the subject
`Persist Trip aggregates transactionally`; documentation remains uncommitted.

## Prompt 34 — Complete Production SQLite Wiring

Iteration 6 replaced the production in-memory composition with
`SqliteTripRepository` at `data/doggo.db` and added clean startup guidance for
an inaccessible database. A two-session CLI integration test proves a Trip
created in one application run is displayed after restart. Parent review
verified the focused tests, full 329-test suite, shaded JAR manifest, and a
packaged smoke launch that created the default database without native-access
warnings. The Developer Guide and durable project memory now describe the
production persistence behavior. The changes were committed as `7aad568` with
the subject `Use SQLite for production storage`; plan and log changes remain
uncommitted.

## Prompt 35 — Run Tests from the VS Code Testing Tab

The user asked why tests currently pass through `./gradlew test` but do not
run correctly from VS Code's Testing tab. Inspection confirmed the project has
Gradle JUnit Platform configuration and no workspace-level VS Code settings.
The response will recommend the Extension Pack for Java, a Java 25 runtime,
and enabling Gradle test execution in VS Code so the Testing tab delegates to
Gradle and preserves the project's SQLite/JUnit runtime configuration.

## Prompt 36 — Clarify VS Code Workspace Variable

The user asked whether `${workspaceFolder}` in the suggested VS Code test
configuration must be renamed. The response clarified that it is a built-in
VS Code variable resolving to the opened project root and should remain
unchanged; only the Java installation path, if manually configured, needs a
machine-specific value.

## Prompt 37 — Diagnose VS Code Test Runner Configuration

The user reported that the VS Code Testing tab still does not run the tests.
Inspection found a new `.vscode/settings.json` containing a Java 25 test
profile with the workspace directory and native-access VM argument. The
response clarified that this configures the standalone Java runner only; it
does not select the profile or delegate execution to Gradle. The next fix is
to select the Gradle test profile in Testing Explorer, set the Java profile as
default if using the standalone runner, and reset Java/Gradle project
discovery if tests remain unavailable.

## Prompt 38 — Remove Unused Gallery Test Variable

The user asked whether `selectedPlan` at line 538 of
`GalleryMaintenanceTest.java` was used. It was assigned but never referenced,
so the assignment was removed while retaining the Plan creation needed by the
fixture. The focused Gallery maintenance suite and diff checks passed.
