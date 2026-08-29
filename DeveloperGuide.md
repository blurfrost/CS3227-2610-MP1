# doggo Developer Guide

## Introduction

doggo is a local-first travel planning and journalling application. It allows users to track current trips, organise future trips, and revisit past trips through ratings, reviews, and, in a future extension, photos.

Development began with a tested command-line interface (CLI). The desktop
entry point now uses JavaFX while retaining the same domain, application, and
persistence logic; the CLI remains available through `./gradlew runCli`.

## Product Terminology

- **Trip:** An overall journey, such as a trip to Japan. A Trip contains zero or more Plans.
- **Plan:** One scheduled itinerary item belonging to a Trip, such as visiting a restaurant or landmark.
- **Review:** An immutable value with an optional whole-number rating from 1 to 5
  and optional written text associated with a completed Trip or Plan; at least
  one field must be present.
- **TripStatus:** A value derived from a Trip's inclusive start and end dates: future, current, or past.
- **Dashboard:** Displays a flat chronological list of Plans scheduled for the current day, with each Plan's owning Trip title.
- **Organise:** Displays Trips and allows users to select a Trip to view and manage its itinerary.
- **Gallery:** Displays every Trip whose end date has passed, regardless of whether reviews are present.

## Functional User Stories

### CLI MVP

1. As a new user, I can view available commands and usage examples so that I can discover doggo's functions.
2. As a user, I can create a Trip with its title, start date, and end date so that I can organise an overall journey.
3. As a user, I can edit an existing Trip so that I can correct or update its details.
4. As a user, I can delete a Trip after confirming the action so that I can remove journeys I no longer need.
5. As a user, I can add a Plan with a destination, date, and time to a Trip so that I can build its itinerary.
6. As a user, I can edit an existing Plan so that I can adjust my itinerary.
7. As a user, I can delete an existing Plan after confirming the action so that I can remove unwanted itinerary items.
8. As a user, I can select a Trip and view its Plans in chronological order so that I can understand its itinerary.
9. As a daily user, I can view all Plans scheduled for today in chronological order with their owning Trip titles so that I can follow my daily itinerary.
10. As a user, I can view Trips grouped as future, current, or past so that I can find the relevant journey quickly.
11. As a frequent user, I can give a completed Trip or Plan an optional whole-number rating from 1 to 5 and optional written review text so that I can record my experience.
12. As a frequent user, I can edit or remove a review so that my recorded experience remains accurate.
13. As a returning user, I can find my Trips, Plans, and reviews after restarting doggo so that my travel data is retained.

### JavaFX Desktop

14. As a new user, I can access Dashboard, Organise, and Gallery through persistent primary navigation so that I can discover and reach every major function.
15. As a user, I can use Dashboard to inspect the details of an individual Plan scheduled for today.
16. As a user, I can use Organise to select a Trip and view or manage its itinerary.
17. As a user, I can use Gallery to view every Trip whose end date has passed, regardless of whether it has been reviewed.
18. As a user, I can view available Trip and Plan ratings and reviews within a past Trip's Gallery entry.

### Future Extensions

19. As a user, I can attach, view, and remove photos from a Plan review so that I can preserve visual memories.
20. As a frequent user, I can copy a Plan from a past Trip into a future Trip so that a positively reviewed place can be reused.
21. As a user, I can search or filter Trips and Plans so that a growing travel history remains manageable.

## Non-Functional User Stories

1. As a user, I receive clear validation and recovery guidance for invalid input so that mistakes do not terminate the application.
2. As a user, previously saved data remains intact if a later save fails so that I do not lose valid travel records.
3. As a user, common commands and views complete within two seconds with up to 1,000 Trips and 10,000 Plans on the supported development environment.
4. As a privacy-conscious user, I can use doggo locally without creating an account or sending travel data externally.
5. As a keyboard user, I can operate all core JavaFX functions without requiring a mouse.
6. As a user, dates and times are displayed consistently and Plans with the same time have deterministic ordering.
7. As a developer, I can replace the CLI with JavaFX without changing domain rules or persistence behaviour.
8. As a developer, I can test domain and application behaviour without launching JavaFX or accessing the production database.

## Domain Rules

- A Trip contains zero or more Plans.
- A Plan belongs to exactly one Trip.
- Trip status is derived from inclusive start and end dates relative to a
  supplied current date:
  - A future Trip starts after the current date.
  - A past Trip ends before the current date.
  - Every other valid range is current, including either boundary date and a
    single-day Trip on the current date.
- Gallery includes every past Trip. Reviews are optional and are displayed only when present.
- A Review may contain a whole-number rating from 1 to 5 and may contain written text, but at least one field must be present. Review text is trimmed and blank text is treated as absent.
- Trip reviews are available after the Trip's end date has passed according to
  the service Clock. Plan reviews are available at or after the Plan's
  scheduled local date and time.
- Repeating `review NUMBER` edits an existing review. During an edit, blank
  input preserves the existing field and an exact `-` clears it. Clearing both
  fields removes the review. On a new review, blank fields are absent.
- A reviewed Trip must remain past when its dates are edited. A reviewed Plan
  cannot be moved later than the current Clock-derived date and time.
- Deleting a Trip requires explicit confirmation and removes its Plans and associated reviews.
- Deleting a Plan requires explicit confirmation and removes its associated review.
- Dashboard includes Plans whose scheduled local date is the current date.
- Plans are displayed chronologically. Plans with equal scheduled times use deterministic ordering.
- Invalid commands, dates, ratings, and references to missing records produce actionable errors without terminating the application.

## Architectural Constraints

- CLI and JavaFX code are presentation layers and do not contain domain or persistence rules.
- Application services coordinate use cases independently of the presentation layer.
- Domain classes do not depend on JavaFX, CLI input, JDBC, or filesystem paths.
- Persistence is accessed through repository interfaces so production and test implementations can be substituted.
- Photo storage is accessed through a storage interface so local media storage can later be replaced if needed.

## CLI MVP Architecture

Java source files use packages that follow the architectural boundaries:

- `doggo.domain` contains Trip and Plan domain objects.
- `doggo.application` contains presentation-independent services and repository contracts.
- `doggo.storage` contains repository implementations.
- `doggo.ui.cli` contains the CLI and its commands, parsing, formatting, and session state.
- `doggo.Doggo` is the CLI entry point and CLI composition root.
- `doggo.ui.javafx.DoggoApplication` and `doggo.ui.javafx.DoggoLauncher` are
  the JavaFX composition root and classpath-safe desktop entry point.

JavaFX presentation code uses the separate `doggo.ui.javafx` package and calls
the application services without depending on CLI commands.

The CLI is divided into Main navigation and Dashboard, Organise, selected-Trip,
Gallery, and selected-Gallery-Trip modes:

```text
Doggo
  -> Cli -> Parser -> ModeCommandParser -> Command
                    Main / Dashboard / Organise / Trip / Gallery parsers
                                             |
                                       DoggoService
                                             |
                                       TripRepository
                                        /            \
                             InMemoryTripRepository    SqliteTripRepository

Domain: Trip -> Plan
          |       |
        Review  Review
```

### Domain

- `Trip` is the aggregate root and contains a UUID, title, inclusive start and
  end dates, Plans, and an optional Review.
- `Plan` contains a UUID, destination, scheduled date and time, and an optional
  Review.
- `Review` is an immutable value containing an optional whole-number rating from
  1 to 5 and optional text; at least one field must be present.
- `TripStatus` contains `FUTURE`, `CURRENT`, and `PAST`. Status is derived from Trip dates and the supplied current date rather than persisted.

### Application and Persistence

- `DoggoService` provides presentation-independent Trip and Plan CRUD, Dashboard
  and Gallery queries, Clock-backed completion checks, and review operations for
  setting, replacing, and removing Trip and Plan reviews.
- `TripRepository` defines `findAll`, `findById`, `save`, and `delete` operations for Trip aggregates.
- `InMemoryTripRepository` supports early development and isolated application tests.
- `SqliteTripRepository` provides durable storage in `data/doggo.db`, which is created on startup.
- SQLite schema version 1 is recorded with `PRAGMA user_version`; unsupported newer versions are
  rejected without modifying the database.
- `DoggoService` receives a `java.time.Clock` so date-sensitive behaviour can be tested deterministically.
- Saving a Trip replaces its root and Plans in one transaction, including optional reviews; a
  failed save is rolled back. Deleting a Trip uses the database foreign-key cascade to remove
  its Plans and reviews.

### JavaFX Presentation

- `doggo.ui.javafx.DoggoLauncher` starts `DoggoApplication` from the classpath.
- `DoggoApplication` initializes the production repository, Clock, and
  `DoggoService`, then loads the FXML shell.
- `AppShellController` owns persistent Dashboard, Organise, and Gallery
  navigation; the latter two currently display styled placeholders.
- `AppShell.fxml` and `doggo.css` define the warm travel-journal shell while
  keeping views independent from CLI commands.

### Build and Run

- `./gradlew run` starts the JavaFX application using `data/doggo.db`.
- `./gradlew runCli` starts the CLI using `data/doggo.db`.
- `./gradlew test` runs the JUnit suite.
- `./gradlew shadowJar` creates the executable JAR in `build/libs`.
- JavaFX `26.0.1` dependencies are included for the Windows, macOS, and Linux
  classifiers.
- Java 25 native access is enabled for Gradle-launched tests and runs and is recorded in the
  executable JAR manifest for the SQLite JDBC driver.

### CLI

- `Doggo` is the composition root and application entry point.
- `Cli` owns the read-evaluate-print loop and injected input and output streams.
- `CliMode` identifies the implemented `MAIN`, `ORGANISE`, `TRIP`, `DASHBOARD`,
  `GALLERY`, and `GALLERY_TRIP` modes.
- `CliSession` tracks the current mode, selected Trip, and mappings from displayed list numbers to UUIDs.
- `Parser` normalizes input, handles global `exit` and `back` commands, and
  delegates mode-specific parsing through `ModeCommandParser` implementations.
- All six Main, Organise, selected-Trip, Dashboard, Gallery, and
  selected-Gallery-Trip parsers own the command grammar for their respective
  modes.
- `IndexedCommandParser` shares indexed-command construction, while
  `InvalidIndexCommand` reports errors using the active displayed snapshot.
- `Command` executes an action through `DoggoService` and `CliSession`.
- `CommandResult` contains output, navigation changes, and whether the application should exit.
- `CliFormatter` formats domain information, help, and errors.
- `CliPrompter` gathers fields interactively for creation and editing commands.

Use separate command classes for navigation and user actions:

- Navigation and global commands open Dashboard, Organise, or Gallery, return
  to the previous menu, display help, or exit the application.
- Dashboard commands list today's Plans, create a Trip, edit or delete a Plan by
  number, and review a completed Plan by number. Dashboard Plan creation and
  detailed Plan viewing remain deferred.
- Organise commands support creating a Trip, viewing a Trip and its Plans, editing or deleting a Trip by index, and managing Plans within a viewed Trip.
- Gallery lists past Trips and supports Trip `new`, `edit`, `delete`, and
  `review NUMBER`. Selected Gallery Trips support Plan `new`, `edit`, `delete`,
  and `review NUMBER`.

### CLI Behaviour

- Main mode accepts `new`, `organise`, `dashboard`, `gallery`, and `exit`.
- Dashboard mode accepts `new`, `edit NUMBER`, `delete NUMBER`,
  `review NUMBER`, and `back`; global `exit` remains available. Plan edits,
  deletions, and reviews keep Dashboard active.
- Trip creation can originate in Main, Dashboard, Organise, or Gallery and
  enters the resulting status list with list-first behavior. Trip editing
  originates in Organise or Gallery and also routes by the resulting status.
  Plan mutations remain in the selected Trip mode.
- Organise lists current and future Trips and accepts `new`, `edit NUMBER`,
  `view NUMBER`, `delete NUMBER`, and `back`. When a Trip is viewed, it accepts
  `new`, `edit NUMBER`, `delete NUMBER`, `review NUMBER`, and `back` for its
  Plans.
- Gallery lists past Trips and accepts `new`, `view NUMBER`, `edit NUMBER`,
  `delete NUMBER`, `review NUMBER`, and `back`. A selected Gallery Trip accepts
  Plan `new`, `edit NUMBER`, `delete NUMBER`, `review NUMBER`, and `back`;
  global `exit` remains available in both Gallery modes.
- The CLI displays short one-based list numbers while retaining stable UUIDs internally.
- Creation and editing commands prompt for individual fields instead of requiring long command lines.
- Trip and Plan dates use the strict `DD/MM/YYYY` format.
- Plan times use the strict 24-hour `HH:mm` format.
- Invalid commands remain in the current mode and display actionable help.
- Deletion commands require explicit confirmation.

### Implemented CLI Feature Sets

- Feature Set 1 supports creating and listing Trips through the available Trip
  creation modes, with status-aware list-first routing.
- Feature Set 2 supports viewing a Trip with `view NUMBER`, using its one-based displayed index.
- Feature Set 3 supports editing and deleting Trips and Plans through the
  mode-specific Organise and Gallery commands.
- Selected Trips display their Plans in chronological date-and-time order.
- Plans require a destination, a strict `DD/MM/YYYY` date, and a strict `HH:mm` time.
- Plan dates must fall within the selected Trip's inclusive date range.
- During Plan creation, `back` is accepted as a destination; for date and time prompts it is invalid input and causes a reprompt.
- Displayed Trip and Plan indices use retained UUID mappings, and stale targets are reported without prompting for destructive or edit actions.
- Mode-specific parsing is delegated through parsers for Main, Organise,
  selected Trip, Dashboard, Gallery, and selected Gallery Trip, with shared
  indexed-command validation and snapshot-aware feedback.
- Dashboard lists today's Plans in deterministic order and supports
  repository-backed Plan editing and deletion through composite UUID targets.
- Gallery lists every past Trip and provides retained UUID-targeted Trip and
  Plan maintenance. Trip mutations route by resulting status and Plan
  mutations stay in the selected Trip view.
- Reviews support immutable rating/text values, Clock-backed eligibility,
  contextual Trip and Plan commands, replacement and removal, retained target
  validation, and rendering in every relevant Trip or Plan view. Review input
  preserves fields on blank input and clears fields on exact `-` input.
- Feature Sets 1–3, Dashboard, and Gallery maintenance use the SQLite repository in production;
  the in-memory repository remains available for isolated tests. The JavaFX
  shell foundation is implemented; functional GUI views remain in progress.

## Acceptance and Test Coverage

- Verify Trip and Plan creation, editing, deletion, retrieval, and persistence across application restarts.
- Verify Trip status at start-date and end-date boundaries.
- Verify Dashboard includes only today's Plans and orders them deterministically.
- Verify Gallery excludes current and future Trips, includes past Trips without
  reviews, and supports safe Trip and Plan maintenance.
- Verify Trip and Plan review eligibility, rating/text validation, contextual
  CLI review flows, replacement/removal semantics, rendering, and reviewed-date
  edit restrictions.
- Verify failed writes do not damage previously persisted data.
- Verify domain and application tests run without JavaFX or the production database.
- Verify the CLI exposes help for all supported commands and handles invalid input without crashing.
- Verify mode-specific parsing, navigation, and list-number-to-UUID mappings.
- Verify date-sensitive behaviour with an injected fixed Clock.
