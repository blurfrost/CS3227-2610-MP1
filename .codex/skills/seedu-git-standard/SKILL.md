---
name: seedu-git-standard
description: Apply the SE-EDU Git conventions to commits and branch names in this project. Use whenever creating a commit, proposing a commit message, or creating or renaming a branch.
---

# SE-EDU Git Standard

Follow the SE-EDU Git conventions at
<https://se-education.org/guides/conventions/git.html> whenever creating a
commit or branch.

## Commit Subjects

- Write a clear subject for every commit.
- Prefer 50 characters or fewer; never exceed 72 characters.
- Use the imperative mood, for example `Add README.md`, not `Added README.md`.
- Capitalize the first letter.
- Do not end the subject with a period.
- Add a relevant scope or category when useful, such as `Parser: Handle empty input`.

## Commit Bodies

- Add a body for every non-trivial commit.
- Separate the subject and body with one blank line.
- Wrap body lines at 72 characters.
- Use blank lines between paragraphs and bullets where helpful.
- Explain what changed and why, not how the implementation works.
- Structure the body in this order:
  1. State the current situation in the present tense.
  2. Explain why it needs to change.
  3. Describe the change in the imperative mood.
  4. Explain why the change is implemented that way.
  5. Add other relevant information.
- Avoid unnecessary words such as `currently` and `originally`.

## Branch Names

- Use meaningful keywords in kebab case, such as `refactor-ui-tests`.
- For issue-related branches, use `issueNumber-keywords-from-issue-title`.

## Commit Workflow

1. Inspect `git status`, `git diff`, and recent history before committing.
2. Stage only files belonging to the intended commit.
3. Review the staged diff and verify the subject and body against this skill.
4. Do not include unrelated worktree changes.
