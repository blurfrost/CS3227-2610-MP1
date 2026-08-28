package doggo.ui.cli;

import java.util.Optional;
import java.util.UUID;

/**
 * Deletes a Trip selected from an active Trip list.
 */
final class DeleteTripCommand implements Command {
    private final int index;

    DeleteTripCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        CliMode initiatingMode = context.session().mode();
        Optional<UUID> tripId = context.session().tripIdAt(index);
        if (tripId.isEmpty()) {
            return tripResult(context, context.formatter().error(
                    context.formatter().invalidIndex("delete", IndexedEntity.TRIP,
                            context.session().displayedTripCount())), initiatingMode);
        }
        if (context.displayedTripAt(index, initiatingMode).isEmpty()) {
            return tripResult(context, context.formatter().error(
                    "The selected Trip is no longer available."), initiatingMode);
        }

        String confirmation = context.prompter().prompt(
                "Type yes to delete this Trip and all its Plans, or no to cancel:");
        if (confirmation == null) {
            return new CommandResult("Bye!", true);
        }
        while (!confirmation.trim().equals("yes") && !confirmation.trim().equals("no")) {
            context.output().println(context.formatter().error("Please enter exactly yes or no."));
            confirmation = context.prompter().prompt(
                    "Type yes to delete this Trip and all its Plans, or no to cancel:");
            if (confirmation == null) {
                return new CommandResult("Bye!", true);
            }
        }
        if (confirmation.trim().equals("no")) {
            return tripResult(context, "Trip deletion cancelled.", initiatingMode);
        }

        try {
            context.service().deleteTrip(tripId.orElseThrow());
        } catch (IllegalArgumentException exception) {
            return tripResult(context, context.formatter().error(exception.getMessage()), initiatingMode);
        }
        return tripResult(context, "Trip deleted.", initiatingMode);
    }

    /**
     * Returns a Trip command result with the initiating Trip list refreshed.
     *
     * @param context CLI dependencies.
     * @param message Operation result message.
     * @param initiatingMode Trip list mode from which the command started.
     * @return Result containing the message and refreshed Trip list.
     */
    private static CommandResult tripResult(CliContext context, String message,
                                             CliMode initiatingMode) {
        return new CommandResult(message + "\n" + context.refreshTripList(initiatingMode), false);
    }
}
