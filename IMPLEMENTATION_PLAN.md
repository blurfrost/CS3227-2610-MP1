# doggo CLI Implementation Plan

## Status

- [x] Steps 1–8: Organise CRUD, stable targeting, parser decomposition,
  Trip status, and Dashboard CLI
- [ ] Step 9: Add Gallery and reviews

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
- Top-level `new` creates a Trip and enters Organise.

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

## Step 9 — Add Gallery and Reviews

Gallery is delivered in vertical slices so past Trips remain useful before the
review model and review commands are introduced.

### Ordered Subtasks

1. [x] Add read-only Gallery navigation and past-Trip views.
   - Add Main `gallery`, Gallery list and selected-Trip modes, mode-specific
     parsers, `view NUMBER`, `back`, global `exit`, and help text.
   - Query `DoggoService.getPastTrips()`, retain displayed Trip UUIDs, and
     render selected Trip Plans without Organise mutation commands.
   - Change Organise to list `getCurrentAndFutureTrips()` after Gallery becomes
     reachable.
   - Cover empty, boundary, ordering, malformed-index, stale-target,
     read-only, and navigation behavior.
2. [ ] Add Clock-backed status classification and centralized Trip-list
   routing.
   - Add `DoggoService.getTripStatus(Trip)` using the injected Clock.
   - Add one CLI helper that enters Gallery for a past Trip or Organise for a
     current/future Trip and renders the corresponding list.
   - Cover inclusive status boundaries and both routing destinations.
3. [ ] Make Trip creation status-aware in every Trip-creation mode.
   - Add Gallery-list `new` and reuse `NewTripCommand`.
   - Route newly created past Trips to the Gallery list and current/future
     Trips to the Organise list, regardless of whether creation began in Main,
     Dashboard, Organise, or Gallery.
   - Do not automatically open the new Trip.
4. [ ] Add Gallery Trip editing with active-list validation.
   - Route Gallery `edit NUMBER` to the existing `EditTripCommand`.
   - Reuse existing prompts and validation while refreshing the initiating
     list for no-op, invalid, stale, and failed outcomes.
   - After a successful edit, route by the updated Trip status. Past-to-active
     edits enter Organise; active-to-past edits enter Gallery.
5. [ ] Add Gallery Trip deletion.
   - Route Gallery `delete NUMBER` to the existing `DeleteTripCommand`.
   - Preserve exact confirmation and aggregate deletion behavior, including
     eventual removal of owned reviews.
   - Refresh Gallery after deletion or cancellation and safely reject stale or
     reclassified targets before prompting.
6. [ ] Add Plan creation to selected Gallery Trips.
   - Record composite Trip/Plan targets while rendering Gallery Trip details.
   - Route selected-Gallery-Trip `new` to the existing `NewPlanCommand`.
   - Reuse inclusive Trip-date validation and refresh the Gallery Trip detail
     after success or failure.
7. [ ] Add Gallery Plan editing.
   - Route selected-Gallery-Trip `edit NUMBER` to `EditPlanCommand`.
   - Validate the selected Trip and composite Plan target in Gallery mode,
     then refresh chronological ordering after every non-exit outcome.
8. [ ] Add Gallery Plan deletion.
   - Route selected-Gallery-Trip `delete NUMBER` to `DeletePlanCommand`.
   - Preserve exact confirmation, cancellation, stale-target protection, and
     refreshed numbering.
9. [ ] Complete Gallery maintenance acceptance coverage and synchronize
   documentation before starting reviews.
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

### First-Slice Acceptance Criteria

- [x] Gallery includes only past Trips and includes Trips without reviews.
- [x] Organise lists only current and future Trips.
- [x] Gallery selection uses the displayed UUID snapshot and rejects stale or
  out-of-range targets without opening another Trip.
- [x] Selected Gallery Trips display Plans chronologically and expose no Trip
  or Plan mutation commands.
- [x] Gallery detail `back` returns to Gallery; Gallery `back` returns to Main;
  global `exit` remains available.

### Gallery Maintenance Requirements

- Gallery list commands are `new`, `view NUMBER`, `edit NUMBER`,
  `delete NUMBER`, and `back`; global `exit` remains available.
- Selected Gallery Trip commands are Plan-level `new`, `edit NUMBER`,
  `delete NUMBER`, and `back`; global `exit` remains available.
- Existing Trip and Plan CRUD commands and service operations are reused. Do
  not introduce duplicate Gallery-specific mutation commands.
- A displayed Trip must still belong to the initiating Organise or Gallery
  query before prompting. Deleted or reclassified records are stale targets.
- Selected Trip operations must match the selected Trip UUID, and Plan
  operations must resolve through the retained composite target.
- Trip creation and successful Trip editing route by resulting status and show
  the corresponding Trip list. Plan mutations stay in the selected Trip view.
- Invalid input, cancellation, stale targets, and late failures refresh the
  initiating mode without mutating another aggregate.
- Gallery permits historical Plan creation so manually entered past Trips can
  be populated before reviews are added.

### Gallery Maintenance Acceptance Criteria

- [ ] Creating a past Trip from any Trip-creation mode shows it in Gallery;
  creating a current/future Trip shows it in Organise.
- [ ] Gallery Trip edits and deletions reuse existing validation, confirmation,
  copy-on-write, and stable-target behavior.
- [ ] A Trip edited across the past/current boundary moves to and displays in
  the correct list automatically.
- [ ] Selected Gallery Trips support Plan creation, editing, and deletion and
  remain in the historical Trip context after each Plan mutation.
- [ ] Gallery and Organise reject stale or reclassified Trip and Plan targets
  without prompting or mutating a different record.
- [ ] Existing Main, Dashboard, Organise, and selected-Trip behavior remains
  green under the complete test suite.

## Future Roadmap

1. [ ] Complete reviews and Gallery (Step 9).
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
