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
