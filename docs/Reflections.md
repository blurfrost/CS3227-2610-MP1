# Reflection for Mini Project 1

## How I Initially Approached the Project

I began doggo with a broad product idea: a local-first Java desktop application for planning and journalling travels. Although I already had experience with Java, object-oriented programming, and JavaFX, the project still involved many decisions beyond writing code. I had to define the product vocabulary, decide how Trips and Plans related to each other, choose an architecture, establish persistence rules, design both CLI and GUI interactions, and keep the growing codebase maintainable.

My initial approach was therefore documentation-first and iterative. I used AI to help create skeletons of the project standards, Developer Guide, memory file, implementation plan, and prompt logs before attempting the complete application. This gave each later conversation a stable source of truth. In particular, `MEMORY.md` stored durable facts, the logs preserved how decisions were reached, and `IMPLEMENTATION_PLAN.md` tracked the next executable slices. These files reduced the risk of a long AI-assisted project losing its direction between sessions.

The early planning also established a layered design: 
1. Domain objects for Trips, Plans, and Reviews
2. An application service for use cases; repository interfaces and implementations for persistence
3. Separate presentation packages for the CLI and JavaFX. 

AI was useful here as a design partner because it could compare alternatives and identify consequences that were easy to overlook. For example, an architecture review highlighted the danger of mutating a Trip before a save succeeded, stale list-number mappings, and invalid navigation state. This led to copy-on-write aggregates, UUID-backed display snapshots, and explicit session transitions before those problems became embedded throughout the application.

However, I also learnt very early that a detailed prompt does not guarantee the intended scope. During the first CLI feature, the AI implemented Plan-related groundwork even though I wanted to stop after Trip creation. I recorded this directly in the log as the output being “a bit too overzealous” and asked it to remove the unreachable groundwork. This was an important early lesson: iterative delivery only works when I ensure that my prompt remains narrow in scope.

## Implementing the CLI to Discover What I Wanted

I initially planned doggo as a JavaFX application, but implementing a chatbot-style CLI first was one of the most valuable decisions in the project. The CLI made behavior cheap to build, run, test, and revise. Without having to settle layouts or visual styling, I could focus on the domain model and the meaning of each operation.

One of my earliest substantial prompts described an entire interaction, including commands such as `organise`, `new`, `view NUMBER`, and `back`, along with the expected terminal output. An excerpt was:

> “Let's plan a simple CLI implementation based on the simple architecture previously discussed, focusing on building up new features iteratively.”

[The full prompt can be viewed here](<../logs/03 - Bare CLI Implementation.md>)

This prompt was interesting because it used a concrete conversation rather than only a list of requirements. It helped expose details such as whether `back` should be treated as a command while a form field was being entered, how dates and times should be parsed, when menus should refresh, and whether list indices should remain stable. Some of the eventual rules were not obvious from the original product idea; they emerged by interacting with the CLI and noticing awkward cases.

The CLI also helped me decide what doggo itself should be. Dashboard evolved into a current-day, cross-Trip itinerary. Organise became the home for current and future Trips. Gallery became both an archive and a place where past Trips could still be maintained. Review behavior was reconsidered after implementation: instead of restricting reviews to completed Trips and Plans, I decided that reviews should behave as user-owned annotations that could be added at any time. Because this logic was already separated from the presentation layer, I could change the service rules and CLI behavior before bringing the same policy into JavaFX.

Another useful prompt was:

> “Read `AGENTS.md` and `DeveloperGuide.md`, inspect the current implementation, and review its overall architecture without modifying the Java code.”

This showed that AI assistance did not always need to produce code. Asking for a read-only review created space to examine failure handling, persistence boundaries, identity, and navigation before deciding what to change. It also prevented an architectural discussion from turning immediately into a large, difficult-to-review patch.

By the time I moved beyond the CLI, the application already had validated domain objects, copy-on-write updates, deterministic Clock-based queries, a repository abstraction, comprehensive command behavior, and SQLite persistence. The CLI was therefore not discarded prototyping work. It was a functional first client of the application layer and a way to make the product requirements concrete.

## Working Towards the GUI

The JavaFX transition was deliberately incremental. I first configured the cross-platform build and composition root, then built the FXML application shell, followed by a read-only Dashboard. The Organise and Gallery menus began as placeholders and then became read-only views before creation, editing, deletion, and review actions were added. This order let me verify the application structure and data flow before adding complicated forms.

The GUI phase also changed the type of feedback I needed to give the AI. CLI behavior could often be specified through text input and output, while GUI work required visual judgement. I repeatedly reviewed the running application and reported concrete issues: ambiguous dates without years, a Dashboard that did not refresh, long names being clipped, unexpected horizontal scrollbars, confirmation-dialog styles being overridden, and review text being compressed to one line. These observations produced narrow regression fixes rather than vague requests to “improve the UI.”

For example, I asked:

> “Reconsider the compact-card viewport sizing because Organise still displays a horizontal scrollbar alongside the vertical scrollbar.”

An earlier solution that I prompted Codex with had subtracted a fixed margin from the width of one of the container elements I used to store a list of Trips. Using Codex's helpful image uploading feature, my screenshot and follow-up caused the implementation to account for the rendered vertical scrollbar instead. This was a useful reminder that a plausible code-level solution is not necessarily correct in the rendered interface. The GUI had to be inspected at realistic window sizes and with enough data to trigger JavaFX's virtualized scrolling behavior.

The same pattern appeared in the Trip Review layout. One percentage-based bounded layout technically met the initial request but damaged the rest of the Trip details view, so I had to roll back my changes. The feature was then reimplemented with a capped, content-aware card that reserved space for Plans. Treating rollback as a normal engineering action made it easier to experiment without defending an unsuitable AI-generated approach.

The GUI also demonstrated the benefit of the CLI-first architecture. JavaFX controllers and dialogs could call the same `DoggoService` operations rather than duplicating business rules. Trips created or edited in one view were routed according to their status, Plan reviews behaved consistently across Dashboard, Organise, and Gallery, and SQLite persistence required no GUI-specific redesign. Most GUI iterations were therefore about presentation state, validation feedback, selection, and layout rather than rebuilding the application core.

## Ensuring Code Quality Through Skills and a Consistent Workflow

AI made implementation faster, but the consistency of the workflow made the output trustworthy. I introduced project-local skills for the SE-EDU Java coding standard, JUnit test conventions, Git conventions, and visual change presentation. These skills converted broad expectations into repeatable checks that could be applied even across long sessions and different implementation tasks. Maintaining an AGENTS.md file was useful in ensuring that coding agents across different sessions and contexts still refer to this source of truth and perform essential checks after each prompt.

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

The project benefited from matching different levels of AI agents to different scopes of work. I mainly used GPT-5.6 Sol as the parent agent for planning, architectural reviews, and decisions that crossed several layers of the application. These tasks required a broad view of the repository: understanding the requirements, comparing the implementation with `IMPLEMENTATION_PLAN.md`, identifying dependencies between milestones, and deciding how a large feature should be divided into safe iterations. Sol was especially useful before work such as Dashboard, Gallery, reviews, SQLite persistence, and JavaFX because it could turn a broad goal into an ordered plan with explicit boundaries and acceptance criteria.

Once a plan was sufficiently detailed, I mainly used GPT-5.6 Luna to implement one small part at a time. Instead of asking a lower-level implementation agent to interpret an entire milestone independently, I gave it a bounded task whose behavior, affected layers, and verification expectations had already been established. For example, SQLite persistence was divided into repository-failure handling, restoration factories, schema initialization, aggregate reads, transactional writes, and production wiring. This kept each change reviewable and reduced the chance that one agent would make several hidden design decisions while producing a very large patch.

The Review milestone used this structure directly. GPT-5.6 Luna handled individual implementation iterations at high reasoning effort, while I reviewed how each result fitted the larger application using GPT-5.6 Sol and reran the tests before approving it. The parent review caught issues such as stale Dashboard membership handling that were easy to miss when concentrating on only one operation. For some SQLite iterations, a Luna agent at high reasoning effort implemented the code, while a Luna agent at medium reasoning effort handled the narrower Git workflow after the implementation had been reviewed. This showed me that model selection was not simply about always choosing the most capable model; it was about using greater reasoning scope where architectural judgement was needed and a faster model where the task was already well specified.

This agent hierarchy also made the handoff between planning and implementation explicit. A strong plan reduced ambiguity for Luna, while the implementation result gave Sol concrete code and test evidence to review. If the implementation exposed a flaw in the plan, the work returned to the parent level for reconsideration rather than allowing the small implementation task to quietly redefine the architecture. This was particularly important in a long-running project where a local change could affect CLI behavior, JavaFX state, SQLite persistence, tests, and documentation at the same time.

The agents were still supported by narrower verification tools. Gradle tests, SQLite integration tests, JavaFX smoke and rendered-layout tests, coding and Git skills, prompt logs, and visual diffs provided evidence that a handoff had succeeded. These checks mattered because using different model levels would otherwise only distribute the work, not make it dependable.

My main takeaway is that effective AI-assisted software engineering is less about finding one model that can build everything and more about designing a reliable hierarchy of responsibility. GPT-5.6 Sol helped me reason about direction and integration, while GPT-5.6 Luna efficiently carried out smaller steps derived from those plans. I still supplied product judgement, scope control, visual evaluation, and final accountability. doggo became stronger because every delegated task remained inspectable, testable, and open to revision.
