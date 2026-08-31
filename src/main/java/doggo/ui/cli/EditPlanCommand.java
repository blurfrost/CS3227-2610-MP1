package doggo.ui.cli;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import doggo.domain.Plan;
import doggo.domain.Trip;

final class EditPlanCommand implements Command {
    private final int index;

    EditPlanCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        Optional<PlanTarget> planTarget = context.session().planTargetAt(index);
        if (planTarget.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    context.formatter().invalidIndex("edit", IndexedEntity.PLAN,
                            context.session().displayedPlanCount()) + "\n"
                            + context.refreshCurrentView()), false);
        }
        PlanTarget target = planTarget.orElseThrow();
        if (context.session().mode() == CliMode.TRIP
                || context.session().mode() == CliMode.GALLERY_TRIP) {
            Optional<Trip> selectedTrip = context.selectedTripForMode();
            if (selectedTrip.isEmpty()) {
                String message = context.session().selectedTripId()
                        .flatMap(context.service()::getTrip)
                        .isEmpty()
                                ? "Selected Trip could not be found."
                                : "Selected Trip is no longer available.";
                return new CommandResult(context.formatter().error(
                        message + "\n"
                                + context.refreshCurrentView()), false);
            }
        }
        if (context.resolvePlanTargetForCurrentMode(target).isEmpty()) {
            return new CommandResult(context.formatter().error(
                    "The selected Plan is no longer available.\n"
                            + context.refreshCurrentView()), false);
        }
        Optional<Trip> trip = context.service().getTrip(target.tripId());
        if (trip.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    "Selected Trip could not be found.\n" + context.refreshCurrentView()), false);
        }
        Trip selectedTrip = trip.orElseThrow();
        Optional<Plan> plan = selectedTrip.plans().stream()
                .filter(candidate -> candidate.id().equals(target.planId()))
                .findFirst();
        if (plan.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    "The selected Plan is no longer available.\n"
                            + context.refreshCurrentView()), false);
        }

        Plan selectedPlan = plan.orElseThrow();
        String destination = promptDestination(context, selectedPlan);
        if (destination == null) {
            return new CommandResult("Bye!", true);
        }
        LocalDate date = promptDate(context, selectedPlan.date());
        if (date == null) {
            return new CommandResult("Bye!", true);
        }
        while (date.isBefore(selectedTrip.startDate()) || date.isAfter(selectedTrip.endDate())) {
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
            return planResult(context, "No changes made.");
        }
        try {
            context.service().editPlan(target.tripId(), target.planId(), destination, date, time);
        } catch (IllegalArgumentException exception) {
            return planResult(context, context.formatter().error(exception.getMessage()));
        }
        return planResult(context, "Plan updated.");
    }

    private static String promptText(CliContext context, String message) {
        String value = context.prompter().prompt(message);
        return value == null ? null : value.trim();
    }

    /**
     * Prompts until a valid Plan destination is entered or the current destination is preserved.
     *
     * @param context CLI dependencies.
     * @param plan Plan being edited.
     * @return Valid destination, current destination for blank input, or null when input ends.
     */
    private static String promptDestination(CliContext context, Plan plan) {
        while (true) {
            String destination = promptText(context,
                    "Enter plan destination [Current: " + plan.destination() + "]:");
            if (destination == null) {
                return null;
            }
            if (destination.isBlank()) {
                return plan.destination();
            }
            String validationMessage = CliTextValidator.validatePlanDestination(destination);
            if (validationMessage.isEmpty()) {
                return destination;
            }
            context.output().println(context.formatter().error(validationMessage));
        }
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
     * Refreshes the active view after a Plan operation.
     *
     * @param context CLI dependencies.
     * @param message Operation result message.
     * @return Result containing the message and refreshed view.
     */
    private static CommandResult planResult(CliContext context, String message) {
        return new CommandResult(message + "\n" + context.refreshCurrentView(), false);
    }
}
