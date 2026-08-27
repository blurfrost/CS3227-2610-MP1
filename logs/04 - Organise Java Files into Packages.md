# Organise Java Files into Packages

## Proposed File Structure

```text
src/main/java/
└── doggo/
    ├── Doggo.java
    ├── domain/
    │   ├── Plan.java
    │   └── Trip.java
    ├── application/
    │   ├── DoggoService.java
    │   ├── RepositoryException.java
    │   └── TripRepository.java
    ├── storage/
    │   └── InMemoryTripRepository.java
    └── ui/
        └── cli/
            ├── BackCommand.java
            ├── Cli.java
            ├── CliContext.java
            ├── CliFormatter.java
            ├── CliMode.java
            ├── CliPrompter.java
            ├── CliSession.java
            ├── Command.java
            ├── CommandResult.java
            ├── DeletePlanCommand.java
            ├── DeleteTripCommand.java
            ├── EditPlanCommand.java
            ├── EditTripCommand.java
            ├── ExitCommand.java
            ├── InputParser.java
            ├── InvalidCommand.java
            ├── NewPlanCommand.java
            ├── NewTripCommand.java
            ├── OrganiseCommand.java
            ├── Parser.java
            ├── UnknownCommand.java
            └── ViewTripCommand.java
```

Tests will mirror their production packages under `src/test/java/doggo/`.

The intended dependency direction is:

```text
doggo.Doggo
 ├── doggo.ui.cli
 ├── doggo.application
 └── doggo.storage
          │
doggo.ui.cli ──> doggo.application ──> doggo.domain
                         ▲
doggo.storage ───────────┘
```

`Doggo` remains the composition root. `TripRepository` belongs to the
application package as the persistence port required by application use cases,
while storage implementations depend on that port. All current CLI commands
remain together in `doggo.ui.cli` because they depend on CLI prompts, session
state, modes, and output. A future JavaFX interface will use its own
`doggo.ui.javafx` controllers and form handling, calling `DoggoService` directly
rather than reusing CLI commands.

The migration is structural: move files, add package declarations and imports,
adjust only the visibility needed across package boundaries, update Gradle's
main class to `doggo.Doggo`, and synchronize the architecture documentation and
implementation plan. Parser decomposition, JavaFX implementation, and runtime
behavior changes remain separate tasks.

## Prompt 1 — Propose the Package Structure

The user requested a proposed Java package structure before implementation. The
recommended structure places the composition root in `doggo`, domain objects in
`doggo.domain`, application services and repository contracts in
`doggo.application`, repository implementations in `doggo.storage`, and all CLI
presentation classes in `doggo.ui.cli`. Tests will mirror the production
packages. Implementation was deferred until the structure was reviewed.

## Prompt 2 — Confirm CLI Command Placement

The user asked whether the `*Command` classes belong in `doggo.ui.cli` because a
future JavaFX interface will submit Trip and Plan details through individual
form fields. The design was confirmed: current commands are CLI-specific because
they depend on `CliContext`, prompting, modes, session state, and terminal
output. Future JavaFX controllers will collect and parse form values, invoke
`DoggoService`, and display results or validation errors without using CLI
commands.

## Prompt 3 — Start the Package Migration Log

The user requested recording the proposed package structure in a new package-
migration log, using a better filename if appropriate, and updating `AGENTS.md`
so subsequent prompt summaries are written there instead of the bare CLI log.
The new log is named `04 - Organise Java Files into Packages.md`, contains the
agreed structure and architectural rationale, and is now the active prompt log.

## Prompt 4 — Implement the Package Migration

The user authorized moving the Java production and test files into the agreed
packages, including all required imports and the application launch-path update
in `build.gradle`. The implementation moved the composition root, domain,
application, storage, and CLI sources into their package directories; mirrored
the test packages; exposed only the cross-package APIs; changed the Gradle main
class to `doggo.Doggo`; and synchronized the Developer Guide and implementation
roadmap. Runtime behavior and CLI commands were not changed. The clean test
suite and Shadow JAR build passed, the manifest names `doggo.Doggo`, and a
packaged-application smoke test exited successfully.

## Prompt 5 — Evaluate Current Progress

The user asked for the implementation plan to be evaluated against the actual
codebase. The audit confirmed that all five Organise CRUD and stabilization
steps are implemented, the package migration is structurally complete, and
the broader roadmap still has Parser decomposition, TripStatus, Dashboard,
reviews/Gallery, SQLite persistence, and JavaFX outstanding. Java 25.0.3 was
used to run the clean test and Shadow JAR build: all 82 tests passed, the JAR
manifest points to `doggo.Doggo`, and a packaged CLI smoke test exited
successfully. No production changes were needed from the evaluation.

## Prompt 6 — Commit the Changes

The user requested committing the current package migration, documentation,
configuration, source, test, and prompt-log changes. The commit will follow
the SE-EDU Git standard and will be created only after staging and reviewing
the complete migration diff.
