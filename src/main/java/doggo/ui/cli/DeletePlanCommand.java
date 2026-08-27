package doggo.ui.cli;

import java.util.Optional;
import java.util.UUID;

import doggo.domain.Trip;

/**
 * Deletes a Plan selected from the displayed Plans in a Trip.
 */
final class DeletePlanCommand implements Command {
    private final int index;

    DeletePlanCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        Optional<UUID> planId = context.session().planIdAt(index);
        if (planId.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    "Plan index must refer to a listed Plan.\n" + refreshSelectedTrip(context)), false);
        }
        Optional<Trip> selectedTrip = context.session().selectedTripId().flatMap(context.service()::getTrip);
        if (selectedTrip.isEmpty()) {
            context.session().enterOrganise();
            return new CommandResult(context.formatter().error(
                    "Selected Trip could not be found.\n" + context.organiseMenu()), false);
        }
        if (selectedTrip.orElseThrow().plans().stream().noneMatch(
                plan -> plan.id().equals(planId.orElseThrow()))) {
            return new CommandResult(context.formatter().error(
                    "The selected Plan is no longer available.\n" + refreshSelectedTrip(context)), false);
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
            return selectedTripResult(context, "Plan deletion cancelled.");
        }

        try {
            context.service().deletePlan(context.session().selectedTripId().orElseThrow(),
                    planId.orElseThrow());
        } catch (IllegalArgumentException exception) {
            return new CommandResult(context.formatter().error(
                    exception.getMessage() + "\n" + refreshSelectedTrip(context)), false);
        }
        return selectedTripResult(context, "Plan deleted.");
    }

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

    /**
     * Refreshes the selected Trip view or returns to Organise if it disappeared.
     *
     * @param context CLI dependencies.
     * @return Refreshed active view.
     */
    private static String refreshSelectedTrip(CliContext context) {
        return context.session().selectedTripId()
                .flatMap(context.service()::getTrip)
                .map(context::selectedTripView)
                .orElseGet(() -> {
                    context.session().enterOrganise();
                    return context.organiseMenu();
                });
    }
}
