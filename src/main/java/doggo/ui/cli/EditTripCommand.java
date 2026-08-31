package doggo.ui.cli;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import doggo.domain.Trip;

final class EditTripCommand implements Command {
    private final int index;

    EditTripCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        CliMode initiatingMode = context.session().mode();
        Optional<UUID> tripId = context.session().tripIdAt(index);
        if (tripId.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    context.formatter().invalidIndex("edit", IndexedEntity.TRIP,
                            context.session().displayedTripCount())
                            + "\n" + context.refreshTripList(initiatingMode)), false);
        }
        Optional<Trip> storedTrip = context.displayedTripAt(index, initiatingMode);
        if (storedTrip.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    "The selected Trip is no longer available.\n"
                            + context.refreshTripList(initiatingMode)), false);
        }

        Trip trip = storedTrip.orElseThrow();
        String title = promptTitle(context, trip);
        if (title == null) {
            return new CommandResult("Bye!", true);
        }
        LocalDate[] dates = promptDates(context, trip);
        if (dates == null) {
            return new CommandResult("Bye!", true);
        }
        LocalDate startDate = dates[0];
        LocalDate endDate = dates[1];
        if (title.equals(trip.title()) && startDate.equals(trip.startDate())
                && endDate.equals(trip.endDate())) {
            return new CommandResult("No changes made.\n"
                    + context.refreshTripList(initiatingMode), false);
        }

        Trip updatedTrip;
        try {
            updatedTrip = context.service().editTrip(trip.id(), title, startDate, endDate);
        } catch (IllegalArgumentException exception) {
            context.output().println(context.formatter().error(exception.getMessage()));
            return new CommandResult(context.refreshTripList(initiatingMode), false);
        }
        return new CommandResult("Trip updated.\n" + context.enterTripListFor(updatedTrip), false);
    }

    private static String promptText(CliContext context, String message) {
        String value = context.prompter().prompt(message);
        return value == null ? null : value.trim();
    }

    /**
     * Prompts until a valid Trip title is entered or the current title is preserved.
     *
     * @param context CLI dependencies.
     * @param trip Trip being edited.
     * @return Valid title, current title for blank input, or null when input ends.
     */
    private static String promptTitle(CliContext context, Trip trip) {
        while (true) {
            String title = promptText(context, "Enter trip title [Current: " + trip.title() + "]:");
            if (title == null) {
                return null;
            }
            if (title.isBlank()) {
                return trip.title();
            }
            String validationMessage = CliTextValidator.validateTripTitle(title);
            if (validationMessage.isEmpty()) {
                return title;
            }
            context.output().println(context.formatter().error(validationMessage));
        }
    }

    /**
     * Prompts for both Trip dates until their combined values are valid.
     *
     * @param context CLI dependencies.
     * @param trip Trip being edited.
     * @return Proposed dates, or null when input ends.
     */
    private static LocalDate[] promptDates(CliContext context, Trip trip) {
        LocalDate startDate = promptStartDate(context, trip);
        if (startDate == null) {
            return null;
        }
        LocalDate endDate = promptEndDate(context, trip, startDate);
        return endDate == null ? null : new LocalDate[] {startDate, endDate};
    }

    /**
     * Prompts for a start date that does not exclude the current end date or Plans.
     *
     * @param context CLI dependencies.
     * @param trip Trip being edited.
     * @return Proposed start date, or null when input ends.
     */
    private static LocalDate promptStartDate(CliContext context, Trip trip) {
        while (true) {
            LocalDate startDate = promptDate(context, "trip start date", trip.startDate());
            if (startDate == null) {
                return null;
            }
            if (startDate.isAfter(trip.endDate())) {
                context.output().println(context.formatter().error(
                        "Trip start date cannot be after its current end date."));
                continue;
            }
            if (trip.plans().stream().anyMatch(plan -> startDate.isAfter(plan.date()))) {
                context.output().println(context.formatter().error(
                        "Trip start date cannot be after an existing Plan date."));
                continue;
            }
            return startDate;
        }
    }

    /**
     * Prompts for an end date that does not precede the proposed start date or Plans.
     *
     * @param context CLI dependencies.
     * @param trip Trip being edited.
     * @param startDate Proposed start date.
     * @return Proposed end date, or null when input ends.
     */
    private static LocalDate promptEndDate(CliContext context, Trip trip, LocalDate startDate) {
        while (true) {
            LocalDate endDate = promptDate(context, "trip end date", trip.endDate());
            if (endDate == null) {
                return null;
            }
            if (endDate.isBefore(startDate)) {
                context.output().println(context.formatter().error(
                        "Trip end date cannot be before its start date."));
                continue;
            }
            if (trip.plans().stream().anyMatch(plan -> endDate.isBefore(plan.date()))) {
                context.output().println(context.formatter().error(
                        "Trip end date cannot be before an existing Plan date."));
                continue;
            }
            return endDate;
        }
    }

    /**
     * Prompts for one optional Trip date.
     *
     * @param context CLI dependencies.
     * @param fieldName Display name of the date field.
     * @param currentDate Existing date.
     * @return Proposed date, or null when input ends.
     */
    private static LocalDate promptDate(CliContext context, String fieldName, LocalDate currentDate) {
        while (true) {
            String value = context.prompter().prompt(
                    "Enter " + fieldName + " [Current: " + CliFormatter.formatDate(currentDate) + "]:");
            if (value == null) {
                return null;
            }
            if (value.trim().isEmpty()) {
                return currentDate;
            }
            try {
                return InputParser.parseDate(value);
            } catch (IllegalArgumentException exception) {
                context.output().println(context.formatter().error(
                        "Date must use the DD/MM/YYYY format and be a real date."));
            }
        }
    }
}
