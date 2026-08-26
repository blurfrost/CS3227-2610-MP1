# Bare CLI Implementation

## Prompt 1 — Plan Trip and Plan Creation

Plan a simple chatbot-style CLI in iterative feature sets. Support creating and listing Trips through Organise, selecting a Trip, and creating and listing Plans. Trips contain a title and inclusive dates; Plans contain a destination, date, and time. Dates must use `DD/MM/YYYY`, times must use `HH:mm`, and invalid logical ranges should be rejected.

## Prompt 2 — Implement Feature Set 1

Implement the confirmed administrative changes and the first reviewable feature set: the Java 25 Gradle application scaffold, chatbot shell, strict input parsing, in-memory Trip domain and repository, Trip creation, and Trip listing. Stop for review before implementing Plan creation.

## Prompt 3 — Refine Feature Set 1 Scope

(Personal note: I found the output generated to a bit too overzealous as it came up with additional code based on the specification discussed earlier. It might be my fault though as the example I provided was pretty long and involved both the feature sets of creating a new Trip and creating a new Plan within a Trip)
Fix Trip creation input so `back` is saved as a title and treated as an invalid date that shows an error and reprompts. Remove Plan-related groundwork and all currently unreachable methods, retain the command pattern, and remove redundant imports. Defer broader test-case discussion to the next prompt.

## Original Prompt 2 — Bare CLI Implementation Specification

```text
All plans and prompt logs now should be stored in logs/03 - Bare CLI Implementation.md
Let's plan a simple CLI implementation based on the simple architecture previously discussed, focusing on building up new features
iteratively:
1. Creating a new Trip using the "organise" command. Specify its title, its start date and end date. (From `DeveloperGuide.md`, you
may edit User Story 2 to remove the destination).
Trip dates should ONLY accept the DD/MM/YYYY format for now.
2. In an existing Trip, create a new Plan. A Plan should specify its destination, date and time.
Plan dates should ONLY accept the DD/MM/YYYY format for now.
Plan times should ONLY accept the HH:mm format for now.
3. View all existing Trip(s) using the "organise" command.

For this CLI implementation, you can create a simple chatbot interface.

Example: (all inputs are prepended with a >, for example "> organise" represents that "organise" was typed)
---doggo v0.1---
Welcome! Available commands are: "organise", "exit"

> organise
---
[MODE: ORGANISE]
There are no Trips planned.
Type "new" to create a new Trip.
Type "back" to go back to the Main Menu.

> new
---
Enter trip name:

> Japan trip
---
Enter trip start date:

> 01/01/2027
---
Enter trip end date:

> 09/01/2027
---
Trip successfully added!
Here are your trips:
1. Japan trip (from 01/01/2027 to 09/01/2027)

View a trip by entering its index number.
Type "new" to create a new Trip.
Type "back" to go back to the Main Menu.

> 1
---
Viewing: Japan trip (from 01/01/2027 to 09/01/2027)
There are no plans!

Type "new" to create a new Plan.
Type "back" to go back to the Organise Menu.

> new
---
Enter plan destination:

> Mount Fuji
---
Enter plan date:

> 05/01/2027
---
Enter plan time:

> 09:00
---
Plan created!
Viewing: Japan trip (from 01/01/2027 to 09/01/2027)
Plans:
1. Mount Fuji (05/01/2027 at 09:00)

Type "new" to create a new Plan.
Type "back" to go back to the Organise Menu.

> back
---
Here are your trips:
1. Japan trip (from 01/01/2027 to 09/01/2027)

View a trip by entering its index number.
Type "new" to create a new Trip.
Type "back" to go back to the Main Menu.

> back
---
[MODE: MAIN MENU]
Welcome! Available commands are: "organise", "exit"

> exit
---
Bye!
(program closes)
```

## Prompt 4 — Preserve Original Prompt 2

Add the complete original Feature Set 1 prompt, including its chatbot interaction example, to this log for reference.

## Prompt 5 — Evaluate Trip Overlaps and Date Tests

Evaluate whether Trips should be allowed to have overlapping date ranges and whether strict date parsing needs additional representative invalid-date tests beyond relying on `DateTimeFormatter` and `DateTimeParseException`.

## Prompt 6 — Group and Commit Uncommitted Changes

Bundle the uncommitted project changes into sensible groups of local commits using the `seedu-git-standard` skill. Propose the grouping and commit messages before implementing them.

## Prompt 7 — Correct Documentation Commit Message Spacing

Correct the spacing and formatting of the documentation commit message, using the explicitly supplied `git commit -m` command and message text.

## Prompt 8 — Implement Feature Set 2

Implement Feature Set 2: select a Trip by its displayed index, view its Plans, and create Plans with a destination, strict `DD/MM/YYYY` date, and strict `HH:mm` time. Require Plan dates to stay within the selected Trip's inclusive range, display Plans chronologically, preserve `back` as a destination, and treat `back` as invalid for date and time prompts. Retain the command pattern, add representative tests, and leave changes uncommitted for review.

## Prompt 9 — Review Feature Set 2

Review the Feature Set 2 implementation, focusing on methods or return values that are not used and whether `TripTest` tests Trip and Plan as appropriate individual units.

## Prompt 10 — Apply Feature Set 2 Review Improvements

Move chronological Plan ordering into `DoggoService`, add standalone Plan unit tests, add an explicit selected-Trip clearing method, and guarantee `HH:mm` input and display behavior. Keep created-object return values and other currently used methods unchanged.

## Prompt 11 — Commit Feature Set 2 Changes

Group the Feature Set 2 changes into logical local commits using the `seedu-git-standard` skill. Propose the commit groups and messages before implementing them, then create them after approval.
