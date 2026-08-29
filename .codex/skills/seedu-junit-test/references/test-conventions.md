# Test Conventions and Design

These project rules adapt the SE-EDU
[JUnit conventions](https://se-education.org/guides/tutorials/junit.html#conventions-to-follow)
and the CS2103/T textbook's
[testing types](https://nus-cs2103-ay2526s1.github.io/website/se-book-adapted/chapters/testing.html#testing-types)
to doggo's Gradle and JUnit Jupiter test suite.

## Location and Class Naming

- Put Java test code under `src/test/java`.
- Mirror the production package and path. For example,
  `src/main/java/doggo/domain/Trip.java` is tested by
  `src/test/java/doggo/domain/TripTest.java` in package `doggo.domain`.
- Name a unit-test class after its production class with the `Test` suffix.
- Place a test in the same package as its subject so package-private behavior
  can be tested where appropriate without widening production visibility.
- Tests spanning several production classes may use a cohesive behavior or
  boundary name ending in `Test`, such as `GalleryMaintenanceTest`. Keep such
  tests in the package that owns the tested public boundary.
- Place reusable test fixtures at the narrowest shared test package. A helper
  is not a test class and need not end in `Test`.

## Test Method Naming

Use `methodOrOperation_scenario_expectedBehavior` when all three parts add
information, for example:

```java
void save_existingTrip_replacesAggregate()
```

- Name the operation as the caller sees it, not after private implementation.
- State the distinguishing input, state, or boundary in the scenario.
- State one observable outcome. Avoid vague endings such as `works` or
  `testSuccessful`.
- A shorter name is acceptable when the scenario or result would only repeat
  the operation, but keep naming consistent within a test class.

## Selecting Test Types

### Unit tests

Use unit tests for individual domain values, classes, methods, parsers, or
commands in isolation.

- Cover each public behavior with relevant normal, boundary, and invalid
  cases; do not generate one test mechanically for every method.
- Replace external or complex dependencies with a simple stub, fake, or test
  implementation when dependency behavior is not under test.
- Prefer doggo's injected `Clock`, in-memory repositories, and in-memory I/O
  over time, database, filesystem, or console globals.
- Assert observable results and state. Avoid testing private methods or the
  exact sequence of internal calls unless that interaction is the contract.

### Integration tests

Use integration tests to verify that real components agree at their boundary,
especially application-to-repository mapping and transaction behavior.

- Exercise the real collaborators whose glue code is under test.
- For SQLite, use a fresh temporary database per test and verify round trips,
  constraints, updates, deletion, transaction rollback, and reopening.
- Do not call a test a unit test if it depends on the real database adapter.
- A hybrid test using small, already-tested in-process collaborators is
  reasonable when stubbing would obscure the behavior; keep slower external
  infrastructure out unless it is the target.

### System and acceptance tests

Use system tests for the complete application's externally specified behavior
and acceptance tests for user requirements and use cases.

- Drive the CLI through its public input/output boundary rather than invoking
  command internals.
- Include positive workflows and relevant negative cases that prove graceful
  failure. Acceptance coverage should emphasize representative user outcomes,
  not duplicate every unit-level validation case.
- Keep expected output deterministic. Inject time and control persistence so
  results do not depend on the machine or execution date.

### Regression tests

Add an automated regression test when fixing a defect or protecting behavior
that could be broken by the current change.

- Reproduce the failure with the smallest appropriate test level.
- Name the user-visible scenario, not an issue number alone.
- Retain related existing tests and run them frequently after small changes.

Exploratory testing can reveal scenarios worth automating, but it does not
replace the scripted regression suite. Record a discovered defect as a focused
automated test when reproducible.

## Curating Cases

- Derive expected behavior from requirements and public contracts before
  reading implementation details.
- Partition inputs into meaningfully different valid and invalid classes, then
  select representatives rather than testing arbitrary permutations.
- Test inclusive and exclusive boundaries explicitly where dates, times,
  indexes, ratings, or ranges change behavior.
- For a state transition, verify both the returned result and durable stored
  state when each is part of the contract. On rejected or failed mutations,
  verify prior state remains intact.
- Keep one primary behavior per test. Multiple assertions are appropriate when
  they describe one outcome or aggregate state.
- Follow Arrange–Act–Assert structure in substance. Add comments only when the
  phases or reason for a non-obvious fixture are unclear.
- Make every test independent and repeatable: no ordering assumptions, shared
  mutable state, real current time, production database, network, or user home
  data.
- Prefer concise builders or fixtures when setup obscures the behavior, but do
  not hide the important inputs that distinguish the case.
- Remove or consolidate duplicate tests only after confirming they exercise
  the same behavior at the same test level.
