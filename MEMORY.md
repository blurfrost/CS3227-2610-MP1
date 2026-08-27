# Project Memory

## Project

- This repository contains doggo, a local-first Java desktop application for planning and journalling travels.
- The application uses JavaFX for its graphical user interface.
- The project is packaged as a JAR with `./gradlew clean shadowJar`.
- Development begins with a tested CLI before moving to the JavaFX desktop interface.

## Product Model

- A Trip is an overall journey and contains zero or more Plans.
- A Plan is one scheduled itinerary item belonging to a Trip.
- The desktop interface has three primary portions: Dashboard, Organise, and Gallery.
- Dashboard shows Plans scheduled for the current day.
- Organise allows users to select Trips and manage their itineraries.
- Gallery contains every Trip whose end date has passed, whether or not it has reviews.
- Completed Trips and Plans can have a whole-number rating from 1 to 5 and optional written review text.
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
- The project includes local copies of `present-changes-visually`, `seedu-git-standard`, and `seedu-java-coding-standard`.
- Keep this file limited to durable, project-relevant context; keep transient task details in the prompt logs.
