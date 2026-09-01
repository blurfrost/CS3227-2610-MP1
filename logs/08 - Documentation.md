## Prompt 1 — Create the User Guide

The user requested a complete `UserGuide.md` covering cross-platform setup, Java dependencies, GitHub JAR download and
launch methods, interface menus, all Trip and Plan workflows, and frequently asked questions. Created the guide at
`docs/UserGuide.md`, documenting the current JavaFX UI, bundled runtime dependencies, working-directory database path,
validation rules, status-based navigation, review behavior, deletion consequences, and troubleshooting guidance.

## Prompt 2 — Update the Developer Guide

The user requested a current `DeveloperGuide.md` containing a setup guide,
architecture and design explanation, UML/component/sequence/activity diagrams,
target-user and value-proposition requirements, prioritized user stories, use
cases with Main Success Stories and Extensions, non-functional requirements,
a glossary, and manual-testing instructions for launch/shutdown, Trip
creation, Plan creation, and saving data. Updated `docs/DeveloperGuide.md` to
match the implemented JavaFX and CLI system: it documents Java 25.0.3.fx-zulu,
JavaFX 25.0.3, SQLite JDBC, the Gradle wrapper, the layered architecture with
repository dependency inversion, current domain rules, status-aware routing,
and eager persistence. Added Mermaid component, class, startup, Trip/Plan
mutation sequence, and activity diagrams. The guide marks photo attachments,
Plan copying, and search/filtering as deferred extensions. No Java production or
test code was changed; verification and the HEAD-versus-WORKTREE visual diff
remain to be completed.

## Prompt 3 — Fix the Developer Guide Component Diagram

The user reported a Mermaid parse error in the Developer Guide component
diagram because class-diagram relationship operators (`*--`, `o--`, and
`..|>`) had been used inside a `flowchart`. Replaced those relationships with
valid flowchart arrows and labels for ownership, optional reviews, derived
status, and repository implementation. The class diagram retains the original
UML relationship notation. No Java code was changed; the corrected block was
checked and the HEAD-versus-WORKTREE visual diff was regenerated successfully.
