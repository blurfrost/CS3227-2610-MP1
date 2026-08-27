package doggo.ui.cli;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import doggo.domain.Plan;
import doggo.domain.Trip;

final class EditPlanCommand implements Command {
    private final int index;

    EditPlanCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        Optional<UUID> tripId = context.session().selectedTripId();
        Optional<UUID> planId = context.session().planIdAt(index);
        Optional<Trip> trip = tripId.flatMap(context.service()::getTrip);
        if (trip.isEmpty()) {
            context.session().enterOrganise();
            return new CommandResult(context.formatter().error(
                    "Selected Trip could not be found.\n" + context.organiseMenu()), false);
        }
        if (planId.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    "Plan index must refer to a listed Plan.\n" + context.selectedTripView(trip.orElseThrow())), false);
        }
        Optional<Plan> plan = trip.orElseThrow().plans().stream()
                .filter(candidate -> candidate.id().equals(planId.orElseThrow()))
                .findFirst();
        if (plan.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    "The selected Plan is no longer available.\n"
                            + context.selectedTripView(trip.orElseThrow())), false);
        }

        Plan selectedPlan = plan.orElseThrow();
        String destination = promptText(context,
                "Enter plan destination [Current: " + selectedPlan.destination() + "]:");
        if (destination == null) {
            return new CommandResult("Bye!", true);
        }
        if (destination.isBlank()) {
            destination = selectedPlan.destination();
        }
        LocalDate date = promptDate(context, selectedPlan.date());
        if (date == null) {
            return new CommandResult("Bye!", true);
        }
        while (date.isBefore(trip.orElseThrow().startDate())
                || date.isAfter(trip.orElseThrow().endDate())) {
            context.output().println(context.formatter().error(
                    "Plan date must fall within the Trip dates."));
            date = promptDate(context, date);
            if (date == null) {
                return new CommandResult("Bye!", true);
            }
        }
        LocalTime time = promptTime(context, selectedPlan.time());
        if (time == null) {
            return new CommandResult("Bye!", true);
        }
        if (destination.equals(selectedPlan.destination()) && date.equals(selectedPlan.date())
                && time.equals(selectedPlan.time())) {
            return selectedTripResult(context, "No changes made.");
        }
        try {
            context.service().editPlan(tripId.orElseThrow(), planId.orElseThrow(), destination, date, time);
        } catch (IllegalArgumentException exception) {
            return selectedTripResult(context, context.formatter().error(exception.getMessage()));
        }
        return selectedTripResult(context, "Plan updated.");
    }

    private static String promptText(CliContext context, String message) {
        String value = context.prompter().prompt(message);
        return value == null ? null : value.trim();
    }

    /**
     * Prompts for an optional Plan date until it is syntactically valid.
     *
     * @param context CLI dependencies.
     * @param currentDate Existing date.
     * @return Proposed date, or null when input ends.
     */
    private static LocalDate promptDate(CliContext context, LocalDate currentDate) {
        while (true) {
            String value = context.prompter().prompt(
                    "Enter plan date [Current: " + CliFormatter.formatDate(currentDate) + "]:");
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

    /**
     * Prompts for an optional Plan time until it is syntactically valid.
     *
     * @param context CLI dependencies.
     * @param currentTime Existing time.
     * @return Proposed time, or null when input ends.
     */
    private static LocalTime promptTime(CliContext context, LocalTime currentTime) {
        while (true) {
            String value = context.prompter().prompt(
                    "Enter plan time [Current: " + CliFormatter.formatTime(currentTime) + "]:");
            if (value == null) {
                return null;
            }
            if (value.trim().isEmpty()) {
                return currentTime;
            }
            try {
                return InputParser.parseTime(value);
            } catch (IllegalArgumentException exception) {
                context.output().println(context.formatter().error(
                        "Time must use the HH:mm format and be a real time."));
            }
        }
    }

    /**
     * Refreshes the selected Trip view after a Plan operation.
     *
     * @param context CLI dependencies.
     * @param message Operation result message.
     * @return Result containing the message and refreshed view.
     */
    private static CommandResult selectedTripResult(CliContext context, String message) {
        return context.session().selectedTripId()
                .flatMap(context.service()::getTrip)
                .map(trip -> new CommandResult(message + "\n" + context.selectedTripView(trip), false))
                .orElseGet(() -> {
                    context.session().enterOrganise();
                    return new CommandResult(context.formatter().error(
                            "Selected Trip could not be found.\n" + context.organiseMenu()), false);
                });
    }
}
