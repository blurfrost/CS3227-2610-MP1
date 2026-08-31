# Reflection for Mini Project 1

## How I Initially Approached the Project

I began doggo with a broad product idea: a local-first Java desktop application for planning and journalling travels. Although I already had experience with Java, object-oriented programming, and JavaFX, the project still involved many decisions beyond writing code. I had to define the product vocabulary, decide how Trips and Plans related to each other, choose an architecture, establish persistence rules, design both CLI and GUI interactions, and keep the growing codebase maintainable.

My initial approach was therefore documentation-first and iterative. I used AI to help create the project standards, Developer Guide, memory file, implementation plan, and prompt logs before attempting the complete application. This gave each later conversation a stable source of truth. In particular, `MEMORY.md` stored durable facts, the logs preserved how decisions were reached, and `IMPLEMENTATION_PLAN.md` tracked the next executable slices. These files reduced the risk of a long AI-assisted project losing its direction between sessions.

The early planning also established a layered design: domain objects for Trips, Plans, and Reviews; an application service for use cases; repository interfaces and implementations for persistence; and separate presentation packages for the CLI and JavaFX. AI was useful here as a design partner because it could compare alternatives and identify consequences that were easy to overlook. For example, an architecture review highlighted the danger of mutating a repository-owned Trip before a save succeeded, stale list-number mappings, and invalid navigation state. This led to copy-on-write aggregates, UUID-backed display snapshots, and explicit session transitions before those problems became embedded throughout the application.

However, I also learnt very early that a detailed prompt does not guarantee the intended scope. During the first CLI feature, the AI implemented Plan-related groundwork even though I wanted to stop after Trip creation. I recorded this directly in the log as the output being “a bit too overzealous” and asked it to remove the unreachable groundwork. This was an important early lesson: iterative delivery only works when I actively check that each iteration is actually narrow.

## Implementing the CLI to Discover What I Wanted

I initially planned doggo as a JavaFX application, but implementing a chatbot-style CLI first was one of the most valuable decisions in the project. The CLI made behavior cheap to build, run, test, and revise. Without having to settle layouts or visual styling, I could focus on the domain model and the meaning of each operation.

One of my earliest substantial prompts described an entire interaction, including commands such as `organise`, `new`, `view NUMBER`, and `back`, along with the expected terminal output. An excerpt was:

> “Let's plan a simple CLI implementation based on the simple architecture previously discussed, focusing on building up new features iteratively.”

This prompt was interesting because it used a concrete conversation rather than only a list of requirements. It helped expose details such as whether `back` should be treated as a command while a form field was being entered, how dates and times should be parsed, when menus should refresh, and whether list indices should remain stable. Some of the eventual rules were not obvious from the original product idea; they emerged by interacting with the CLI and noticing awkward cases.

The CLI also helped me decide what doggo itself should be. Dashboard evolved into a current-day, cross-Trip itinerary. Organise became the home for current and future Trips. Gallery became both an archive and a place where past Trips could still be maintained. Review behavior was reconsidered after implementation: instead of restricting reviews to completed Trips and Plans, I decided that reviews should behave as user-owned annotations that could be added at any time. Because this logic was already separated from the presentation layer, I could change the service rules and CLI behavior before bringing the same policy into JavaFX.

Another useful prompt was:

> “Read `AGENTS.md` and `DeveloperGuide.md`, inspect the current implementation, and review its overall architecture without modifying the Java code.”

This showed that AI assistance did not always need to produce code. Asking for a read-only review created space to examine failure handling, persistence boundaries, identity, and navigation before deciding what to change. It also prevented an architectural discussion from turning immediately into a large, difficult-to-review patch.

By the time I moved beyond the CLI, the application already had validated domain objects, copy-on-write updates, deterministic Clock-based queries, a repository abstraction, comprehensive command behavior, and SQLite persistence. The CLI was therefore not discarded prototyping work. It was a functional first client of the application layer and a way to make the product requirements concrete.

## Working Towards the GUI

The JavaFX transition was deliberately incremental. I first configured the cross-platform build and composition root, then built the FXML application shell, followed by a read-only Dashboard. Organise and Gallery began as placeholders and then became read-only master-detail views before creation, editing, deletion, and review actions were added. This order let me verify the application structure and data flow before adding complicated forms.

The GUI phase also changed the type of feedback I needed to give the AI. CLI behavior could often be specified through text input and output, while GUI work required visual judgement. I repeatedly reviewed the running application and reported concrete issues: ambiguous dates without years, a Dashboard that did not refresh, long names being clipped, unexpected horizontal scrollbars, confirmation-dialog styles being overridden, and review text being compressed to one line. These observations produced narrow regression fixes rather than vague requests to “improve the UI.”

For example, I asked:

> “Reconsider the compact-card viewport sizing because Organise still displays a horizontal scrollbar alongside the vertical scrollbar.”

The earlier solution had subtracted a fixed margin from the ListView width. My screenshot and follow-up caused the implementation to account for the rendered vertical scrollbar instead. This was a useful reminder that a plausible code-level solution is not necessarily correct in the rendered interface. The GUI had to be inspected at realistic window sizes and with enough data to trigger JavaFX's virtualized scrolling behavior.

The same pattern appeared in the Trip Review layout. One percentage-based bounded layout technically met the initial request but damaged the rest of the details view, so I explicitly rolled it back. The feature was then reimplemented with a capped, content-aware card that reserved space for Plans. Treating rollback as a normal engineering action made it easier to experiment without defending an unsuitable AI-generated approach.

The GUI also demonstrated the benefit of the CLI-first architecture. JavaFX controllers and dialogs could call the same `DoggoService` operations rather than duplicating business rules. Trips created or edited in one view were routed according to their status, Plan reviews behaved consistently across Dashboard, Organise, and Gallery, and SQLite persistence required no GUI-specific redesign. Most GUI iterations were therefore about presentation state, validation feedback, selection, and layout rather than rebuilding the application core.

## Ensuring Code Quality Through Skills and a Consistent Workflow

AI made implementation faster, but the consistency of the workflow made the output trustworthy. I introduced project-local skills for the SE-EDU Java coding standard, JUnit test conventions, Git conventions, and visual change presentation. These skills converted broad expectations into repeatable checks that could be applied even across long sessions and different implementation tasks.

The Java coding skill was used after production or test Java changes to check matters such as naming, visibility, documentation, and formatting. The JUnit skill helped reorganize a broad test suite package by package. Rather than maximizing the test count, I reviewed whether each test belonged at the domain, application, storage, command, parser, GUI, integration, or acceptance level. This led to focused test classes while retaining representative end-to-end workflows. Deterministic `Clock` values, in-memory repositories, temporary SQLite databases, and JavaFX toolkit setup made the tests more reliable.

The Git skill encouraged small, imperative commits grouped by intent, while the workflow prohibited automatic staging or committing. This separation was important: the AI could implement and verify a slice, but I retained control over when it became repository history. The visual-diff skill then presented `HEAD` against the entire worktree, including unstaged and untracked files, which provided a final review surface beyond terminal summaries.

The recurring workflow became:

1. Read the project memory, requirements, and active implementation plan.
2. Agree on a small iteration and record its intended scope.
3. Implement the slice without automatically committing it.
4. Review the Java and tests against the relevant skills.
5. Run focused tests, the complete Gradle suite, build checks, and formatting checks as appropriate.
6. Inspect the running GUI when visual behavior was involved.
7. Generate a visual diff and commit only when explicitly requested.
8. Update the prompt log so the reasoning and outcome were not lost.

This process was more valuable than any single generated class. It made AI assistance reproducible and reviewable, and it allowed later agents to understand not only the current code but also why it had reached that state.

## Why I Could Not Rely Only on AI Output

Throughout the project, I treated AI output as a proposal that still needed engineering judgement. The model was strong at quickly producing scaffolding, identifying edge cases, refactoring repeated logic, and generating tests. It was less reliable at knowing when a technically reasonable choice did not match my intended scope or user experience.

Several examples made this clear. The first CLI implementation exceeded the requested feature slice. A JavaFX runtime change intended to address font rendering introduced classifier considerations and was later restored. A bounded review layout had to be rolled back because it harmed the overall screen. Fixed-width safety margins appeared to solve card overflow in tests but failed when a real vertical scrollbar was present. A GUI structure review also found a validation defect involving restored legacy over-limit names despite the full test suite passing. In each case, the AI's work was useful, but acceptance required reading the diff, running the software, examining edge cases, and sometimes disagreeing with the first solution.

I also had to make the product decisions. The AI could explain the tradeoffs of overlapping Trips, review eligibility, navigation after creation, name limits, or database location, but it could not decide what experience I wanted doggo to provide. My follow-up prompts often clarified these choices—for example, requiring blank creation fields to reprompt while blank edit fields preserve their existing values. Precise feedback turned an almost-correct implementation into behavior that matched my intent.

This changed how I wrote prompts. I became more explicit about whether I wanted planning, diagnosis, implementation, review, rollback, or a commit. I named files that should remain uncommitted, asked the AI to stop between iterations, and described observable behavior rather than only internal code changes. I also learnt to ask for evidence: focused tests, the complete test suite, rendered JavaFX checks, packaged-JAR smoke tests, and visual diffs.

## Using Different Tools for Different Scopes

The project benefited from matching the tool to the size and nature of the task. For broad design work, I used conversational planning and read-only architecture reviews. For bounded implementation, I used the coding agent with repository context and an explicit checklist. For repetitive but separable milestones such as reviews and SQLite persistence, I experimented with specialized subagents and different model settings while retaining a parent review step.

One prompt that captured this was:

> “Implement each Review iteration with a `gpt-5.6-luna` xhigh subagent, parent-agent review and testing, and the same subagent performing the SE-EDU-standard commit after approval.”

This division of work was interesting because model delegation was not treated as automatic trust. A subagent could focus deeply on one bounded iteration, while the parent checked integration, ran tests, and caught issues such as stale Dashboard membership handling. A separate, lower-scope pass could then apply the Git convention after the code had been approved. The same general pattern was later used for SQLite iterations, whose schema, reading, transactional writing, and production wiring were separated so each risk could be reviewed independently.

Other tools served narrower purposes. Gradle provided repeatable unit, integration, JavaFX, and packaging verification. The CLI enabled fast behavioral exploration. SQLite integration tests proved persistence and rollback across repository restarts. JavaFX smoke and rendered-layout tests protected FXML loading and important geometry, but manual visual inspection remained necessary. Skills supplied consistent coding, testing, Git, and review rules. `MEMORY.md`, the implementation plan, and prompt logs acted as different forms of project memory rather than forcing one document to serve every purpose.

My main takeaway is that effective AI-assisted software engineering is less about finding one model that can build everything and more about designing a reliable collaboration system. The AI accelerated implementation and expanded the range of alternatives I could consider, while I supplied product judgement, scope control, visual evaluation, and final accountability. doggo became stronger because the project combined both: AI was used extensively, but every important decision remained inspectable, testable, and open to revision.
