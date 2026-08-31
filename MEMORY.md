# Project Memory

## Project

- This repository contains doggo, a local-first Java desktop application for planning and journalling travels.
- The application uses JavaFX for its graphical user interface.
- Production CLI data is persisted in `data/doggo.db` through the versioned SQLite repository.
- The project is packaged as a JAR with `./gradlew clean shadowJar`.
- JavaFX is the default desktop entry point through `doggo.ui.javafx.DoggoLauncher`; the tested CLI remains available through `./gradlew runCli`.

## Product Model

- A Trip is an overall journey and contains zero or more Plans.
- A Plan is one scheduled itinerary item belonging to a Trip.
- The desktop interface has three primary portions: Dashboard, Organise, and Gallery.
- Dashboard shows Plans scheduled for the current day and refreshes its query
  whenever the mode is selected.
- Organise allows users to select Trips and manage their itineraries.
- Gallery contains every Trip whose end date has passed, whether or not it has reviews.
- Trips and Plans can have an optional whole-number rating from 1 to 5 and optional written review text; each Review must contain at least one field.
- Reviews are implemented as immutable values. `review NUMBER` targets Trips
  from Organise or Gallery and Plans from Dashboard or selected Trip views;
  blank fields preserve existing values, `-` clears a field, and clearing both
  removes the review.
- `DoggoService` review mutations accept Trips and Plans regardless of their
  scheduled dates, and reviewed Trips or Plans may be edited without losing
  their reviews. Reviewability query APIs and CLI eligibility checks have been
  removed.
- Attaching photos to Plan reviews is a future extension.
- `DeveloperGuide.md` is the canonical reference for requirements and domain rules.

## Toolchain

- Java: `25.0.3.fx-zulu`
- JavaFX: `25.0.3`
- IDE: Visual Studio Code

## Developer Context

- The primary user is a student with 4 years of experience.
- The student knows Java, object-oriented programming concepts, and has completed projects using JavaFX.

## Project Practices

- Prompt summaries are maintained in Markdown files under `logs/`.
- Active multi-step implementation work is tracked in `IMPLEMENTATION_PLAN.md`; read it before resuming that work.
- Project-local Codex skills are stored under `.codex/skills/`.
- The project includes local copies of `present-changes-visually`,
  `seedu-git-standard`, `seedu-java-coding-standard`, and `seedu-junit-test`.
- CLI parsing uses a package-private `ModeCommandParser` contract with
  mode-specific parsers for all implemented CLI modes; shared indexed parsing
  uses retained display snapshots for Trip/Plan feedback.
- Dashboard CLI mode is implemented with a Clock-backed current-day Plan query,
  composite Trip/Plan targets, and repository-backed Plan editing and deletion.
- Gallery CLI modes maintain Clock-classified past Trips and their Plans with
  status-aware Trip creation/edit routing; Plan mutations remain in the
  selected context, while retained UUID/composite targets protect stale or
  reclassified records.
- JavaFX composition and shell classes live under `doggo.ui.javafx`; the FXML
  shell provides persistent navigation and Dashboard, Organise, and Gallery
  views.
- The Organise view displays current and future Trips in a selectable list with
  selected-Trip itinerary details and supports adding and editing Plans in the
  selected Trip; other Trip and Plan mutations remain deferred.
- The Gallery view displays past Trips in a selectable list with completed
  status, optional Trip reviews, selected-Trip itinerary details, and adding
  or editing Plans in the selected Trip.
- Dashboard supports editing the selected current-day Plan from its detail
  pane and refreshes its list when the edited Plan leaves today's itinerary.
- Dashboard Plan details identify the owning Trip with a `From` label and an
  underlined Trip name; Organise and Gallery show dynamic `Plans (N)` headings
  with `+ Add plan` aligned to the right.
- Trip reviews can be added, edited, or removed from Organise and Gallery
  through an expandable optional rating-and-Notes dialog; both detail panes
  display the same review card and state-aware neutral Add Review or Edit Review action;
  empty review cards are hidden, short reviews use content-tight adaptive
  sizing, and long review text scrolls within a 200px capped card that reserves
  Plan space.
- Plan reviews can be added, edited, or removed from Dashboard and from a
  reusable Plan details inspector opened by compact Plan cells in Organise and
  Gallery; cells place a complete rating/status cue after the schedule time
  while full review text remains in the detail surfaces, and the inspector
  fits its visible content while resizing after review changes.
- Plan deletion is available in Dashboard, Organise, and Gallery behind a
  shared confirmation dialog; it removes the Plan review and selects the next
  remaining Plan, falling back to the previous one.
- Trip deletion is available in Organise and Gallery behind the same shared
  confirmation dialog; it removes the complete Trip aggregate and selects the
  next remaining Trip, falling back to the previous one.
- Trip creation is available from the persistent sidebar through a modal form
  with a title and inclusive start/end dates; current and future Trips return
  to Organise, while past Trips route to Gallery.
- Trip editing is available from Organise and Gallery detail panes through the
  shared expandable Trip dialog; it prepopulates stored fields, preserves
  reviews, keeps existing Plans inside the edited date range, and routes status
  changes to the matching view.
- New Plan dialogs default to the service Clock's current date when it falls
  within the selected Trip, otherwise to the Trip start date; Trip and Plan
  dialogs support expansion while preserving their opening dimensions as
  minimums.
- New Trip titles and Plan destinations are limited to 50 Unicode code points
  for new or changed values. Legacy over-limit values loaded through restore
  remain usable during non-name updates.
- JavaFX Trip and Plan form validators surface the same name-length rule and
  disable submission through the existing dialog validation flow.
- CLI Trip and Plan creation/edit prompts reject over-limit names and
  reprompt before calling the application service.
- Compact GUI Trip and Plan cards keep names on one line with ellipsis and
  full-value tooltips; detail panes use constrained TextFlow content to retain
  complete wrapped names, including unbroken names.
- Compact Trip and Plan cards constrain both their cards and owning list cells
  to the list viewport, subtracting a visible vertical scrollbar's width so
  ellipsized names do not introduce horizontal scrolling after item growth,
  hidden-mode navigation, or window resizing.
- Dashboard, Organise, and Gallery use equal-width master-detail panels. The
  primary window cannot be resized below its approximately 1180×760 opening size.
- The sidebar Create Trip action is a centered, light, thick-bordered button
  with a minimum height greater than the navigation buttons and a larger
  label than the navigation actions.
- The JavaFX build packages `win`, `mac`, `mac-aarch64`, and `linux` native
  classifiers in its cross-platform Shadow JAR.
- Keep this file limited to durable, project-relevant context; keep transient task details in the prompt logs.
