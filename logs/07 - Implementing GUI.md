# Implementing GUI

## Prompt 1 — Update the Documentation Before JavaFX

The user asked to update the documentation before starting JavaFX source
implementation. `IMPLEMENTATION_PLAN.md` was compacted by assimilating the
completed domain, application, CLI, review, and SQLite work from Steps 1–11
into a completed-foundation summary. JavaFX is now the active Step 12 and the
plan records its build, launcher, application-shell, Dashboard, testing, and
documentation subtasks.

The JavaFX build plan uses version `26.0.1` and follows the SE-EDU setup guide:
`javafx-base`, `javafx-controls`, `javafx-fxml`, and `javafx-graphics` are to
be included for the `win`, `mac`, and `linux` classifiers in one universal
Shadow JAR. The user confirmed that the tutorial classifiers run correctly on
Apple Silicon, so no additional classifier was added to the documented plan.

The agreed first GUI slice uses FXML and controllers, a warm travel-journal
visual style, persistent left-sidebar navigation, JavaFX as the default
`run` target with a separate CLI task, and a read-only Dashboard with a
list-and-detail-pane layout. Organise and Gallery remain styled placeholders
until later GUI milestones. No `build.gradle` or Java source changes were
made in this documentation-first iteration.

## Prompt 2 — Configure the Cross-Platform JavaFX Build

The user asked to begin implementation by configuring `build.gradle`. A shared
JavaFX `26.0.1` version was added with `javafx-base`, `javafx-controls`,
`javafx-fxml`, and `javafx-graphics` dependencies for the `win`, `mac`, and
`linux` classifiers. The existing CLI entry point, native-access settings,
SQLite dependency, and Shadow JAR configuration remain unchanged until the
JavaFX launcher is added. `./gradlew test` and `./gradlew shadowJar` both pass;
the resulting 23 MB JAR contains JavaFX native entries for Windows, macOS,
and Linux. The launcher and default GUI run target remain deferred until the
JavaFX application classes are added.

## Prompt 3 — Add the JavaFX Composition Root

The user asked to proceed with the next GUI iteration. Added the
classpath-safe `DoggoLauncher` and no-argument `DoggoApplication` under
`doggo.ui.javafx`. The application initializes the SQLite repository, system
Clock, and `DoggoService`, opens a temporary placeholder window with the
planned dimensions, and displays a startup error dialog before exiting when
the database cannot be initialized. `./gradlew run` now targets the JavaFX
launcher, while `./gradlew runCli` preserves the existing CLI entry point.

The full JUnit suite, `shadowJar`, and an interactive `runCli` smoke test pass.
The default JavaFX task reached the running application and was stopped after
startup because this verification session cannot provide a persistent GUI
interaction. The implementation plan marks build configuration and the
composition root complete; the FXML application shell is next.

## Prompt 4 — Build the Application Shell

The user asked to implement the next JavaFX iteration. Added an FXML-backed
`AppShellController`, `AppShell.fxml`, and `doggo.css` under
`doggo.ui.javafx`. The shell uses a persistent left sidebar, active navigation
styles, warm travel-journal colors, keyboard-focus styling, and styled
Dashboard, Organise, and Gallery placeholders. The JavaFX application now
loads this shell and injects the initialized `DoggoService` into its
controller; domain, application, storage, and CLI behavior remain unchanged.

The full JUnit suite passes, and `./gradlew run` successfully loads the FXML
and stylesheet. The expected JavaFX warning reports the bundled Zulu runtime
as 25.0.3 while the FXML namespace is 26.0.1; no FXML loading errors occur.
The implementation plan marks the application-shell subtask complete. The
read-only Dashboard data view is the next iteration.

## Prompt 5 — Implement the Read-Only Dashboard

The user asked to implement the next JavaFX iteration. Added
`DashboardController`, `DashboardEntryCell`, and `DashboardView.fxml`. The
Dashboard now loads today's entries through `DoggoService`, renders a
chronological list of time, destination, and Trip cards, selects the first
entry automatically, shows explicit empty and no-selection states, and
displays the selected Plan's schedule and optional review in a detail pane.
Repository query failures produce an actionable alert while leaving the
application open. Organise and Gallery remain placeholders.

The full JUnit suite passes, and `./gradlew run` loads the Dashboard FXML,
stylesheet, list cell, and detail bindings without runtime errors. The
implementation plan marks the read-only Dashboard subtask complete; GUI CRUD
and functional Organise/Gallery views remain future work.

## Prompt 6 — Complete the JavaFX Foundation Verification

The user asked to implement the final iteration of Step 12. Added the
`AppShellFxmlTest` smoke test with explicit JavaFX toolkit initialization and
shutdown, using an in-memory `DoggoService` so the test does not access the
production database. The test verifies that the application shell, included
Dashboard view, controllers, and stylesheet load successfully.

The focused smoke test, complete JUnit suite, and `shadowJar` build pass. The
implementation plan now marks all Step 12 subtasks complete. Functional GUI
CRUD and full Organise/Gallery screens remain deferred.

## Prompt 7 — Assess Readiness for GUI Trip and Plan Creation

The user asked whether any setup remains before transferring the CLI's Trip
and Plan creation behavior into JavaFX, and invited questions about the form
design. The review confirmed that the domain, `DoggoService` creation APIs,
SQLite persistence, cross-platform JavaFX build, application shell, and FXML
test baseline are ready. The remaining preparation belongs within the next GUI
feature slice: replace the Organise placeholder with a real view/controller,
define refresh behavior after navigation and successful mutations, and use a
consistent form-validation and repository-error pattern. No additional domain,
service, database, or build work is required before implementation. UI choices
for form placement, Organise layout, Plan time entry, validation feedback, and
post-save selection remain to be confirmed with the user.

## Prompt 8 — Correct the Active GUI Prompt Log Path

The user clarified that ongoing GUI prompt summaries belong in
`logs/07 - Implementing GUI.md`, not the earlier Dashboard transition log.
Updated `AGENTS.md` to reference the GUI log and moved the preceding GUI
readiness summary into the correct file.

## Prompt 9 — Commit Completed GUI Iterations

The user asked to preserve the completed GUI work before beginning the next
iteration and to bundle commits by iteration using the SE-EDU Git standard.
Committed the read-only Dashboard implementation and its documentation as
`d106b76`, then committed the implementation-plan, active-log, and instruction
path updates as `85a8e84`. The full Gradle test suite passes and the worktree
is clean.

## Prompt 10 — Align the Local JavaFX Runtime

The user asked to try the first configuration-shift iteration after diagnosing
bold-font rendering artifacts. Installed Azul's verified plain Zulu JDK
25.0.3 alongside the existing FX build without changing the global SDKMAN
default. Added `.sdkmanrc` and updated the project standards, memory, and
developer guide to use the plain JDK so JavaFX 26.0.1 comes exclusively from
Gradle rather than conflicting JavaFX 25.0.3 system modules. Runtime and test
verification then exposed that the tutorial's `mac` classifier contains
x86_64 native libraries; replaced it with `mac-aarch64` for the supported Apple
Silicon environment. The focused FXML test, full test suite, and Shadow JAR
build then passed under the plain JDK. The GUI launched through JavaFX 26.0.1
without the former FXML-version or native-architecture warnings; visual
confirmation of the bold-font rendering remains with the user.

## Prompt 11 — Restore the Original JavaFX Runtime Configuration

The user confirmed Arial as the GUI font for now and asked which SDK was in
use, whether the original Gradle and Zulu configuration could be restored, and
to see the updated build. Restored the FX-bundled `25.0.3.fx-zulu` JDK policy,
the original JavaFX `mac` dependency classifiers, and removed the repository
`.sdkmanrc`; retained the Arial stylesheet change. The clean test suite and
Shadow JAR build passed with all 330 tests. The user accepted the runtime
warnings produced by this configuration, and the GUI was launched for visual
inspection.

## Prompt 12 — Add Apple Silicon JavaFX Dependencies

The user asked whether Apple Silicon JavaFX dependencies could coexist with
the existing Windows, Intel macOS, and Linux dependencies and requested the
change. Added the `mac-aarch64` classifier for the JavaFX base, controls, FXML,
and graphics modules while retaining every existing classifier. Updated the
implementation plan, developer guide, and project memory to describe the
four-classifier dependency set. The clean test suite and cross-platform Shadow
JAR build passed with all 330 tests under `25.0.3.fx-zulu`.

## Prompt 13 — Assess JavaFX 25.0.3 Standardisation

The user asked about the effects of changing the Gradle JavaFX dependency
version from 26.0.1 to 25.0.3 to match the FX-bundled Zulu JDK. Confirmed that
stable JavaFX 25.0.3 artifacts are available and compatible with JDK 25.
Explained that alignment should remove the API/runtime mismatch and is low risk
for the current basic GUI, but the two FXML namespace declarations and project
documentation must be changed as well; changing only `build.gradle` would
leave the FXML warning. Also noted the tradeoff of foregoing JavaFX 26 features
and fixes. No version change was made pending the user's decision.

## Prompt 14 — Standardise on JavaFX 25.0.3

The user approved the JavaFX version change. Updated the shared Gradle version,
both FXML namespace declarations, and the project guidance and memory to use
JavaFX 25.0.3 with the existing `25.0.3.fx-zulu` runtime. Historical prompt
entries retain their original version references for an accurate implementation
record. Full verification follows.

JavaFX 25.0.3 resolved successfully for all configured classifiers. The full
`clean test shadowJar` verification passed with all 330 tests under
`25.0.3.fx-zulu`.

## Prompt 15 — Suppress JavaFX Native-Access Warnings

The user asked how to suppress the Java 25 warning emitted when JavaFX loads
native libraries. Confirmed that `build.gradle` already supplies
`--enable-native-access=javafx.graphics` for the Gradle desktop application.
Explained that VS Code or another direct Java launch must include the same VM
argument explicitly; the CLI and tests may continue using their separate
`ALL-UNNAMED` configuration. No project code changes were needed.

## Prompt 16 — Diagnose JAR Native-Access Warnings

The user asked why the JAR still emits native-access warnings after checking
the VS Code configuration. Inspection found that `.vscode/launch.json` only
contains a top-level `vmArgs` property, rather than a Java launch
configuration, so VS Code ignores it; direct `java -jar` and desktop launches
also do not read that file. The Shadow JAR manifest enables native access for
`ALL-UNNAMED`, but the warning identifies the named `javafx.graphics` module,
so the JAR launch command must receive
`--enable-native-access=javafx.graphics` explicitly. No production changes
were made.

## Prompt 17 — Configure VS Code Launch Files

The user asked to configure the VS Code files. Replaced the incomplete
top-level `vmArgs` object in `.vscode/launch.json` with valid Java launch
configurations for the project GUI and the Shadow JAR, both using
`--enable-native-access=javafx.graphics`. Added `.vscode/tasks.json` to build
the Shadow JAR before its launch configuration runs, and aligned the Java test
configuration in `.vscode/settings.json` with the named JavaFX module.

## Prompt 18 — Diagnose Terminal JAR Warnings

The user reported that the warnings still appear when launching from the
terminal. Verified that the current Shadow JAR manifest contains only
`Enable-Native-Access: ALL-UNNAMED`, while the application is loading the
JavaFX `javafx.graphics` named module from the FX-enabled Zulu runtime. The
VS Code configuration cannot affect a terminal command. Clarified that the
module option must be placed before `-jar` in a direct launch command; no
additional source changes were made.

## Prompt 19 — Plan Organise and Gallery GUI Work

The user asked whether the functional Organise and Gallery screens should be
built before GUI forms for creating Trips and Plans, and whether wireframes or
screen-composition suggestions are needed. Recommended first establishing the
read-only browsing and selection structure, then proceeding in thin vertical
slices rather than completing both screens before all mutations: implement
Organise browsing, Trip creation, Plan management, and then Gallery browsing
and reviews. Explained that supplied wireframes are optional because draft
layouts can be derived from the existing Dashboard style, while rough
wireframes or explicit preferences would help settle interaction and layout
choices before FXML implementation.

## Prompt 20 — Construct the Organise Page Prototype

The user asked for a suggested Organise page prototype and planned to provide
comments after reviewing it. Replaced the Organise shell placeholder with a
read-only master-detail view: current and future Trips appear as selectable
cards, while the selected Trip shows its status, date range, plan count, and
chronologically ordered itinerary. Added reusable Trip and Plan cells, shared
styling, service-backed loading and error handling, and disabled Trip/Plan
action buttons as visual affordances for the next implementation slice. Wired
the new FXML controller into the application and smoke test, and updated the
implementation plan and memory to record the partial Organise milestone.
The focused FXML test and full test suite passed.

## Prompt 21 — Implement the Read-Only Gallery

The user selected the first phase of the Gallery-first rollout and asked for
the read-only Gallery implementation. Replaced the Gallery placeholder with a
service-backed master-detail view of past Trips, including completed status,
date range, plan count, optional Trip review, and chronological itinerary
details. Reused the Organise Trip and Plan cells, added completed status
styling, refreshed Gallery data on navigation while preserving selection when
possible, and extended the FXML smoke test for the new view and controller.
Updated project memory and the implementation plan to record both Organise
and Gallery browsing slices as complete; Trip creation, routing, and mutation
forms remain deferred. The focused FXML test passed; full verification follows.

## Prompt 22 — Implement Trip Creation

The user asked to implement Trip creation through the Organise page. Added an
app-owned modal form with title, start date, and end date fields, defaulting
both dates to the injected service Clock's current date. Added inline
validation for blank titles, missing dates, and reversed date ranges; failed
repository saves keep the dialog open with actionable feedback. Successful
current and future Trips refresh and select in Organise, while past Trips
route to the read-only Gallery and become selected there. Added a
Clock-backed `DoggoService.getCurrentDate()` API, package-private validation
coverage, FXML wiring coverage, and styling. The first focused test exposed an
incorrect nested-FXML namespace assertion, which was removed while preserving
the resource smoke coverage. Focused and full Gradle test suites passed.
Trip and Plan editing, deletion, review, and Plan creation remain deferred.

## Prompt 23 — Make Trip Creation Available from Every Mode

The user asked to move the Create Trip action from the Organise page into the
persistent left sidebar, matching the CLI's access from any mode. Moved the
New trip button into a dedicated CREATE section below the EXPLORE navigation,
moved modal creation and status-based routing into the app shell, and removed
the Organise-specific creation callback. Current and future Trips still return
to Organise with the new Trip selected, while past Trips still open Gallery
with the new Trip selected. Updated the Organise guidance, CSS, memory,
implementation plan, and FXML smoke coverage. The existing modal validation,
persistence behavior, and deferred editing work are unchanged.

## Prompt 24 — Place Create Trip Above Explore

The user requested that the Create Trip action appear above the EXPLORE section
in the persistent sidebar, followed by Dashboard, Organise, and Gallery. Moved
the existing sidebar action above the navigation and renamed its visible label
to Create Trip. Updated the Organise guidance and implementation-plan wording;
the modal creation flow, global availability, and status-based routing remain
unchanged.

## Prompt 25 — Record the Trip and Plan CRUD Roadmap

The user asked to add the reviewed implementation plan to
`IMPLEMENTATION_PLAN.md` before continuing development. Replaced the broad
deferred CRUD bullet with ordered JavaFX milestones: Plan creation, Plan
editing, Trip editing, Plan deletion, Trip deletion, and finally Trip and Plan
reviews. Recorded that each workflow should be reusable and exposed in every
relevant view: Plan creation in Organise and Gallery; Plan editing and deletion
in all three views; and Trip editing and deletion in Organise and Gallery.
Confirmed that Gallery supports maintenance of past-trip Plans, while Dashboard
does not create Plans because it has no selected Trip context.

## Prompt 26 — Implement Plan Creation

The user asked to implement the first CRUD roadmap iteration. Added an
app-owned Plan creation dialog and exposed an enabled Add plan action whenever
a Trip is selected in Organise or Gallery, including past Trips. The form
validates destination, strict `HH:mm` time, and inclusive date bounds within
the selected Trip; its date picker disables dates outside that range. Successful
creation persists through `DoggoService`, refreshes the selected Trip, and
selects the new Plan. Persistence and validation failures keep the dialog open.
Added validator coverage, updated memory and the implementation plan to mark
12.9 complete, and kept Dashboard Plan creation and other mutations deferred.

## Prompt 27 — Include Plan Years in Displayed Dates

The user reported that Plan cards displayed only day and month, which could be
ambiguous for multi-year Trips. Updated the shared Organise/Gallery Plan cell
to display dates as `D MMM YYYY (Day) HH:mm`, using the calendar year and a
three-letter weekday, and added focused JavaFX renderer coverage for the exact
`30 Aug 2026 (Sun) 23:30` presentation.

## Prompt 28 — Refresh Dashboard After Plan Creation

The user reported that creating a current-day Plan in Organise did not update
the Dashboard. Updated the shell to refresh Dashboard data whenever its
navigation mode is selected, exposed a service-backed Dashboard refresh method,
and added a regression test that adds a current-day Plan after shell loading
before returning to Dashboard. Existing Plan creation behavior and selection
remain unchanged.

## Prompt 29 — Default Plan Date to the Current Date

The user requested that the Plan creation dialog initially select the current
date instead of the selected Trip's first day. Updated the dialog to use the
injected service Clock's current date as its DatePicker value, while retaining
the existing inclusive Trip-date validation and disabled out-of-range dates.

## Prompt 31 — Blend DatePicker Fields with Form Styling

The user asked to experiment with styling the Trip and Plan creation dialogs'
DatePicker fields so they blend with regular form fields while retaining the
calendar icon. Added CSS for the DatePicker container, internal text field, and
arrow button to use the shared cream background, border radius, focus styling,
and a subtle arrow hover state without changing calendar behavior.

## Prompt 30 — Commit the GUI Changes Excluding Logs and Plan Files

The user requested a commit containing the current GUI implementation and test
changes while leaving the logs and implementation-plan files uncommitted.

## Prompt 32 — Edit Plans in All GUI Modes

The user requested implementation of the next plan milestone: editing Plans in
Organise, Gallery, and Dashboard. Confirmed that one reusable modal Plan form
would serve both creation and editing, with existing fields prefilled,
reviews retained under the service's eligibility rules, and successful edits
refreshing the originating view while preserving selection where possible.
Added edit actions to Organise and Gallery Plan cells, placed Dashboard's edit
action in the selected Plan detail pane between the schedule and review card,
and made Dashboard refresh/reselect edited current-day Plans or remove Plans
that no longer belong to today's list. Added dialog, cell, FXML, and
persistence coverage; marked implementation-plan milestone 12.10 complete.

## Prompt 33 — Allow Editing Reviewed Plans

The user requested removing the restriction that prevented changing a Plan's
destination, date, or time after it had been reviewed, with no warning shown
for such edits. Removed the reviewed-Plan completion-time guard from
`DoggoService.editPlan`, retained the existing review through the immutable
Plan update, and changed application, GUI-plan, and developer-guide records to
describe the new behavior. Updated the service regression test to verify that a
reviewed Plan can be moved after its completion time while keeping its review.

## Prompt 34 — Reassess Plan Review Eligibility After Editing Changes

The user asked whether `isPlanReviewable` is still relevant now that reviewed
Plans may be edited freely, and requested removing it and its references if it
is no longer needed. Inspection found that the edit restriction has already
been removed, but `isPlanReviewable` still independently enforces the rule that
new or replacement Plan reviews are allowed only at or after the Plan's
scheduled time in `DoggoService` and the CLI review flow. No code was removed;
clarification is required before changing that separate review-eligibility
rule.

## Prompt 35 — Discuss Unrestricted Trip and Plan Reviews

The user asked whether allowing Trip and Plan reviews before their scheduled
completion creates any logical problem, and argued that unrestricted reviews
would give users more freedom without forcing artificial date changes.
Inspection confirmed that review timing is a product policy rather than a
domain or persistence invariant: reviews can technically remain attached to
future, current, or past records. Discussed the semantic tradeoff that
"review" may then include expectations or planning notes, plus the UI
consistency work needed because future Trip reviews are not currently exposed
or displayed in Organise. Recommended that unrestricted reviews are coherent
if doggo intentionally treats reviews as user-owned annotations and updates
its requirements, eligibility checks, and relevant UI entry points together.

## Prompt 39 — Implement the First CLI Review-Availability Iteration

The user requested implementation of the first iteration for standardizing CLI
review availability. Generalized `ReviewTripCommand` to support both top-level
Organise and Gallery Trip lists, using retained-ID validation against the
initiating mode and refreshing that same list after operations. Added Organise
coverage for current and future Trip reviews, review removal, mode retention,
and stale reclassification; updated Gallery expectations to use the shared
stale-target message. Selected Trip views continue to use `review NUMBER` for
Plans, and parser/documentation changes remain deferred to later iterations.

## Prompt 37 — Implement the Second Review-Policy Iteration

The user requested implementation of the second iteration for unrestricted Trip
and Plan reviews. Removed `isTripReviewable`, `isPlanReviewable`, and their
unused time-based service logic; removed both CLI Plan review eligibility
checks; and simplified Gallery Trip lookup to retain only Gallery membership
validation. Updated CLI wording and regression tests so future Plans can be
reviewed through existing review flows while stale-target protections remain.
The broader Developer Guide and implementation-roadmap wording remains
deferred to the documentation iteration.

## Prompt 38 — Implement the Third Review-Policy Iteration

The user requested the next iteration after removing service and CLI review
eligibility checks. Updated the Developer Guide, implementation roadmap,
`Review` documentation, and project memory to state that reviews may be added,
updated, or removed regardless of scheduled dates and that reviewed Trips or
Plans may be edited freely. Kept date-based Trip status and Gallery wording
where it describes classification rather than review eligibility.

## Prompt 36 — Implement the First Review-Policy Iteration

The user requested implementation of the first iteration for unrestricted Trip
and Plan reviews. Removed application-service rejection paths that required a
Trip to be past or a Plan to have reached its scheduled time, and removed the
reviewed-Trip date-edit restriction. Updated service tests to verify reviews on
current/future records and reviewed Trips moving out of the past while
retaining their reviews. The public reviewability query methods and CLI
eligibility checks remain temporarily for the next iteration, and the broader
Developer Guide and implementation-roadmap wording is deferred.

## Prompt 40 — Implement the Second CLI Review-Availability Iteration

The user requested implementation of the next CLI review-availability iteration. Added `review NUMBER` to
top-level Organise mode for Trip reviews, updated Organise help text, and added parser, formatter, and end-to-end
coverage for adding, editing, and removing a Trip review while remaining in Organise. Selected Organise Trip views
continue to use `review NUMBER` for Plans, and no documentation-roadmap files were changed.

## Prompt 41 — Implement the Third CLI Review-Availability Iteration

The user requested implementation of the next CLI review-availability iteration. Added end-to-end coverage proving
that Plan reviews work in selected Organise and selected Gallery Trip views, complementing the existing Dashboard
coverage. Updated the Developer Guide and implementation roadmap to document and mark complete the standardized
Trip and Plan review actions across their relevant CLI modes.

## Prompt 42 — Commit the Review-Availability Changes

The user requested committing the completed workspace changes. Staged the CLI review-standardization code, tests,
documentation, implementation roadmap, memory, and prompt log, then created commit `b512572` with the subject
`Standardize CLI Trip and Plan reviews`. The required prompt summary was added to the same commit afterward.

## Prompt 43 — Implement PlanCreationDialog Changes First

The user requested implementing the PlanCreationDialog portion of the date-default and expandable-dialog
enhancement before changing TripCreationDialog. Updated new Plan creation to default to the service Clock's current
date when it lies within the selected Trip and to the Trip start date otherwise. Added shared expand-only dialog
window configuration for Plan creation and editing, with deterministic JavaFX coverage for date partitions,
resizability, opening-size minimums, and existing edit-date behavior. TripCreationDialog remains deferred to the next
slice.

## Prompt 45 — Implement the Plan Dialog Minimum-Size Lifecycle Fix

The user requested implementation of the lifecycle fix after diagnosing that minimum dimensions were captured too
early. Updated the shared Plan-dialog window support to configure minimum dimensions from the underlying Stage's
`onShown` event, which is after the final opening size for `showAndWait()`. Reworked the JavaFX tests to avoid flaky
modal-window interaction while retaining deterministic coverage for Plan date defaults, dialog resizability, and
opening-size minimum configuration. The complete Gradle suite passes; TripCreationDialog remains unchanged.

## Prompt 44 — Implement the Plan Dialog Minimum-Size Fix

The user requested implementation of the Plan dialog lifecycle fix. Updated `DialogWindowSupport` to apply opening
dimensions when the underlying Stage is shown, preventing Add Plan and Edit Plan dialogs from shrinking below their
opening size while preserving expand-only behavior. Added date-boundary and dialog-configuration coverage, kept
TripCreationDialog unchanged, and verified the focused JavaFX tests and complete Gradle suite.

## Prompt 46 — Commit the New Plan-Date Changes First

The user requested a focused commit for the new Plan-date default behavior. Isolated and committed the logic and
tests as `bda3025` (`Set valid default dates for new Plans`), leaving dialog-resizing changes, logs, and memory
updates unstaged for a later commit.

## Prompt 47 — Extend Dialog Resizing to Trip Creation

The user requested extending the expand-only resizing behavior from Plan creation and editing to Trip creation.
Updated `TripCreationDialog` to use the shared dialog-window configuration, and extended the JavaFX resizability
coverage to include Trip creation. The implementation plan remains unchanged.

## Prompt 49 — Implement Name-Length Iteration 1

The user requested implementation of the first small iteration from the name-length plan. Added 50-Unicode-code-
point limits to new and changed Trip titles and Plan destinations at the domain boundary, while allowing legacy
over-limit values loaded through `restore` to remain usable during non-name copy operations. Added boundary,
rejection, and legacy-compatibility domain tests. The complete Gradle test suite passes; GUI, CLI, and documentation
iterations remain deferred.

## Prompt 50 — Commit Dialog-Resizing Changes First

The user requested committing the dialog-resizing changes separately. Staged and committed the shared dialog-window
helper, Trip and Plan dialog integration, and JavaFX coverage as `fc660ba` (`Make creation dialogs expand-only`).
The name-length domain changes, memory, and logs remain outside that commit.

## Prompt 51 — Implement Name-Length Iteration 2

The user requested implementation of the next name-length iteration. Updated the JavaFX Trip and Plan form
validators to reject values over 50 Unicode code points with live validation messages, using the existing dialog
button-disable flow. Added exact-limit and over-limit validator tests. The CLI and compact-card display iterations
remain deferred, and the complete Gradle test suite passes.

## Prompt 48 — Consider Trip and Plan Name Limits

The user asked whether limiting Trip and Plan names to 30 characters would reasonably reduce GUI formatting issues.
Concluded that a limit is useful for data quality but should not replace resilient wrapping or ellipsis because
characters have different rendered widths. Recommended a less restrictive shared limit around 50–60 characters,
or separate limits for Trip titles and Plan destinations, with validation enforced outside the GUI as well. No
validation behavior was changed.

## Prompt 52 — Implement CLI Name-Length Validation

The user reported that CLI creation crashed when Trip or Plan names exceeded 50 characters and requested input
validation. Added shared CLI text validation for the 50-Unicode-code-point domain limits, applied it to Trip and Plan
creation and editing prompts, and added regression tests confirming over-limit input is rejected and reprompted.
Focused CLI tests and the complete Gradle test suite pass; GUI compact-card display handling remains deferred.

## Prompt 53 — Implement Compact GUI Name Display

The user requested implementation of the next name-length iteration. Configured Trip, Plan, and Dashboard compact
cards to keep names on one line, shrink around fixed status/action content, render overflow with ellipsis, and expose
the complete value through tooltips. Added JavaFX coverage for Trip, Plan, and Dashboard cell label behavior; detail
pane wrapping remains unchanged. The complete Gradle test suite passes.

## Prompt 54 — Diagnose Missing Compact-Card Ellipses

The user reported that long GUI names are clipped instead of visibly ending with ellipses and asked which labels
wrap. Inspection found that compact Trip, Plan, and Dashboard card labels are configured as single-line labels with
`OverrunStyle.ELLIPSIS`, but their card widths are not explicitly constrained to the available ListView cell width;
the parent can therefore clip the card before the label applies its overrun rendering. The wrapped name labels are
the Dashboard detail destination and trip labels, and the Organise and Gallery detail trip-title labels. Form fields
remain single-line and scroll horizontally. No application code was changed.

## Prompt 55 — Fix Organise and Gallery Trip-Detail Wrapping

The user reported that long Trip details in Organise and Gallery were still being clipped while Dashboard Plan
details wrapped. Added an explicit infinite maximum width to both Organise and Gallery detail Trip-title labels so
their existing `wrapText` setting can use the full detail-panel width. Added a JavaFX regression test covering both
views, and the complete Gradle test suite passes.

## Prompt 56 — Reassess Master-Detail Layout and Overflow

The user showed that a long Trip destination still failed to wrap in the Trip Details pane and requested an
explanation of the current left and right panel sizing. Agreed to evaluate equal panel widths, ellipsized Trip names
in compact cards without horizontal scrolling, and wrapped full names in detail panes without allowing the right
panel to expand into the left panel. Implementation decisions were deferred pending clarification.

## Prompt 57 — Plan the Layout Fix Iteratively

The user requested an iterative implementation plan. Proposed five slices: establish equal-width panels across all
three views, fix detail-name wrapping, constrain Trip cards for ellipses, extend the constraint to Plan cards, and
perform integrated verification at default and minimum window sizes.

## Prompt 58 — Prepare the Layout Plan for Documentation

The user requested adding the agreed layout plan to `IMPLEMENTATION_PLAN.md`. Because the session was in planning
mode, provided a decision-complete documentation amendment without changing repository files.

## Prompt 59 — Add the Layout Plan and Correct Review-Form Status

The user requested applying the layout iterations to `IMPLEMENTATION_PLAN.md` and correcting Step 12.14 because the
Trip and Plan review forms are not complete. Added Step 12.15 with five incremental layout and verification slices,
and removed the incorrect Complete marker from Step 12.14.

## Prompt 60 — Implement Master-Detail Layout Iteration 1

The user requested implementing the first iteration of Step 12.15. Replaced the outer two-panel HBox in Dashboard,
Organise, and Gallery with equal 50/50 GridPane columns, removed panel minimum widths that prevented shrinking, and
preserved the existing 18 px gap and 960×640 application minimum. Added JavaFX coverage for all three views at the
default and minimum-width targets; the focused test passes. Marked Iteration 1 complete in the implementation plan.

## Prompt 61 — Implement Master-Detail Layout Iteration 2

The user requested the next iteration of Step 12.15. Constrained detail content in Dashboard, Organise, and Gallery
to the fixed right-hand panel and changed detail name fields to character-wrapping TextFlow content so long unbroken
names wrap instead of being clipped. Preserved complete visible names and existing styling, added rendered JavaFX
coverage at default and minimum content widths, marked Iteration 2 complete, and verified the complete Gradle suite.

## Prompt 62 — Implement Master-Detail Layout Iteration 3

The user requested the next iteration of Step 12.15. Added a reusable card-width binding for Trip cells so compact
Trip cards fit their list-cell viewport, preserve the status badge, ellipsize long titles, and retain full-value
tooltips. Added rendered Organise and Gallery coverage confirming no visible horizontal scrollbar, marked Iteration 3
complete, and verified the complete Gradle suite.

## Prompt 63 — Remove Status Badges from Compact Trip Cards

The user requested removing the “Now”, “Upcoming”, and “Completed” labels from compact Trip cards in the Organise
and Gallery Your Trips/Past Trips lists while preserving them in the individual Trip Details cards. Removed status
rendering and its service dependency from `TripCell`, retained the detail-pane status badges, and added regression
assertions that compact cards contain only their title and date range. The complete Gradle test suite passes.

## Prompt 64 — Implement Master-Detail Layout Iteration 4

The user requested the next iteration of Step 12.15. Applied the shared list-cell width constraint to Plan cards in
Dashboard, Organise, and Gallery so long destinations and trip names use ellipses without introducing horizontal
scrolling, while retaining dates, times, and Edit actions. Added rendered JavaFX coverage for all three views,
marked Iteration 4 complete, and verified the complete Gradle test suite.

## Prompt 65 — Implement Master-Detail Layout Iteration 5

The user requested the final iteration of Step 12.15. Added integrated JavaFX coverage for both the default and
minimum shell sizes, navigation between all three modes, selection changes, wrapped detail names, ellipsized Trip
and Plan cards, and hidden horizontal scrollbars. Strengthened the shared compact-card helper to constrain both
cards and their virtualized ListView cells through resize and hidden-mode transitions, marked Iterations 4 and 5
complete, and verified the complete Gradle test suite and formatting checks.

## Prompt 66 — Tighten Compact-Card Widths

The user reported that the final layout sizing change left Organise and Gallery Trip cells slightly too wide,
causing a small horizontal scrollbar. Increased the shared compact-card safety margin so Trip and Plan cards and
their owning ListView cells stay within the actual viewport. Focused JavaFX tests, the complete Gradle suite, and
formatting checks pass.

## Prompt 67 — Reconsider Compact-Card Viewport Sizing

The user provided a screenshot showing that Organise still displays a horizontal scrollbar alongside the vertical
scrollbar and asked for Iteration 4 to be reconsidered. Inspection found that the current helper subtracts a fixed
margin from the outer ListView width, which does not represent the VirtualFlow viewport once a vertical scrollbar is
present. Recommended reopening Iteration 4 around VirtualFlow-owned cell sizing, avoiding arbitrary margins and
scrollbar-hiding-only fixes, and adding regression coverage with enough Trips to force vertical scrolling.

## Prompt 68 — Implement Scrollbar-Aware Compact Cards

The user requested implementation of the revised Iteration 4 plan. Updated the shared compact-card width binding to
subtract the rendered vertical scrollbar width whenever it is visible, while retaining the small ListView skin
margin. Added rendered regression coverage that grows Organise and Gallery from one Trip to enough Trips to require
vertical scrolling, plus vertically overflowing Plan lists in Dashboard, Organise, and Gallery at default and
minimum content widths. The tests confirm cards shrink dynamically and horizontal scrollbars remain hidden; the
focused JavaFX tests, complete Gradle suite, and formatting checks pass.

## Prompt 69 — Commit Name and Layout Improvements

The user requested committing the completed changes while excluding logs and `IMPLEMENTATION_PLAN.md`. Committed
the domain, CLI, JavaFX, resource, test, and memory updates together as `0a70f26` (`Harden name handling and JavaFX
layouts`), leaving the requested documentation files unstaged.

## Prompt 70 — Revisit Trip Editing Requirements

The user requested revisiting deferred item 12.11 before implementation: Edit Trip should reuse the expandable
Trip dialog and prepopulate the selected Trip's existing fields. Inspected the current placeholder buttons, shared
dialog sizing support, status-aware views, and domain rule requiring all Plans to remain within the Trip dates.
Asked for confirmation of the existing detail-pane button placement and how proactively the dialog should prevent
date ranges that exclude existing Plans; status-crossing edits will route to and select the Trip in the appropriate
view while preserving its review.

## Prompt 71 — Implement Trip Editing

The user confirmed activating the existing Edit trip buttons and proactively restricting edited date ranges. Extended
the shared Trip dialog to support editing with prefilled title and dates, expandable sizing, Plan-aware date-picker
boundaries, and validation. Wired Organise and Gallery detail actions, added status-aware shell routing, preserved
reviews through service edits, added JavaFX and validator regression tests, marked item 12.11 Complete, and verified
the focused tests.

## Prompt 72 — Commit Trip Editing Changes

The user requested committing the Trip-editing implementation while excluding `IMPLEMENTATION_PLAN.md` and all log
files. The commit will include the JavaFX dialog, controller, FXML, validator, tests, and durable memory update;
the excluded documentation files will remain unstaged.

## Prompt 73 — Plan and Implement Plan Deletion

The user requested implementing item 12.12 with Delete beside Edit in Dashboard, Organise, and Gallery, plus a
simple Yes/No confirmation dialog. Confirmed the recommended behavior: destructive red Delete styling, No as the
safe keyboard default, next-then-previous reselection after deletion, cancellation without mutation, and safe
handling of stale or failed deletions.

## Prompt 74 — Implement Plan Deletion

The user requested implementation of the confirmed item 12.12 plan. Added a shared non-expandable Delete-a-plan
dialog with wrapped names, safe No default, and destructive styling; added Delete beside Edit in Dashboard,
Organise, and Gallery; wired aggregate deletion, refresh, next-then-previous reselection, cancellation, and error
handling; added JavaFX and service regression coverage; marked item 12.12 Complete; and verified the focused
JavaFX tests.

## Prompt 75 — Refine Plan Deletion Styling

The user requested matching Dashboard Delete and Edit heights, distinguishing the confirmation actions with a red
Yes and neutral outlined No, and placing Yes to the left of No while retaining No as the Enter default. Updated
shared CSS, enforced explicit dialog button order, preserved No as the cancel/default action, expanded JavaFX
regression assertions, and verified the full test suite.

## Prompt 76 — Match Delete Button and Confirmation Styling

The user requested making Dashboard Delete match Edit height, making Yes visibly red and No neutral with a reddish
outline, and swapping the dialog order while keeping No as the Enter default. Matched shared button padding,
added confirmation-specific colors, forced Yes-before-No ordering independently of platform defaults, added a
rendered height regression test, and verified the complete test suite.

## Prompt 77 — Re-verify Deletion Dialog Styling

The user reported that the Yes/No order was correct but the requested styling changes were not visible. Computed
JavaFX styles showed that later `.dialog-pane .button:default` and `.dialog-pane .button:not(:default)` rules were
overriding the custom classes. Added higher-specificity confirmation-button rules, kept Yes left of No with No as
the default, added a regression check for the rendered colors, and verified the complete test suite.

## Prompt 78 — Protect Minimum Window and Plan Actions

The user requested preventing the main window from shrinking below its opening size and constraining Organise and
Gallery Plan destinations so long text cannot truncate Edit or Delete. Set the opening stage dimensions as its
minimum after the decorated window is shown, reserved each Plan cell's action area at its preferred width, retained
destination ellipses and tooltips within the remaining width, and added rendered JavaFX regression coverage. The
focused JavaFX tests, complete Gradle suite, and coding-standard checks pass.

## Prompt 79 — Plan Trip Deletion with Confirmation

The user requested implementing item 12.13: delete Trips with confirmation in Organise and Gallery, reuse the Plan
deletion dialog, use the exact Trip title and description, keep Yes destructive and No as the Enter default, and place
Delete trip after + Add plan in the Trip Details pane. Generalized the confirmation dialog, confirmed adjacent
reselection as the default behavior, and retained sentence-case destructive styling for the new action.

## Prompt 80 — Implement Trip Deletion with Confirmation

The user requested implementing the agreed Trip-deletion plan. Added the shared `DeletionConfirmationDialog` for Plan
and Trip messages, wired red Delete trip actions into Organise and Gallery, deleted Trip aggregates through the
existing service operation, refreshed views with next-then-previous reselection and empty states, and added dialog,
layout, and aggregate-review regression coverage. Marked item 12.13 complete; focused and complete Gradle checks
pass.

## Prompt 81 — Commit Trip Deletion Changes

The user requested committing the current changes while excluding the implementation plan and prompt logs.
The implementation and tests had already passed the complete Gradle checks; stage the remaining project files,
review the staged diff, and create a convention-compliant commit.

## Prompt 82 — Plan Dashboard and Trip Details Visual Updates

The user requested changing Dashboard's Trip attribution to `From TRIP NAME` with only the Trip name underlined,
renaming the Organise and Gallery itinerary heading to a dynamic `Plans (N)`, and moving `+ Add plan` to the right
of that heading to prepare space for future Trip reviews. The agreed scope is layout only; Trip Review behavior
remains deferred. The implementation should preserve wrapping, existing button behavior, and add JavaFX regression
coverage for the label content, plan counts, and control placement.

## Prompt 83 — Implement Dashboard and Trip Details Visual Updates

The user requested implementing the visual-update plan. Added mixed-style Dashboard Trip text with a `From` prefix
and underlined Trip name, dynamic `Plans (N)` headings in Organise and Gallery, and right-aligned `+ Add plan`
controls beside those headings while retaining Edit/Delete actions above. Added focused JavaFX assertions and
updated durable project memory; full checks and visual review remain to be completed.

## Prompt 84 — Remove Unhelpful Trip Summary Lines

The user requested removing the `X plans · A trip worth remembering` line from Gallery and the
`X plans · This trip is happening now` line from Organise because they add little value. Remove the
summary labels and their now-unused controller formatting code while retaining the dynamic Plans (N)
heading and Trip status badge.

## Prompt 85 — Plan Trip Reviews in Organise and Gallery

The user requested a Trip Review action between Edit and Delete in Organise and Gallery, a `Review a trip`
dialog with an optional 1–5 rating and multiline Notes, always-enabled Save and Cancel actions, and the Gallery
Trip review card mirrored in Organise. Selected five numeric toggle buttons, expandable dialog behavior, existing
review prefill, empty-form removal, and state-aware `Add Review` or `Edit Review` button text; Plan Reviews remain
deferred to the next item 12.14 iteration.

## Prompt 86 — Implement Trip Reviews in Organise and Gallery

The user requested implementing the agreed Trip Review plan. Added a reusable expandable Review dialog with five
clearable numeric rating buttons and multiline Notes, wired state-aware Add/Edit Review actions into Organise and
Gallery, mirrored the Trip review card in Organise, and centralized review display formatting. Empty saves remain
valid, clearing both fields removes an existing review, and focused JavaFX coverage verifies layout, display,
prefill, persistence, and removal. Focused and complete Gradle checks pass.

## Prompt 87 — Label Trip Review Notes

The user requested that Trip Review cards display written Notes as well as ratings, with explicit field labels and
no placeholder lines for omitted optional fields. Updated the shared review formatter so rating-only, Notes-only,
combined, and empty reviews match the requested output, changed the Trip Review heading to title case, and updated
regression coverage. Focused and complete Gradle checks pass.

## Prompt 88 — Diagnose Combined Trip Review Display

The user questioned whether only one of Rating or Notes is visible because the Trip Review container has insufficient
height. Inspected the formatter, FXML, CSS, controllers, and focused JavaFX tests. The formatter produces both fields
with a newline, and the review labels wrap without an explicit maximum height, so no max-height restriction was found.
Focused review-display and FXML tests pass; likely causes to investigate if the running app still shows one field are
stale application resources, persisted review data containing only one field, or clipping outside the review label.

## Prompt 89 — Reproduce Combined Trip Review Clipping

The user provided the exact Review combinations and confirmed that Notes disappear only when Rating and Notes are both
present. Verified that the production SQLite database retains both fields and reproduced the Organise layout at the
1180×760 opening size. The two-line label contained both values but was compressed from its 33 px preferred height to
its 16 px one-line minimum; preserving the label's preferred minimum height displayed both lines and transferred the
17 px reduction to the Plan list. No production fix was applied pending the user's direction.

## Prompt 90 — Preserve Combined Review Label Height

The user asked whether the combined Rating-and-Notes display issue had been fixed and requested changing the label's
minimum height if not. Set each Dashboard, Organise, and Gallery review label to use its preferred height as its
minimum, preventing the surrounding VBox from compressing a two-line review to one line. Added FXML regression
assertions for all three views; focused tests and the complete Gradle test/check suite pass.

## Prompt 91 — Bound Long Trip Review Text

The user requested restricting the Trip Review container to roughly 30–35% of the Trip Details area and adding a
vertical scrollbar when notes exceed that space. Added a shared 35% maximum-height binding, placed review labels in
fit-to-width ScrollPanes with vertical scrolling and no horizontal scrolling, and applied the structure consistently
to Dashboard, Organise, and Gallery. Added a long-review JavaFX regression test; focused and complete Gradle checks
pass.

## Prompt 92 — Roll Back Bounded Review Layout

The user reported that the bounded review-card and ScrollPane change broke the Trip Details layout and requested a
rollback. Removed only that iteration's 35% height binding, ScrollPane structure and styling, shared layout helper,
and long-review regression test. Restored the earlier direct review-label layout and retained the preferred-height fix
for combined Rating-and-Notes reviews; verification is in progress.

## Prompt 93 — Reimplement Bounded Trip Review Layout

The user asked to implement the revised plan for long Trip reviews. The implementation will cap the Organise and
Gallery review cards, place the review text in a vertically scrollable fit-to-width viewport, and reserve enough
height for the Plans heading and at least one Plan cell. The percentage-based layout binding from the rolled-back
iteration will not be reused. Implemented and verified the layout at the 1180×760 application minimum, including a
maximum-length Trip title and long review note; the complete Gradle test and check suite passes.

## Prompt 95 — Guarantee Two-Line Trip Review Viewport

The user confirmed that the 200px maximum did not affect the rendered viewport because the review card was being
compressed to its 76px minimum and its inner ScrollPane was not growing. Increased the Organise and Gallery review
card minimum to 120px, set the ScrollPane minimum to 64px, and allowed it to grow within the card. Added rendered
coverage verifying that combined rating-and-notes reviews fit without clipping and that long reviews still scroll
while preserving the Plans viewport.

## Prompt 96 — Standardize Main Menu Headers

The user requested consistent spacing and sizing for the slogan, menu title, and descriptor across Dashboard,
Organise, and Gallery, following Dashboard's existing header structure. Removed Organise's extra header wrapper and
the “Use Create Trip in the sidebar to start planning.” hint so all three pages use the shared page VBox spacing and
the same eyebrow, page-title, and page-subtitle styles. Added JavaFX coverage for the common header structure.

## Prompt 97 — Hide Empty Trip Review Cards

The user requested hiding the Trip Review card when a Trip has no review, removing the empty-state message, and
restoring Add Review after a review is cleared. Added managed/visible state handling to the Organise and Gallery
controllers, removed the default empty-state text from their FXML labels, cleared stale review text, and added
regression coverage for unreviewed, reviewed, and cleared-review states. Dashboard Plan-review behavior remains
unchanged.

## Prompt 94 — Increase Trip Review Maximum Height

The user reported that the 140px Trip Review maximum clipped the first line of Notes and requested increasing it
to 200px. Increased the Organise and Gallery review-card maximum to 200px while retaining the scrollable review body
and reserved Plan-list viewport, and updated the regression test to protect the new limit.

## Prompt 98 — Make Trip Review Cards Content-Tight

The user requested a style change so Trip Review cards minimize to their content, grow as the review becomes longer,
and show a vertical scrollbar after reaching the existing 200px maximum. Implemented shared adaptive sizing for the
Organise and Gallery review cards: the wrapped review body determines the preferred height, the card and body use
content-sized minimums, and long reviews remain vertically scrollable without horizontal scrolling. Updated rendered
JavaFX regression coverage for compact combined reviews and capped long reviews. The full Gradle test and check suite
passes.

## Prompt 99 — Verify Trip Detail Action Button Heights

The user asked whether Edit trip, Add/Edit Review, and Delete trip have matching heights and requested
standardization if they do not. Added rendered JavaFX coverage for the Organise and Gallery Trip Details action bars.
The existing shared font and vertical padding rules produce equal heights for all three buttons, so no production CSS
change was necessary.

## Prompt 100 — Restyle Trip Review Actions

The user clarified that the requested neutral beige styling applies to the Add Review and Edit Review buttons in
Organise and Gallery. Added a dedicated review-action-button style with the app cream background, darker beige border,
dark text, and hover state while retaining the shared action-button sizing. Added rendered JavaFX assertions for the
style class, computed colors, and equal heights with Edit trip and Delete trip. Focused and complete Gradle checks pass.

## Prompt 101 — Commit Existing Implementation Changes

The user requested committing the existing implementation changes while excluding logs and implementation-plan files.
Staged and committed the JavaFX review-card and review-action styling changes, tests, and durable memory update as
804825e. Left IMPLEMENTATION_PLAN.md and the GUI prompt log unstaged.

## Prompt 102 — Implement Plan Review UI

The user requested implementing the agreed Plan Review UI. Added Plan review actions to Dashboard, compact Plan cells
with rating/status cues and a Details button in Organise and Gallery, and a reusable expandable Plan details inspector
with Edit, Review, and Delete actions. Extended the shared Review dialog to persist, edit, and clear Plan reviews,
updated JavaFX regression coverage, and marked implementation-plan item 12.14 Iteration 2 complete.

## Prompt 103 — Refine Plan Inspector Sizing and Review-Cue Priority

The user asked for a plan to remove the unused vertical space in the Plan details dialog and to ensure compact Plan
cells prioritize review status over long destinations. The agreed implementation keeps the 560px dialog width,
derives its height from visible content, resizes it when Reviews are added or cleared, and reserves the review cue's
width so destinations truncate with ellipses while `X/5 stars` or `Reviewed` remains visible.

## Prompt 104 — Move Plan Review Cues beside Schedule Time

The user proposed moving the compact Plan review status from the destination row to immediately after the time label,
so the destination can regain the full width of its row. Implemented this layout refinement with baseline-aligned date,
time, and review status controls, retained destination ellipses and tooltips, updated the related JavaFX assertions,
and preserved the no-horizontal-scroll behavior.

## Prompt 105 — Clarify Dashboard Empty-State Messages

The user requested replacing the Dashboard detail-pane message with “There are no plans to view.” when no Plans are
available, and changing the Dashboard list descriptor to “Select a plan to see its details.” Updated the Dashboard
controller and FXML, preserved the data-load error message, and added JavaFX regression coverage for both messages.

## Prompt 106 — Commit GUI Plan Review Changes

The user requested committing the implementation changes while excluding `IMPLEMENTATION_PLAN.md` and the GUI log. The
staged set contains the JavaFX Plan review workflows, Dashboard empty-state copy, tests, and memory update; the
implementation plan and log remain unstaged.

## Prompt 107 — Review GUI Structure and Tests

The user requested one review pass over non-CLI production and test code, focusing on package placement,
compartmentalisation, test design, and other notable concerns. The audit found that the architectural package and
resource boundaries are broadly appropriate, but identified a GUI validation defect that prevents date/time-only edits
to restored Plans with unchanged legacy over-limit destinations. It also identified heavy duplication between the
Organise and Gallery controllers, an oversized mixed-responsibility JavaFX test class with gaps around controller
workflows, failures, and keyboard operation, stale GUI status documentation, and an ineffective JavaFX native-access
test argument. No application code was changed. The complete Gradle test suite passed when forcibly rerun, with the
native-access warning noted above.

## Prompt 108 — Commit Remaining Changes

The user requested committing the remaining worktree changes using the SE-EDU Git standard. The remaining changes are
the completed implementation-plan updates and GUI prompt log; they will be reviewed, included in one documentation
commit, and left uncommitted only if verification exposes an issue.
