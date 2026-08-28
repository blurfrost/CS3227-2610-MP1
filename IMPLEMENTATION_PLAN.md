# doggo CLI Implementation Plan

## Status

- [x] Steps 1–8: Organise CRUD, stable targeting, parser decomposition,
  Trip status, and Dashboard CLI
- [x] Step 9: Add Gallery navigation and maintenance
- [ ] Step 10: Add reviews

Keep completed implementation detail in the prompt logs and Git history. Keep
this file focused on durable decisions, the active milestone, and future work.

## Completed Milestones

### Organise CRUD and Stable Targeting — Steps 1–5

- Trip and Plan creation, editing, deletion, validation, confirmation, and
  copy-on-write aggregate updates are implemented.
- Displayed one-based indexes resolve through retained UUID snapshots, with
  safe stale-target handling and deterministic refresh after mutations.
- Main, Organise, and selected-Trip flows have parser, command, formatter,
  application, and end-to-end acceptance coverage.

### Parser Architecture — Step 6

- `Parser` owns normalization and global `exit`/`back` handling, then delegates
  through package-private `ModeCommandParser` implementations.
- Main, Organise, selected-Trip, and Dashboard grammar is mode-specific;
  indexed command validation and feedback are shared.

### Trip Status and Clock — Step 7

- Public `TripStatus` derives `PAST`, `CURRENT`, or `FUTURE` from inclusive
  Trip dates without persisting status.
- `DoggoService` uses an injected `Clock` for deterministic current/future,
  past, and Dashboard queries.
- Trip creation routes by status to the resulting Trip list and preserves
  list-first behavior.

### Dashboard — Step 8

- Dashboard renders a flat chronological list of today's Plans with owning
  Trip titles and deterministic tie-breaking.
- Main-to-Dashboard navigation, Dashboard help, Trip creation, and shared Plan
  editing/deletion are implemented.
- Composite Trip/Plan UUID targets keep cross-Trip mutations safe across
  refreshes, reordering, and stale records.
- Dashboard behavior is covered from query through end-to-end CLI tests.

## Durable Decisions

- A Trip is the aggregate root and owns zero or more Plans.
- Dashboard is Plan-centric and is not grouped by Trip.
- Gallery contains every Trip whose end date is before the Clock-derived
  current date, whether or not reviews are present.
- Organise contains current and future Trips once Gallery is available.
- Trip creation and successful date-changing Trip edits route to Gallery for
  past Trips and Organise for current or future Trips.
- Trip creation lands on the owning Trip list. The user explicitly opens the
  Trip with `view NUMBER` before managing its Plans.
- In either selected Organise or Gallery Trip, `new` creates a Plan and stays
  in that Trip view.
- `NUMBER` is a positive one-based index into the currently displayed UUID
  snapshot.
- Blank edit input preserves the current field; invalid replacements reprompt.
- Trip dates are inclusive, and Trip edits cannot exclude existing Plans.
- Deletion confirmation accepts only exact lowercase `yes` or `no`.
- Repository-backed aggregate operations remain presentation-independent.

## Gallery Navigation and Maintenance — Step 9

- [x] Gallery navigation and maintenance subtasks 1–9.
- The Gallery LIST supports Trip creation, editing, and deletion. A selected
  Gallery Trip supports Plan creation, editing, and deletion.
- Trip creation and successful date-changing edits route to the resulting
  status list while preserving list-first behavior; Plan mutations stay in the
  selected Trip view.
- Retained Trip UUIDs and composite Trip/Plan targets, active-list validation,
  confirmation, stale/reclassified protection, and refreshed numbering are
  implemented and covered by unit, integration, and end-to-end tests.
- Gallery maintenance acceptance coverage is complete. Reviews are not part
  of this milestone.

## Review Subtasks

10. [ ] Add an immutable `Review` value with a required whole-number rating
   from 1 to 5 and optional normalized text.
11. [ ] Add optional Trip and Plan reviews to copy-on-write aggregates without
   weakening existing validation or failed-save safety.
12. [ ] Add Clock-backed review eligibility and application operations for
   adding, editing, and removing completed Trip and Plan reviews.
13. [ ] Add Gallery Trip-review commands and rendering with safe UUID targeting.
14. [ ] Add Gallery Plan-review commands and rendering with safe composite
   Trip/Plan targeting.
15. [ ] Complete review acceptance coverage, documentation synchronization,
   Java-standard review, full tests, diff checks, and visual review.

- Gallery acceptance coverage verifies status-aware creation, cross-status Trip
  editing, historical Trip/Plan mutations, refreshed numbering, cancellation,
  stale-target handling, and unchanged behavior in existing modes.

## Future Roadmap

1. [ ] Complete reviews (Step 10).
2. [ ] Add SQLite persistence after the aggregate schema is stable.
3. [ ] Add a centralized `RepositoryException` boundary before connecting a
   fallible repository, with failing read/save/delete tests and cause
   preservation.
4. [ ] Build the JavaFX presentation over the established application
   services.

## Deferred Work

- Do not add repository retries, a broad exception hierarchy, or logging
  infrastructure without concrete persistence requirements.
- Dashboard Plan creation remains deferred because Dashboard has no selected
  Trip.
- Dashboard and Organise Plan detail commands remain unnecessary while their
  existing views display all current Plan fields.
- Photos, copying past Plans, and search/filtering remain future extensions.
