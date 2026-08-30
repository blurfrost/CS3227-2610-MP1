# doggo Implementation Plan

## Status

- [x] Complete the domain, application, CLI, review, and SQLite foundation
  (Steps 1–11).
- [x] Build the JavaFX presentation over the established services (Step 12).

Completed implementation detail is preserved in the prompt logs and Git
history. This file records durable decisions, the active milestone, and
future work.

## Completed Foundation — Steps 1–11

- The Trip aggregate, Plans, Reviews, derived Trip status, validation, and
  copy-on-write updates are implemented with deterministic ordering.
- Application services provide Trip and Plan CRUD, Dashboard and Gallery
  queries, Clock-backed status checks, review operations, and safe
  cross-context targeting without presentation dependencies.
- The CLI supports Main, Dashboard, Organise, selected-Trip, Gallery, and
  selected-Gallery-Trip navigation with mode-specific parsers, shared indexed
  validation, stable UUID snapshots, actionable errors, and end-to-end
  acceptance coverage.
- SQLite versioned persistence is transactional, restart-safe, review-aware,
  foreign-key constrained, and exposed through the repository abstraction with
  a centralized repository-failure boundary.
- Domain, application, storage, CLI, integration, regression, and acceptance
  tests pass under Java 25.0.3.

## Durable Decisions

- A Trip is the aggregate root and owns zero or more Plans.
- Trip status is derived from inclusive dates and an injected Clock; it is not
  persisted.
- Dashboard is a flat chronological view of today's Plans with owning Trip
  context. Gallery contains every past Trip, including unreviewed Trips.
- Organise contains current and future Trips. Trip creation and successful
  date-changing edits route to the list matching the resulting status.
- Reviews use optional whole-number ratings from 1 to 5 and optional trimmed
  text, with at least one field required. Reviews are independent of Trip and
  Plan scheduled dates, and reviewed records may be edited freely.
- Repository operations remain presentation-independent. Domain and
  application code must not depend on JavaFX, CLI input, JDBC, or filesystem
  paths.

## JavaFX Presentation — Step 12

### 12.1 Configure the cross-platform JavaFX build — Complete

- Set a shared JavaFX version of `25.0.3` in `build.gradle`.
- Add `javafx-base`, `javafx-controls`, `javafx-fxml`, and `javafx-graphics`
  dependencies for each `win`, `mac`, `mac-aarch64`, and `linux` classifier,
  following the
  [SE-EDU JavaFX setup guide](https://se-education.org/guides/tutorials/javaFxPart1.html).
- Include all classifiers in the existing Shadow JAR so one executable JAR
  supports Windows, Intel and Apple Silicon macOS, and Linux.
- Keep SQLite native-access JVM and manifest configuration intact.

### 12.2 Add the JavaFX composition root — Complete

- Add a non-`Application` launcher and a JavaFX `Application` class.
- Construct the existing SQLite repository, system-default Clock, and
  `DoggoService` at startup.
- Make `./gradlew run` launch JavaFX and add a `runCli` task that launches the
  existing CLI entry point.
- Show a startup error dialog and exit cleanly when the database cannot be
  initialized.

### 12.3 Build the application shell — Complete

- Place GUI code under `doggo.ui.javafx`; keep domain, application, storage,
  and CLI packages independent of JavaFX.
- Use FXML and controllers for primary views, with CSS for shared styling and
  Java only for small reusable components such as list cells.
- Use an approximately 1180×760 window with a minimum size around 960×640.
- Add persistent left-sidebar navigation for Dashboard, Organise, and Gallery,
  including active-state and keyboard-focus styling.
- Use warm cream surfaces, charcoal text, terracotta accents, muted sage
  secondary elements, system fonts, and CSS-only decoration.
- Provide styled placeholders for Organise and Gallery during this slice.

### 12.4 Implement the read-only Dashboard — Complete

- Load entries only through `DoggoService.getDashboardEntries()`.
- Display today's date and a chronological `ListView` of Plan cards containing
  time, destination, and owning Trip.
- Select the first entry automatically when entries exist and show an explicit
  empty state otherwise.
- Show the selected Plan in an adjacent detail pane with its Trip, destination,
  full date and time, and optional rating/review.
- Convert repository failures into actionable alerts without terminating the
  application when recovery is possible.
- Preserve keyboard navigation through the sidebar, Plan list, and detail pane.

### 12.5 Verify and document the GUI slice — Complete

- Preserve the existing JUnit suite and add tests for plain-Java presentation
  models or formatting logic without requiring JavaFX startup.
- Add an FXML/resource smoke test where the JavaFX toolkit can be initialized
  reliably.
- Verify `run`, `runCli`, `clean shadowJar`, and the resulting universal JAR.
- Manually verify navigation, empty and populated Dashboard states, ordering,
  Plan selection, review rendering, resizing, keyboard use, and startup-error
  handling.
- Run `git diff --check`, review changed Java with the SE-EDU standard, update
  documentation, and generate `_temp/visual-diff.html` before review.

### 12.6 Add the Organise browsing prototype — Complete

- Replace the Organise placeholder with a current and future Trip list.
- Show the selected Trip's status, date range, plan count, and chronological
  itinerary in a master-detail layout.
- Keep Trip and Plan mutation controls visible as deferred affordances until
  their forms are implemented.
- Preserve the existing service boundary and repository-failure error handling.

### 12.7 Add the read-only Gallery — Complete

- Replace the Gallery placeholder with a past Trip list and selected-Trip
  detail pane.
- Display completed status, inclusive dates, optional Trip review, plan count,
  and chronological itinerary details.
- Refresh Gallery data when its navigation item is selected while preserving
  the current selection when possible.
- Preserve explicit empty and repository-failure states.

### 12.8 Create Trips from the Sidebar — Complete

- Open an app-owned modal form from the persistent sidebar's Create Trip button.
- Default both date fields to the service Clock's current date and validate
  the title and inclusive date range before enabling creation.
- Persist valid Trips through `DoggoService`, refresh and select current or
  future Trips in Organise, and route past Trips to Gallery.
- Keep the modal open with actionable feedback when persistence fails.

### 12.9 Add Plans to selected Trips — Complete

- Add a reusable Plan form for destination, date, and time.
- Make Plan creation available from selected Trips in Organise and Gallery.
- Constrain Plan dates to the selected Trip's inclusive date range and select
  the newly created Plan after a successful save.
- Refresh Dashboard data whenever it is selected so current-day additions are
  visible after Plan mutations in other views.

## Deferred Work

### 12.10 Edit Plans in relevant views — Complete

- Reuse the Plan form for editing existing destination, date, and time values.
- Make Plan editing available in Organise, Gallery, and Dashboard.
- Retain existing Plan reviews during edits and refresh Dashboard when an
  edited Plan no longer belongs in today's list.

### 12.11 Edit Trips with status-aware routing

- Reuse the Trip form for editing a Trip's title and inclusive date range.
- Make Trip editing available in Organise and Gallery.
- Route the updated Trip to Organise or Gallery according to its resulting
  status, while preserving any existing review.

### 12.12 Delete Plans with confirmation

- Make Plan deletion available in Organise, Gallery, and Dashboard.
- Require explicit confirmation and remove the Plan's associated review.
- Refresh the relevant view and preserve or restore a sensible selection after
  successful deletion.

### 12.13 Delete Trips with confirmation

- Make Trip deletion available in Organise and Gallery.
- Require explicit confirmation and remove the Trip, its Plans, and associated
  reviews as one aggregate operation.
- Refresh the current view and handle the resulting empty or next selection
  state without leaving stale details visible.

### 12.14 Add Trip and Plan review forms — Complete

- Add Trip review actions in Organise and Gallery, and Plan review actions in
  Dashboard and selected Trip views in Organise and Gallery.
- Preserve the existing rating, text, and clearing rules without restricting
  reviews by scheduled dates.

The remaining CRUD milestones will be implemented as reusable workflows and
exposed in each view where the selected record context makes the operation
meaningful. Gallery supports Plan maintenance for past Trips, while Dashboard
does not create Plans because it has no selected Trip context.

- Photos, copying past Plans, search, and filtering.
- Native installers such as MSI, DMG, or platform-specific package formats.
