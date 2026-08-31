# doggo Developer Guide

This guide describes the current implementation of **doggo**, a local-first
Java desktop application for planning trips, managing itineraries, and
recording reviews. It is written for contributors and reviewers who need to
build, understand, or manually verify the application.

The JavaFX desktop application is the default entry point. The original CLI is
retained and uses the same domain, application-service, and persistence code.

## 1. Setup guide

### 1.1 Required software

Install the following before building from source:

| Tool | Required version | Purpose |
| --- | --- | --- |
| JDK | **Java 25.0.3.fx-zulu** | Compiles and runs the project. The repository toolchain is Java 25. |
| Git | Any recent version | Obtains the source repository. |
| Gradle | Not installed separately | Use the included Gradle wrapper (`./gradlew` or `gradlew.bat`). |
| VS Code | Optional | Recommended IDE for this project. |

Use the Zulu JDK 25.0.3 distribution with JavaFX support specified by the
project environment. A JDK, rather than a JRE, is required for compilation and
for the Gradle test tasks. On macOS, install the package matching the machine's
architecture (Apple Silicon/AArch64 or Intel/x64).

Verify the active runtime:

~~~text
java --version
~~~

The output should report Java 25. If multiple JDKs are installed, configure
the terminal or IDE to use the Java 25.0.3.fx-zulu installation before running
Gradle.

### 1.2 Project dependencies

Gradle downloads the source dependencies from Maven Central. No manual
JavaFX or SQLite installation is needed when building this project.

| Dependency | Version/classifiers | Scope |
| --- | --- | --- |
| JavaFX Base, Controls, FXML, Graphics | 25.0.3 with `win`, `mac`, `mac-aarch64`, and `linux` classifiers | Production |
| SQLite JDBC | 3.53.4.0 | Production persistence |
| JUnit Jupiter | 6.1.2 BOM | Tests |
| JUnit Platform Launcher | Resolved by Gradle | Test runtime |

The JavaFX classifiers allow the Shadow JAR to contain native libraries for
Windows, Intel macOS, Apple Silicon macOS, and Linux. A source build therefore
needs network access the first time Gradle resolves these artifacts.

### 1.3 Build and run

Run these commands from the repository root:

~~~bash
# Start the JavaFX desktop application.
./gradlew run

# Start the retained command-line application.
./gradlew runCli

# Run all automated tests.
./gradlew test

# Build the executable cross-platform JAR.
./gradlew clean shadowJar
~~~

On Windows, use the corresponding wrapper:

~~~powershell
gradlew.bat run
gradlew.bat runCli
gradlew.bat test
gradlew.bat clean shadowJar
~~~

The packaged application is written to `build/libs/doggo.jar`. Gradle-launched
JavaFX execution supplies the required Java 25 native-access option. When
launching the JAR directly, use the option before `-jar` if the runtime reports
a JavaFX native-access warning:

~~~bash
java --enable-native-access=javafx.graphics -jar build/libs/doggo.jar
~~~

### 1.4 Local data

The production entry points use the relative path `data/doggo.db`. The path is
resolved from the process's current working directory, not from the location
of the JAR. The `data` directory and database are created automatically on
first startup.

The SQLite schema is versioned with `PRAGMA user_version`. The current schema
version is 1 and contains `trips` and `plans` tables. Plans reference their
owning Trip with a foreign key and are removed by SQLite's cascade when the
Trip is deleted.

For manual testing, close doggo before copying or backing up `data/doggo.db`.
The application is local-only: it does not require an account or send travel
data to a remote service.

## 2. Design and architecture

### 2.1 Architectural style

doggo uses a **layered architecture with dependency inversion at the
persistence boundary**:

1. The **presentation layer** contains JavaFX views/controllers/dialogs and
   the CLI commands/parsers.
2. The **application layer** contains `DoggoService`, use-case queries, and
   the `TripRepository` interface.
3. The **domain layer** contains the Trip aggregate and its business rules.
4. The **persistence layer** contains the SQLite repository and database
   schema code.

The JavaFX presentation follows an MVC-like arrangement: FXML and CSS define
the view, controllers react to events, and dialogs collect form input. The CLI
is a command-interpreter presentation layer. Both presentations depend on the
same `DoggoService`, so replacing or extending a UI does not duplicate domain
or database rules.

The application is also **local-first**. SQLite is the production repository;
`InMemoryTripRepository` is a substitutable test adapter. Domain classes do
not depend on JavaFX, CLI input, JDBC, or filesystem paths.

### 2.2 Package responsibilities

| Package | Main responsibility | Representative classes |
| --- | --- | --- |
| `doggo.domain` | Immutable domain state and invariants | `Trip`, `Plan`, `Review`, `TripStatus` |
| `doggo.application` | Presentation-independent use cases and ports | `DoggoService`, `TripRepository`, `DashboardEntry` |
| `doggo.storage` | SQLite schema, reading, transactions, and repository adapter | `SqliteDatabase`, `SqliteTripReader`, `SqliteTripRepository` |
| `doggo.ui.javafx` | Desktop composition, FXML controllers, cells, forms, and dialogs | `DoggoApplication`, `AppShellController`, `DashboardController` |
| `doggo.ui.cli` | REPL, navigation modes, parsing, formatting, and command objects | `Cli`, `Parser`, `Command`, `CliSession` |
| `doggo` | CLI composition root | `Doggo` |

### 2.3 Component diagram

~~~mermaid
flowchart LR
    subgraph Presentation[Presentation layer]
        JavaFX[JavaFX shell\nFXML + CSS + controllers + dialogs]
        CLI[CLI\nREPL + parsers + commands]
    end

    subgraph Application[Application layer]
        Service[DoggoService\nuse-case coordinator]
        Port[TripRepository\nrepository interface]
    end

    subgraph Domain[Domain layer]
        Aggregate[Trip aggregate]
        Plan[Plan]
        Review[Review]
        Status[TripStatus]
    end

    subgraph Persistence[Persistence layer]
        SQLite[SqliteTripRepository]
        Reader[SqliteTripReader]
        Database[SqliteDatabase]
        File[(data/doggo.db)]
        Memory[InMemoryTripRepository\ntest adapter]
    end

    JavaFX --> Service
    CLI --> Service
    Service --> Port
    Service --> Domain
    Aggregate -->|owns| Plan
    Aggregate -.->|has optional| Review
    Plan -.->|has optional| Review
    Aggregate -->|derives| Status
    SQLite -.->|implements| Port
    Memory -.->|implements| Port
    SQLite --> Reader
    SQLite --> Database
    Reader --> Database
    Database --> File
~~~

The arrows represent allowed dependencies. In particular, `Trip` and `Plan`
know nothing about `SqliteDatabase` or JavaFX, while the service knows only the
repository abstraction rather than a concrete database implementation.

### 2.4 Main class diagram

The following UML view shows the central runtime relationships. It omits
formatting helpers and individual CLI command subclasses to keep the domain
and use-case boundaries visible.

~~~mermaid
classDiagram
    direction LR

    class Trip {
        +UUID id()
        +String title()
        +LocalDate startDate()
        +LocalDate endDate()
        +List~Plan~ plans()
        +Optional~Review~ review()
        +TripStatus statusOn(LocalDate date)
        +Trip withAddedPlan(Plan plan)
        +Trip withUpdatedDetails(...)
        +Trip withReplacedPlan(Plan plan)
    }

    class Plan {
        +UUID id()
        +String destination()
        +LocalDate date()
        +LocalTime time()
        +Optional~Review~ review()
        +Plan withUpdatedDetails(...)
    }

    class Review {
        +OptionalInt rating()
        +Optional~String~ text()
    }

    class TripStatus {
        <<enumeration>>
        FUTURE
        CURRENT
        PAST
    }

    class DoggoService {
        +createTrip(...): Trip
        +addPlan(...): Plan
        +editTrip(...): Trip
        +editPlan(...): Plan
        +deleteTrip(UUID): void
        +deletePlan(UUID, UUID): void
        +getDashboardEntries(): List
        +getCurrentAndFutureTrips(): List
        +getPastTrips(): List
        +setTripReview(...): Trip
        +setPlanReview(...): Plan
    }

    class TripRepository {
        <<interface>>
        +findAll(): List~Trip~
        +findById(UUID): Optional~Trip~
        +save(Trip): void
        +delete(UUID): void
    }

    class SqliteTripRepository {
        -SqliteDatabase database
        -SqliteTripReader reader
    }

    class InMemoryTripRepository
    class DashboardEntry {
        +UUID tripId()
        +String tripTitle()
        +Plan plan()
    }

    class DashboardController
    class OrganiseController
    class GalleryController
    class AppShellController
    class TripCreationDialog
    class PlanCreationDialog
    class ReviewDialog

    Trip *-- "0..*" Plan : owns
    Trip o-- "0..1" Review : has
    Plan o-- "0..1" Review : has
    Trip --> TripStatus : derives
    DoggoService --> TripRepository : uses
    DoggoService --> Trip : creates/updates
    DoggoService --> DashboardEntry : produces
    SqliteTripRepository ..|> TripRepository
    InMemoryTripRepository ..|> TripRepository
    DashboardEntry --> Plan
    DashboardController --> DoggoService
    OrganiseController --> DoggoService
    GalleryController --> DoggoService
    AppShellController --> DashboardController : navigates
    AppShellController --> OrganiseController : navigates
    AppShellController --> GalleryController : navigates
    TripCreationDialog --> DoggoService
    PlanCreationDialog --> DoggoService
    ReviewDialog --> DoggoService
~~~

### 2.5 Domain and persistence decisions

- `Trip` is the aggregate root. It owns zero or more `Plan` values and an
  optional Trip `Review`.
- A `Plan` belongs to exactly one Trip and has a destination, date, time, and
  optional Review.
- Domain objects are immutable. Mutations return validated copies such as
  `withAddedPlan`, `withUpdatedDetails`, and `withReview`.
- A Trip's status is derived from an injected `Clock` and its inclusive date
  range. Status is not persisted.
- Trip titles and Plan destinations cannot be blank and are limited to 50
  Unicode code points for new or changed values. Restored legacy over-limit
  values can remain usable when their name is not changed.
- A Plan date must fall within the owning Trip's inclusive start/end dates.
- A Review may contain a rating from 1 to 5, Notes text, or both. A completely
  empty Review is absent. Review mutations do not depend on whether a Trip or
  Plan is current, future, or past.
- Successful Trip saves replace the aggregate root and its Plans in one SQLite
  transaction. A failed save is rolled back.
- Deleting a Trip deletes its Plans and their Reviews through the SQLite
  foreign-key cascade. Deleting a Plan removes its Review with the Plan.

### 2.6 JavaFX composition and navigation

`DoggoLauncher` is a classpath-safe `main` method that starts
`DoggoApplication`. The application creates the SQLite repository, system
`Clock`, and `DoggoService`, then loads `AppShell.fxml` with a controller
factory. The shell keeps the sidebar and all three primary pages alive while
switching their visibility and active navigation style.

- **Dashboard** queries today's Plans as `DashboardEntry` values and shows a
  chronological Plan list with a detail pane.
- **Organise** lists current and future Trips and shows the selected Trip's
  Plans, review, and management actions.
- **Gallery** lists every past Trip, including Trips without reviews, and
  shows the selected Trip's Plans and review.
- **Trip cells** and **Plan cells** are compact and ellipsize long names;
  details panes and inspectors show the complete values.
- Trip and Plan forms use dialogs with inline validation. Review dialogs use an
  optional 1–5 rating and multiline Notes field.
- The sidebar can create a Trip from any page. A newly created or edited past
  Trip is routed to Gallery; a current or future Trip is routed to Organise.
- Dashboard does not create Plans because it has no selected Trip context.
  Plans are added from a selected Trip in Organise or Gallery.

### 2.7 Startup sequence

~~~mermaid
sequenceDiagram
    actor User
    participant Launcher as DoggoLauncher
    participant App as DoggoApplication
    participant DB as SqliteTripRepository
    participant Service as DoggoService
    participant FXML as AppShell.fxml
    participant Shell as AppShellController

    User->>Launcher: Start application
    Launcher->>App: Application.launch(...)
    App->>DB: Open or create data/doggo.db
    DB->>DB: Validate/create schema version 1
    App->>Service: Construct with repository and system Clock
    App->>FXML: Load shell with controller factory
    FXML->>Shell: Create shell and page controllers
    Shell->>Service: Refresh Dashboard, Organise, Gallery
    Service-->>Shell: Current view data
    App-->>User: Show JavaFX window
~~~

If database initialization or FXML loading fails, the application shows a
startup error with recovery guidance and exits the JavaFX runtime.

### 2.8 Sequence: adding and saving a Trip

~~~mermaid
sequenceDiagram
    actor User
    participant Shell as AppShellController
    participant Dialog as TripCreationDialog
    participant Service as DoggoService
    participant Domain as Trip
    participant Repo as TripRepository
    participant SQLite as SqliteTripRepository

    User->>Shell: Click Create Trip
    Shell->>Dialog: Open empty Trip form
    User->>Dialog: Enter title and inclusive dates
    Dialog->>Dialog: Validate non-blank title, length, and date range
    User->>Dialog: Click Create trip
    Dialog->>Service: createTrip(title, start, end)
    Service->>Domain: Construct validated Trip
    Service->>Repo: save(trip)
    Repo->>SQLite: Save complete aggregate
    SQLite->>SQLite: Commit transaction
    SQLite-->>Repo: Success
    Repo-->>Service: Success
    Service-->>Dialog: Created Trip
    Dialog-->>Shell: Close with created Trip
    Shell->>Service: Refresh status-appropriate list
    Shell-->>User: Select and display the new Trip
~~~

If validation fails, the dialog remains open and the service is not called. If
the repository fails, the dialog displays an error and the previous database
state is protected by the transaction boundary.

### 2.9 Sequence: adding a Plan to a Trip

~~~mermaid
sequenceDiagram
    actor User
    participant View as OrganiseController or GalleryController
    participant Dialog as PlanCreationDialog
    participant Service as DoggoService
    participant Trip as Trip aggregate
    participant Repo as SqliteTripRepository
    participant DB as SQLite database

    User->>View: Select Trip and click + Add plan
    View->>Dialog: Open form with default date
    User->>Dialog: Enter destination, date, and HH:mm time
    Dialog->>Dialog: Validate text, time, and Trip date range
    User->>Dialog: Click Add plan
    Dialog->>Service: addPlan(tripId, destination, date, time)
    Service->>Repo: Find Trip by UUID
    Repo-->>Service: Trip aggregate
    Service->>Trip: withAddedPlan(new Plan)
    Trip-->>Service: Validated updated Trip
    Service->>Repo: save(updated Trip)
    Repo->>DB: Replace Trip and Plans in transaction
    DB-->>Repo: Commit
    Repo-->>Service: Success
    Service-->>Dialog: Created Plan
    Dialog-->>View: Close with Plan
    View->>Service: Refresh and select new Plan
    View-->>User: Show updated Plans (N)
~~~

Editing a Plan follows the same path, replacing the Plan by UUID and retaining
its existing Review. Editing a Trip similarly rebuilds the Trip aggregate and
routes it to Organise or Gallery according to the resulting status.

### 2.10 Activity diagram: Trip creation and routing

~~~mermaid
flowchart TD
    Start([Start]) --> Open[Open Create Trip dialog]
    Open --> Input[Enter title, start date, and end date]
    Input --> Valid{Input valid?}
    Valid -- No --> Error[Show inline validation]
    Error --> Input
    Valid -- Yes --> Create[DoggoService creates Trip]
    Create --> Persist{Repository save succeeds?}
    Persist -- No --> SaveError[Show save error; keep dialog open]
    SaveError --> Input
    Persist -- Yes --> Status{Trip status today}
    Status -- Past --> Gallery[Refresh and select in Gallery]
    Status -- Current or future --> Organise[Refresh and select in Organise]
    Gallery --> End([Done])
    Organise --> End
~~~

### 2.11 Activity diagram: daily navigation

~~~mermaid
flowchart TD
    Select[User selects a primary menu] --> Menu{Selected menu}
    Menu -- Dashboard --> Today[Query Plans whose date is today]
    Today --> Sort[Sort by time and deterministic tie-breakers]
    Sort --> Detail[Select a Plan and show details]
    Menu -- Organise --> Active[Query current and future Trips]
    Active --> TripDetail[Select Trip and show Plans in date/time order]
    Menu -- Gallery --> Past[Query past Trips]
    Past --> Memory[Select Trip and show completed itinerary/reviews]
    Detail --> Action{User action}
    TripDetail --> Action
    Memory --> Action
    Action -- Add/Edit --> Dialog[Open validated form]
    Action -- Delete --> Confirm[Ask for explicit confirmation]
    Action -- Navigate --> Select
    Dialog --> Persist[Service persists successful mutation]
    Confirm --> Persist
    Persist --> Select
~~~

## 3. Requirements

### 3.1 Target user profile

The primary user is an individual traveller who plans one or more trips,
needs a lightweight itinerary, and wants to record memories after returning.
The user may be comfortable with ordinary desktop applications but should not
need database, networking, or command-line knowledge. The user values:

- quick entry of a Trip and its scheduled destinations;
- a “what is happening today?” view;
- a clear separation between upcoming/current planning and past memories;
- private, offline storage of personal travel data; and
- simple correction, review, and deletion workflows.

The CLI additionally supports technically inclined users and regression
testing, while the JavaFX UI is the primary experience for the target profile.

### 3.2 Value proposition

doggo provides one focused place to plan a Trip, manage its Plans, and preserve
reviews without requiring an account or external service. It combines a daily
Dashboard for immediate action, an Organise view for upcoming work, and a
Gallery for completed journeys. Its local SQLite database keeps the workflow
fast and private, while shared application/domain logic keeps the desktop UI
and CLI behavior consistent.

### 3.3 User stories and priorities

Priority meanings: **Must** is required for the current product, **Should** is
important but not a release blocker, and **Could** is a planned extension.

| ID | Priority | User story | Status |
| --- | --- | --- | --- |
| US-01 | Must | As a new user, I can open Dashboard, Organise, and Gallery from persistent navigation so that I can reach every major function. | Implemented |
| US-02 | Must | As a user, I can create a Trip with a title and inclusive start/end dates so that I can represent a journey. | Implemented |
| US-03 | Must | As a user, I can see current/upcoming Trips separately from past Trips so that I can find the relevant journey quickly. | Implemented |
| US-04 | Must | As a user, I can add a Plan with a destination, date, and time to a Trip so that I can build an itinerary. | Implemented |
| US-05 | Must | As a user, I can edit a Trip or Plan so that I can correct changing travel details. | Implemented |
| US-06 | Must | As a user, I can delete a Plan after confirmation so that unwanted itinerary items are removed safely. | Implemented |
| US-07 | Must | As a user, I can delete a Trip after confirmation so that the complete journey can be removed deliberately. | Implemented |
| US-08 | Must | As a daily user, I can see today's Plans in chronological order with their owning Trip so that I can follow my itinerary. | Implemented |
| US-09 | Must | As a user, I can add, edit, or remove a Trip or Plan review with an optional 1–5 rating and Notes so that I can record experiences. | Implemented |
| US-10 | Must | As a returning user, I can find my Trips, Plans, and reviews after restarting doggo so that my data is retained. | Implemented |
| US-11 | Must | As a user, I receive validation and recovery guidance for invalid input so that a mistake does not terminate the application. | Implemented |
| US-12 | Must | As a privacy-conscious user, I can use doggo locally without an account or external data transfer. | Implemented |
| US-13 | Should | As a keyboard user, I can operate the core JavaFX navigation and forms without requiring a mouse. | Design requirement; verify manually |
| US-14 | Should | As a user, I can see complete long names in details while compact cards remain readable. | Implemented |
| US-15 | Could | As a user, I can attach photos to a Plan review so that I can preserve visual memories. | Deferred |
| US-16 | Could | As a frequent user, I can copy a Plan from a past Trip into a future Trip so that useful places can be reused. | Deferred |
| US-17 | Could | As a user, I can search or filter Trips and Plans so that a growing travel history remains manageable. | Deferred |

### 3.4 Use cases

#### UC-01: Create a new Trip

**Primary actor:** Traveller  
**Trigger:** The user clicks **Create Trip** in the persistent sidebar.

**Main Success Story (MSS)**

1. doggo opens the Trip form.
2. The user enters a non-blank title and chooses inclusive start and end
   dates.
3. doggo validates the title length and confirms that the end date is not
   before the start date.
4. **DoggoService** creates a validated Trip and saves it through
   **TripRepository**.
5. The dialog closes and the application refreshes the status-appropriate
   list.
6. The new Trip is selected in Organise when it is current/upcoming, or in
   Gallery when it is past.

**Extensions**

- 2a. The title is blank or longer than 50 Unicode code points: show inline
  validation and keep the form open.
- 2b. The date range is invalid: show an error and do not call the service.
- 4a. The repository cannot save: show a recoverable error and preserve the
  previously committed database state.
- 5a. The user cancels: close the dialog without changing the database.

#### UC-02: Add a Plan to a Trip

**Primary actor:** Traveller  
**Trigger:** The user selects a Trip in Organise or Gallery and clicks
**+ Add plan**.

**Main Success Story (MSS)**

1. doggo opens the Plan form. The date defaults to today when today falls
   within the Trip; otherwise it defaults to the Trip's start date.
2. The user enters a destination, date, and 24-hour **HH:mm** time.
3. doggo validates the destination and confirms that the Plan date is within
   the Trip's inclusive range.
4. **DoggoService** adds the Plan to the Trip aggregate and saves the aggregate
   transactionally.
5. The dialog closes. The Trip details pane refreshes, updates **Plans (N)**,
   and selects the new Plan.

**Extensions**

- 2a. The destination is blank or longer than 50 Unicode code points: show
  validation and keep the form open.
- 2b. The date is outside the Trip: reject the form and explain the allowed
  range.
- 2c. The time is invalid: request a valid 24-hour time.
- 4a. The Trip no longer exists: show an error and refresh the view.
- 4b. Persistence fails: show an error; the transaction rolls back.
- 5a. The user cancels: no Plan is added.

#### UC-03: View today's itinerary

**Primary actor:** Traveller  
**Trigger:** The user opens or selects **Dashboard**.

**Main Success Story (MSS)**

1. **DashboardController** requests **DashboardEntry** values from
   **DoggoService**.
2. The service selects Plans whose local date equals the Clock's current date.
3. The service sorts them by time and deterministic tie-breakers.
4. doggo displays the list and selects the first Plan when one exists.
5. The detail pane shows destination, owning Trip, date, time, and any review.

**Extensions**

- 2a. No Plan is scheduled today: show “No plans scheduled for today” and
  “There are no plans to view.”
- 4a. The selected Plan is edited so it leaves today: refresh the list and
  select another visible Plan when available.
- 1a. The repository cannot be read: show an actionable error and keep the
  application open.

#### UC-04: Edit a Trip or Plan

**Primary actor:** Traveller  
**Trigger:** The user selects an existing record and clicks its Edit action.

**Main Success Story (MSS)**

1. doggo opens a pre-populated form.
2. The user changes the editable fields and submits.
3. doggo validates the new values and invokes the matching service method.
4. The service creates a validated immutable replacement and saves it.
5. Existing reviews are retained. The relevant view refreshes and preserves
   selection when possible.

**Extensions**

- 2a. A Trip edit would place one of its Plans outside the new date range:
  reject the edit.
- 2b. An invalid name, destination, date, or time is entered: keep the form
  open with validation feedback.
- 4a. A Trip changes from past to current/upcoming, or the reverse: route the
  Trip to Gallery or Organise according to its new status.
- 5a. The user cancels: retain the old aggregate unchanged.

#### UC-05: Add, edit, or remove a review

**Primary actor:** Traveller  
**Trigger:** The user clicks **Add Review** or **Edit Review** for a Trip or
Plan.

**Main Success Story (MSS)**

1. doggo opens the review dialog and pre-fills an existing review when present.
2. The user optionally selects a whole-number rating from 1 to 5 and/or enters
   Notes.
3. doggo saves the review through **DoggoService**.
4. The view refreshes its review card and changes the action text to
   **Edit Review** when a review exists.

**Extensions**

- 2a. Both fields are empty for a new review: no review is created.
- 2b. Both fields are cleared for an existing review: remove the review.
- 3a. Only one field is supplied: save a valid rating-only or Notes-only
  review.
- 3b. Persistence fails: display an error and retain the previous review.

#### UC-06: Delete a Plan or Trip

**Primary actor:** Traveller  
**Trigger:** The user clicks a Delete action.

**Main Success Story (MSS)**

1. doggo displays a confirmation dialog identifying the selected record.
2. The user explicitly confirms deletion.
3. **DoggoService** deletes the Plan, or deletes the Trip aggregate through the
   repository.
4. doggo refreshes the current list and selects a nearby remaining item when
   possible.

**Extensions**

- 2a. The user cancels: retain the record and its reviews.
- 3a. Deleting a Trip also removes all owned Plans and their reviews.
- 3b. The record is missing or the repository fails: show an actionable error
  and refresh the view.

#### UC-07: Save data and reopen the application

**Primary actor:** Traveller  
**Trigger:** The user successfully submits a create, edit, review, or delete
operation and later restarts doggo.

**Main Success Story (MSS)**

1. A successful mutation is immediately saved to **data/doggo.db**; there is no
   separate Save button in the JavaFX UI.
2. The user closes the window.
3. The user launches doggo again from the same working directory.
4. **SqliteTripRepository** reads the persisted Trip aggregates and reviews.
5. The records appear in the correct Dashboard, Organise, or Gallery view.

**Extensions**

- 1a. A save fails: SQLite rolls back the aggregate transaction and the UI
  reports the failure.
- 3a. The application is started from a different working directory: it uses
  that directory's **data/doggo.db**, which may be a different database.
- 4a. The database schema is newer than the supported version: startup rejects
  it rather than modifying it.

### 3.5 Non-functional requirements

| ID | Requirement | Design response / verification |
| --- | --- | --- |
| NFR-01 | Usability | Forms provide inline validation; destructive operations require explicit confirmation; repository errors are recoverable messages. |
| NFR-02 | Performance | Common views and commands should complete within two seconds for up to 1,000 Trips and 10,000 Plans on the supported development machine. |
| NFR-03 | Reliability | Complete Trip saves use one transaction and roll back on failure; unsupported schema versions are rejected safely. |
| NFR-04 | Data consistency | Domain invariants enforce valid date ranges, non-empty text, review rules, and Trip–Plan ownership. |
| NFR-05 | Portability | JavaFX native classifiers cover Windows, Intel macOS, Apple Silicon macOS, and Linux; the Gradle wrapper avoids a system Gradle requirement. |
| NFR-06 | Privacy | The production data store is local SQLite and no account or network service is required. |
| NFR-07 | Maintainability | Presentation, application, domain, and storage packages have one-way responsibilities; repository interfaces permit adapters. |
| NFR-08 | Testability | **DoggoService** receives a **Clock** and **TripRepository**, allowing deterministic tests with an in-memory database and fixed dates. |
| NFR-09 | Accessibility | Core JavaFX controls retain keyboard focus and standard activation behavior; manual keyboard navigation should be checked before release. |
| NFR-10 | Determinism | Trips, Plans, and Dashboard entries use stable ordering tie-breakers; dates and times use consistent formats. |

## 4. Glossary

| Term | Meaning in doggo |
| --- | --- |
| **Aggregate** | A group of domain objects updated as one consistency boundary. In doggo, a Trip and its Plans form one aggregate. |
| **Aggregate root** | The object through which an aggregate is changed. **Trip** is the root for its Plans. |
| **Dashboard** | The view of Plans scheduled for the current local date, ordered chronologically and annotated with their owning Trip. |
| **Gallery** | The view of all Trips whose end date is before today. Reviews are optional; an unreviewed past Trip still appears. |
| **Organise** | The view of current and future Trips and their itineraries. |
| **Plan** | A scheduled itinerary item with a destination, date, time, and optional review. |
| **Review** | An immutable optional value containing a 1–5 rating, Notes text, or both. |
| **Trip** | An overall journey with a title, inclusive date range, Plans, and optional review. |
| **Trip status** | A derived classification: **FUTURE**, **CURRENT**, or **PAST**, calculated from Trip dates and the current Clock date. |
| **Application service** | A presentation-independent class that coordinates a use case. **DoggoService** is doggo's application service. |
| **Repository** | An abstraction for storing and retrieving Trip aggregates. **TripRepository** is the port; SQLite and in-memory classes are adapters. |
| **Composition root** | The startup code that constructs concrete dependencies. **DoggoApplication** is the JavaFX composition root and **Doggo** is the CLI composition root. |
| **DashboardEntry** | A read-model value combining a Trip UUID/title with one Plan for Dashboard display. |
| **FXML** | XML markup used to declare JavaFX view structure and controller bindings. |
| **JavaFX cell** | A reusable renderer for one row in a JavaFX **ListView**, such as **TripCell** or **PlanCell**. |
| **MSS** | Main Success Story: the normal sequence of steps in a use case when no extension occurs. |
| **Extension** | An alternate, error, cancellation, or boundary path from a use case. |

## 5. Instructions for manual testing

### 5.1 Test preparation

1. Install and select Java 25.0.3.fx-zulu.
2. Build the packaged application with **./gradlew clean shadowJar** on
   macOS/Linux, or **gradlew.bat clean shadowJar** on Windows.
3. Confirm that **build/libs/doggo.jar** exists.
4. Close any running doggo process.
5. Back up **data/doggo.db** if it contains records that must be preserved.
6. Record today's date. Several checks depend on whether a Trip or Plan is
   current relative to that date.

Run the following packaged JAR from the repository root for the normal manual
test:

~~~bash
java --enable-native-access=javafx.graphics -jar build/libs/doggo.jar
~~~

On Windows, use the same Java command with the Windows path if necessary:

~~~powershell
java --enable-native-access=javafx.graphics -jar build\libs\doggo.jar
~~~

For a clean test, copy **doggo.jar** into a disposable working directory and
run **java --enable-native-access=javafx.graphics -jar doggo.jar** from there.
This creates an isolated **data/doggo.db**. When the JAR is launched from the
repository root, test data is stored in the repository's **data/doggo.db**.
For all GUI cases below, use the packaged JAR; **./gradlew run** is only an
optional developer smoke test.

### 5.2 Launch and shutdown

**Expected result:** doggo starts without an error and closes cleanly.

1. Launch the packaged **doggo.jar** using the command in Test preparation.
2. Confirm that the **doggo** window opens at a usable size with a persistent
   sidebar containing **Create Trip**, **Dashboard**, **Organise**, and
   **Gallery**.
3. Confirm that Dashboard is selected initially and that the three pages use
   the same application shell.
4. Click Organise and Gallery, then return to Dashboard. Each page should load
   without closing the application, and the selected navigation button should
   be highlighted.
5. Close the window using its normal close control.
6. Confirm that the Java process terminates. If the desktop
   database cannot be opened, doggo should show a startup error explaining that
   the database location must be checked.

Optional CLI smoke check:

1. Run **./gradlew runCli**.
2. Enter **help**, then **exit**.
3. Confirm that help is printed and the process returns to the shell without
   changing stored data.

### 5.3 Adding a new Trip

**Expected result:** valid current/upcoming Trips appear in Organise; valid
past Trips appear in Gallery; valid input is persisted.

1. Launch the packaged **doggo.jar**.
2. Click **Create Trip** from Dashboard.
3. Enter a title such as **Singapore Food Weekend**.
4. Choose a start date and end date that include today. Click **Create trip**.
5. Confirm that the dialog closes, Organise is selected, the Trip is visible,
   and its status is **IN PROGRESS**.
6. Repeat with dates entirely after today. Confirm that the Trip appears in
   Organise with **UPCOMING** status.
7. Repeat with an end date before today. Confirm that the Trip appears in
   Gallery with **COMPLETED** status.
8. Verify invalid input: clear the title, enter a title longer than 50 Unicode
   code points, and choose an end date before the start date. Confirm that
   validation prevents submission and the dialog remains open.
9. Click **Cancel** on an additional test form and confirm that no extra Trip
   appears.

### 5.4 Adding a new Plan to a Trip

**Expected result:** a valid Plan is saved under the selected Trip, appears in
chronological order, and updates the **Plans (N)** count.

1. In Organise, select a current or upcoming Trip.
2. Click **+ Add plan**.
3. Confirm that the date defaults to today when today is inside the Trip;
   otherwise confirm that it defaults to the Trip's start date.
4. Enter a destination such as **National Gallery**, a date inside the Trip's
   range, and a time such as **09:00**.
5. Click **Add plan**.
6. Confirm that the dialog closes, the heading changes to **Plans (1)**, and the
   new Plan is selected or visible in the itinerary.
7. Add a second Plan at an earlier time and a third Plan at a later date.
   Confirm that Plans are displayed by date and time rather than insertion
   order.
8. Click **Details** on a Plan and verify its complete destination, Trip,
   date, and time.
9. Open Dashboard. If a Plan's date is today, confirm that it appears there;
   Plans on other dates should not appear in Dashboard.
10. Verify invalid input by trying a blank or over-limit destination, a date
    outside the Trip, and an invalid time. Confirm that the form refuses to
    save and displays guidance.
11. Repeat Steps 1 - 10 from Gallery for a past Trip and confirm that adding a Plan works
    in the selected Trip's completed itinerary.

### 5.5 Saving data

**Expected result:** successful mutations survive a restart and failed
mutations do not replace the last valid state.

1. Create a uniquely named Trip and at least one Plan.
2. Add a Trip review with a rating and Notes. Add a Plan review with Notes
   only, then close each dialog with **Save**.
3. Edit the Trip or Plan and confirm the existing review remains attached.
4. Close the JavaFX window normally. Do not terminate the process forcefully.
5. From the same working directory, relaunch the packaged **doggo.jar** using
   the same command and working directory:
   - Repository root: `java --enable-native-access=javafx.graphics -jar
     build/libs/doggo.jar`
   - Disposable directory: `java --enable-native-access=javafx.graphics -jar
     doggo.jar`
6. Confirm that the Trip, Plan, ratings, and Notes are present in their
   expected views.
7. Clear both fields of an existing review and save. Confirm that the review
   card disappears and the action returns to **Add Review**. Restart once more
   and confirm that the removal persisted.
8. For a backup check, close doggo and copy the complete **data** directory. The
   copy should contain **doggo.db**; never copy the database while doggo is
   writing to it.
9. Optional failure check: make the database location unavailable before a
   save. Confirm that doggo reports the failure and that the last successful
   records remain intact after restarting with the original database.

### 5.6 Additional manual acceptance checks

- Edit a Trip so that it changes from current/upcoming to past and confirm it
  moves from Organise to Gallery. Edit it back and confirm the reverse route.
- Delete a Plan and confirm that its review is deleted and a nearby Plan is
  selected when one remains.
- Delete a Trip and confirm that its Plans and their reviews disappear with
  it. Cancel the confirmation once and verify that nothing changes.
- Add rating-only, Notes-only, and combined Trip/Plan reviews. Confirm that
  optional fields are labelled correctly and long Notes scroll vertically.
- Resize the window and verify that the two master-detail panels remain equal
  width, long card text ellipsizes, and detail text remains readable.
- Use Tab, Shift+Tab, Enter, and the arrow keys through the sidebar, lists,
  dialogs, and confirmation controls. Confirm that core workflows do not
  require a mouse.

## 6. Automated verification commands

The following commands are useful before submitting a change:

~~~bash
./gradlew clean test
./gradlew shadowJar
~~~

The test suite covers domain invariants, application use cases, SQLite
transactions and restoration, CLI behavior, form validators, and JavaFX FXML
loading. Tests that exercise date-sensitive behavior inject a fixed
**java.time.Clock**; isolated application tests use
**InMemoryTripRepository** instead of the production database.
