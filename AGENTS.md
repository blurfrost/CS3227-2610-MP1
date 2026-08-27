# Project Standards

## Project Context

You are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

## Student Profile

- Years of experience: 4
- Prior relevant knowledge: Knows how to code in Java and related OOP concepts, and has completed projects in JavaFX.
- IDE used in this project: Visual Studio Code.

## Memory File

Read the repository-root `MEMORY.md` at the beginning of each task. Add only concise, durable, project-relevant context to it, and avoid duplicating transient task details that belong in `logs/`.

## Java Version

Use Java 25.0.3.fx-zulu for this project.

## JavaFX Version

Use JavaFX 26.0.1. This is the latest JavaFX release currently documented and is compatible with Java 25; JavaFX 26 requires JDK 24 or later. See the [OpenJFX JavaFX 26 highlights](https://openjfx.io/highlights/26/).

## Post-Prompt Checklist

After every user prompt:

- Summarize this current prompt and add it to the latest `logs/` Markdown file created. For now, this file is `04 - Organise Java Files into Packages.md`.
- Write prompt summaries so they preserve the user's original intent and explain the AI model's response, including decisions, outcomes, and any deferred work. Do not add unrelated feature details merely to make a summary longer.
- If Java production or test code was written or modified, use the `/seedu-java-coding-standard` skill to review the changed Java code and fix applicable coding-standard violations.
- Do not stage or commit changes unless the user explicitly instructs you to do so. Leave completed changes in the worktree so `/present-changes-visually` can compare the current Git `HEAD` against `WORKTREE`.
- Whenever the user explicitly instructs you to commit changes, use the `/seedu-git-standard` skill to guide the commit workflow and commit message. For commits that only change files under `logs/`, use a concise subject and body without enumerating the logged prompt content.
- Once all requested changes and follow-up fixes are complete and ready for review, use the `/present-changes-visually` skill to generate a visual diff between the current Git `HEAD` and `WORKTREE`. Include staged, unstaged, and untracked files, write the result to `_temp/visual-diff.html` unless another output path was requested, and report the generated file path.
