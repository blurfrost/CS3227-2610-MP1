---
name: seedu-junit-test
description: Apply this project's SE-EDU JUnit conventions when designing, creating, reorganizing, or reviewing Java tests. Use for test naming, placement, scope, test-type selection, and coverage curation; do not use for non-Java tests.
---

# SE-EDU JUnit Test Standard

Curate tests as a deliberate suite rather than appending cases mechanically.

Before changing or reviewing tests, read
[references/test-conventions.md](references/test-conventions.md) completely.
Apply it together with the project's Java coding standard.

## Workflow

1. Identify the behavior, owning production boundary, and appropriate test type.
2. Inspect nearby tests before choosing the test class and package. Reuse an
   existing matching class when it remains cohesive.
3. Design cases from requirements and observable behavior, including relevant
   normal, boundary, invalid, and regression scenarios.
4. Keep unit tests isolated and deterministic. Use real collaborating
   components only when their integration is the behavior under test.
5. Name and locate classes and methods using the reference conventions.
6. Run the narrowest relevant Gradle tests while iterating, then run the full
   suite before completion when practical.
7. Review the resulting suite for duplicated scenarios, implementation-coupled
   assertions, hidden order dependencies, and misleading test-type placement.

Do not rename or relocate unrelated existing tests merely to enforce the
standard. When a requested change exposes a nonconforming nearby test, report
it or fix it only when doing so keeps the change focused.
