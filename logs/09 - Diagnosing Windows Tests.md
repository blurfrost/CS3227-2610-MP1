# Diagnosing Windows Test Failures

## Prompt 1 — Diagnose `clean test` Failures on Windows

The user requested diagnosis of failures from running `./gradlew clean test` on Windows. Reproduced two independent
causes without changing Java code: `clean` could not delete `build/libs/doggo.jar` because a running doggo JavaFX
process held the JAR open, while a forced test run completed 421 tests with 11 failures caused by tests hard-coding LF
line endings even though `PrintWriter.println` emits Windows CRLF. Ten failures came from assertions ending in
`Bye!\n`; the remaining failure compared a complete CLI prompt containing `\n`. Also observed that Gradle selected
Oracle JDK 25.0.4.1 rather than the project-specified Zulu 25.0.3, but this toolchain drift did not cause the newline
failures. No fixes were requested or applied; stopping the running app and making the affected assertions
platform-neutral remain deferred.

## Prompt 2 — Implement the Cross-Platform Newline Fix

The user requested implementation of the newline strategy. Added a CLI output writer that normalizes embedded LF and
CRLF content to `System.lineSeparator()` while preserving existing formatter messages and wrapping CLI, prompt, and
context writers. Updated rendered-output assertions to use the host separator. Focused tests, the full 421-test suite,
and `./gradlew clean test` now pass with zero failures. No commit was created.

## Prompt 3 — Diagnose Hanging Ubuntu CI Builds

The user requested diagnosis of Ubuntu CI jobs remaining in Gradle while the macOS and Windows matrix jobs complete.
GitHub Actions metadata and a prior Ubuntu log confirm repository setup, wrapper validation, JDK setup, compilation,
and test compilation all finish before execution stalls in `:test`. The workflow runs the same `./gradlew check` on
all platforms, but Ubuntu has no configured display server while `AppShellFxmlTest` starts the JavaFX toolkit and
creates rendered stages and dialogs. The Linux JavaFX graphics runtime therefore waits during GUI test execution;
running the Linux Gradle command through Xvfb is the recommended workflow correction. No CI or Java code was changed.

## Prompt 4 — Run Ubuntu CI Tests Through Xvfb

The user requested the diagnosed workflow correction. The Ubuntu matrix job now runs `./gradlew check` through an
automatically allocated Xvfb display, while macOS and Windows retain the direct Gradle command. A 15-minute job timeout
was added so a future test deadlock terminates promptly instead of occupying a runner for hours. No Java code changed.

## Prompt 5 — Diagnose the Remaining Ubuntu Test Failure

The user reported one Ubuntu failure from `./gradlew check`. The latest completed GitHub Actions run reaches the Gradle
test task and fails only `AppShellFxmlTest.loadMainViews_longTitles_wrapWithinDetailPanel`; this is not a Checkstyle
failure, and the build does not configure Checkstyle. The failing assertion compares JavaFX rendered bounds to a
calculated panel width with a 0.01px tolerance after one layout pass. It passes locally and on macOS and Windows, so the
remaining issue is a platform-sensitive GUI-test assertion under Linux/Xvfb rather than an application or workflow
failure. No fix was requested or applied.

## Prompt 6 — Inspect the Failing JavaFX Test

The user clarified that the failure was not Checkstyle and requested inspection of the test. The failing test does not
reach its long-title wrapping assertions: it first repeats an equal-panel-width check using `boundsInParent()` with a
0.01px tolerance. Its helper also loads views without the application stylesheet and performs only one layout pass,
whereas nearby rendered-layout tests use the real shell styling and two passes. The test therefore mixes unrelated
behaviors and relies on platform-sensitive JavaFX geometry, producing a Linux/Xvfb false failure. Existing tests already
cover equal panel columns and the production shell's long-title behavior. No fix was requested or applied.

## Prompt 7 — Stabilize the Long-Title JavaFX Test

The user requested implementation of the test correction. Updated the long-title wrapping helper to load the
production stylesheet and complete two layout passes before measuring text. Removed its duplicate, platform-sensitive
panel-width assertions so the test now checks only long-title content and wrapping; equal panel sizing remains covered
by the dedicated tests. The targeted regression and the complete Gradle `check` task pass. No commit was created.
