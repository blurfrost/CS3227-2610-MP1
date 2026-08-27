import java.util.Optional;
import java.util.UUID;

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
                    "Plan index must refer to a listed Plan."), false);
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
            return new CommandResult(context.formatter().error(exception.getMessage()), false);
        }
        return selectedTripResult(context, "Plan deleted.");
    }

    private static CommandResult selectedTripResult(CliContext context, String message) {
        return context.session().selectedTripId()
                .flatMap(context.service()::getTrip)
                .map(trip -> new CommandResult(message + "\n" + context.selectedTripView(trip), false))
                .orElseGet(() -> new CommandResult(context.formatter().error(
                        "Selected Trip could not be found."), false));
    }
}
