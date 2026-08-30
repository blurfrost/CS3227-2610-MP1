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
- Dashboard shows Plans scheduled for the current day.
- Organise allows users to select Trips and manage their itineraries.
- Gallery contains every Trip whose end date has passed, whether or not it has reviews.
- Completed Trips and Plans can have an optional whole-number rating from 1 to 5 and optional written review text; each Review must contain at least one field.
- Reviews are implemented as immutable values. `review NUMBER` targets Trips
  from Gallery and Plans from Dashboard or selected Trip views; blank fields
  preserve existing values, `-` clears a field, and clearing both removes the
  review.
- `DoggoService` uses its injected Clock for review eligibility: Trips after
  their end date and Plans at or after their scheduled local date-time. A
  reviewed Trip must remain past and a reviewed Plan cannot be moved later than
  the Clock-derived current date-time, back to an incomplete state.
- Attaching photos to Plan reviews is a future extension.
- `DeveloperGuide.md` is the canonical reference for requirements and domain rules.

## Toolchain

- Java: `25.0.3.fx-zulu`
- JavaFX: `26.0.1`
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
  shell provides persistent navigation, a read-only Dashboard, and styled
  Organise and Gallery placeholders.
- Keep this file limited to durable, project-relevant context; keep transient task details in the prompt logs.
