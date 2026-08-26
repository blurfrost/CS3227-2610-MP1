# Project Setup

The project will become a simple Java desktop application for planning and journalling travels. It will use Java 25.0.3.fx-zulu and JavaFX for the graphical user interface, and will be packaged as a JAR using `./gradlew clean shadowJar`.

For this initial setup, create a `logs/` directory for Markdown files containing project prompts and create an initially blank `AGENTS.md` file. This prompt is recorded as the first log entry.

## Prompt 2 — Implement AGENTS.md Standards

Implement the proposed addition to `AGENTS.md`: document the project’s Java version as Java 25.0.3.fx-zulu, specify JavaFX 26.0.1 as the compatible version, and add a post-prompt checklist requiring each user prompt to be summarized in the latest logs Markdown file.

## Prompt 3 — Add Project Context

Add project context to `AGENTS.md`, including the default assumption that assistance is being provided to a student, role adaptation for instructors or other stakeholders, and the student’s experience, Java and JavaFX background, and use of Visual Studio Code.

## Prompt 4 — Set Up Repository Memories

Set up a Codex-discoverable repository memory file. Use a root-level `MEMORY.md` for durable project context, instruct agents in `AGENTS.md` to read and maintain it, seed it with the project’s current toolchain and development context, and keep transient details in the prompt logs.

## Prompt 5 — Capture Requirements

Create a root-level `DeveloperGuide.md` containing doggo's agreed functional and non-functional user stories, product terminology, domain rules, architectural constraints, and acceptance scenarios. Add the durable doggo product model and terminology to `MEMORY.md`, and summarize this documentation request in the current log.

## Prompt 6 — Add Project-Local Codex Skills

Copy the `present-changes-visually`, `seedu-git-standard`, and `seedu-java-coding-standard` skill packages into the project root under `.codex/skills/` so they are accessible to the Codex harness. Preserve each package's instructions, metadata, and bundled resources.

## Prompt 7 — Extend the Post-Prompt Workflow

Update `AGENTS.md` so that changed Java code is reviewed with the `/seedu-java-coding-standard` skill and all completed changes are presented using `/present-changes-visually` as a `HEAD` versus `WORKTREE` visual diff.

## Prompt 8 — Add Project Git Ignore Rules

Create a root-level `.gitignore` for Java and Gradle build output, IDE metadata, local environments, doggo runtime data, Codex review artifacts, Python cache files, and operating-system files. Keep project documentation, logs, source files, tests, resources, the Gradle wrapper, and local Codex skills trackable.
