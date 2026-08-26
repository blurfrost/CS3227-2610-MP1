# doggo Developer Guide

## Introduction

doggo is a local-first travel planning and journalling application. It allows users to track current trips, organise future trips, and revisit past trips through ratings, reviews, and, in a future extension, photos.

Development begins with a tested command-line interface (CLI). The application will later use JavaFX for its desktop graphical user interface while retaining the same domain, application, and persistence logic.

## Product Terminology

- **Trip:** An overall journey, such as a trip to Japan. A Trip contains zero or more Plans.
- **Plan:** One scheduled itinerary item belonging to a Trip, such as visiting a restaurant or landmark.
- **Review:** A required whole-number rating from 1 to 5 and optional written text associated with a completed Trip or Plan.
- **TripStatus:** A value derived from a Trip's inclusive start and end dates: future, current, or past.
- **Dashboard:** Displays Plans scheduled for the current day and allows users to inspect an individual Plan.
- **Organise:** Displays Trips and allows users to select a Trip to view and manage its itinerary.
- **Gallery:** Displays every Trip whose end date has passed, regardless of whether reviews are present.

## Functional User Stories

### CLI MVP

1. As a new user, I can view available commands and usage examples so that I can discover doggo's functions.
2. As a user, I can create a Trip with its name, destination, start date, and end date so that I can organise an overall journey.
3. As a user, I can edit an existing Trip so that I can correct or update its details.
4. As a user, I can delete a Trip after confirming the action so that I can remove journeys I no longer need.
5. As a user, I can add a Plan with a title and scheduled date and time to a Trip so that I can build its itinerary.
6. As a user, I can edit an existing Plan so that I can adjust my itinerary.
7. As a user, I can delete an existing Plan after confirming the action so that I can remove unwanted itinerary items.
8. As a user, I can select a Trip and view its Plans in chronological order so that I can understand its itinerary.
9. As a daily user, I can view all Plans scheduled for today in chronological order and grouped by Trip so that I can follow my daily itinerary.
10. As a user, I can view Trips grouped as future, current, or past so that I can find the relevant journey quickly.
11. As a frequent user, I can give a completed Trip or Plan a required whole-number rating from 1 to 5 and an optional written review so that I can record my experience.
12. As a frequent user, I can edit or remove a review so that my recorded experience remains accurate.
13. As a returning user, I can find my Trips, Plans, and reviews after restarting doggo so that my travel data is retained.

### JavaFX Desktop

14. As a new user, I can access Dashboard, Organise, and Gallery through persistent primary navigation so that I can discover and reach every major function.
15. As a user, I can use Dashboard to inspect the details of an individual Plan scheduled for today.
16. As a user, I can use Organise to select a Trip and view or manage its itinerary.
17. As a user, I can use Gallery to view every Trip whose end date has passed, regardless of whether it has been reviewed.
18. As a user, I can view available Trip and Plan ratings and reviews within a past Trip's Gallery entry.

### Future Extensions

19. As a user, I can attach, view, and remove photos from a Plan review so that I can preserve visual memories.
20. As a frequent user, I can copy a Plan from a past Trip into a future Trip so that a positively reviewed place can be reused.
21. As a user, I can search or filter Trips and Plans so that a growing travel history remains manageable.

## Non-Functional User Stories

1. As a user, I receive clear validation and recovery guidance for invalid input so that mistakes do not terminate the application.
2. As a user, previously saved data remains intact if a later save fails so that I do not lose valid travel records.
3. As a user, common commands and views complete within two seconds with up to 1,000 Trips and 10,000 Plans on the supported development environment.
4. As a privacy-conscious user, I can use doggo locally without creating an account or sending travel data externally.
5. As a keyboard user, I can operate all core JavaFX functions without requiring a mouse.
6. As a user, dates and times are displayed consistently and Plans with the same time have deterministic ordering.
7. As a developer, I can replace the CLI with JavaFX without changing domain rules or persistence behaviour.
8. As a developer, I can test domain and application behaviour without launching JavaFX or accessing the production database.

## Domain Rules

- A Trip contains zero or more Plans.
- A Plan belongs to exactly one Trip.
- Trip status is derived from inclusive start and end dates:
  - A future Trip starts after the current date.
  - A current Trip includes the current date within its start and end dates.
  - A past Trip ends before the current date.
- Gallery includes every past Trip. Reviews are optional and are displayed only when present.
- A Review requires a whole-number rating from 1 to 5 and may contain written text.
- Reviews can be added only after the associated Trip or Plan has been completed.
- Deleting a Trip requires explicit confirmation and removes its Plans and associated reviews.
- Deleting a Plan requires explicit confirmation and removes its associated review.
- Dashboard includes Plans whose scheduled local date is the current date.
- Plans are displayed chronologically. Plans with equal scheduled times use deterministic ordering.
- Invalid commands, dates, ratings, and references to missing records produce actionable errors without terminating the application.

## Architectural Constraints

- CLI and JavaFX code are presentation layers and do not contain domain or persistence rules.
- Application services coordinate use cases independently of the presentation layer.
- Domain classes do not depend on JavaFX, CLI input, JDBC, or filesystem paths.
- Persistence is accessed through repository interfaces so production and test implementations can be substituted.
- Photo storage is accessed through a storage interface so local media storage can later be replaced if needed.

## Acceptance and Test Coverage

- Verify Trip and Plan creation, editing, deletion, retrieval, and persistence across application restarts.
- Verify Trip status at start-date and end-date boundaries.
- Verify Dashboard includes only today's Plans and orders them deterministically.
- Verify Gallery excludes current and future Trips and includes past Trips without reviews.
- Verify Trip and Plan review eligibility and rating validation.
- Verify failed writes do not damage previously persisted data.
- Verify domain and application tests run without JavaFX or the production database.
- Verify the CLI exposes help for all supported commands and handles invalid input without crashing.
