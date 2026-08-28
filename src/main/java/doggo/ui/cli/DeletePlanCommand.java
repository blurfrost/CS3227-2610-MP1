package doggo.ui.cli;

import java.util.Optional;

import doggo.domain.Trip;

/**
 * Deletes a Plan selected from the displayed Plans in an active Plan view.
 */
final class DeletePlanCommand implements Command {
    private final int index;

    DeletePlanCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        Optional<PlanTarget> planTarget = context.session().planTargetAt(index);
        if (planTarget.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    context.formatter().invalidIndex("delete", IndexedEntity.PLAN,
                            context.session().displayedPlanCount()) + "\n"
                            + context.refreshCurrentView()), false);
        }
        PlanTarget target = planTarget.orElseThrow();
        if (isSelectedTripMode(context)) {
            Optional<Trip> selectedTrip = context.selectedTripForMode();
            if (selectedTrip.isEmpty()) {
                String message = context.session().selectedTripId()
                        .flatMap(context.service()::getTrip)
                        .isEmpty()
                                ? "Selected Trip could not be found."
                                : "Selected Trip is no longer available.";
                return new CommandResult(context.formatter().error(
                        message + "\n" + context.refreshCurrentView()), false);
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
        if (selectedTrip.plans().stream().noneMatch(
                plan -> plan.id().equals(target.planId()))) {
            return new CommandResult(context.formatter().error(
                    "The selected Plan is no longer available.\n"
                            + context.refreshCurrentView()), false);
        }

        String confirmation = context.prompter().prompt(
                "Type yes to delete this Plan, or no to cancel:");
        if (confirmation == null) {
            return new CommandResult("Bye!", true);
        }
        while (!confirmation.trim().equals("yes") && !confirmation.trim().equals("no")) {
            context.output().println(context.formatter().error("Please enter exactly yes or no."));
            confirmation = context.prompter().prompt(
                    "Type yes to delete this Plan, or no to cancel:");
            if (confirmation == null) {
                return new CommandResult("Bye!", true);
            }
        }
        if (confirmation.trim().equals("no")) {
            return planResult(context, "Plan deletion cancelled.");
        }

        try {
            context.service().deletePlan(target.tripId(), target.planId());
        } catch (IllegalArgumentException exception) {
            return planResult(context, context.formatter().error(exception.getMessage()));
        }
        return planResult(context, "Plan deleted.");
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

    /**
     * Returns whether the current mode has a selected Trip that owns the Plan.
     *
     * @param context CLI dependencies.
     * @return Whether the current mode is a selected Trip mode.
     */
    private static boolean isSelectedTripMode(CliContext context) {
        return context.session().mode() == CliMode.TRIP
                || context.session().mode() == CliMode.GALLERY_TRIP;
    }
}
