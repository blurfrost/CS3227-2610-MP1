package doggo.ui.cli;

import java.util.Optional;
import java.util.UUID;

/**
 * Deletes a Trip selected from the displayed Organise list.
 */
final class DeleteTripCommand implements Command {
    private final int index;

    DeleteTripCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        Optional<UUID> tripId = context.session().tripIdAt(index);
        if (tripId.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    context.formatter().invalidIndex("delete", IndexedEntity.TRIP,
                            context.session().displayedTripCount()) + "\n"
                            + context.organiseMenu()), false);
        }
        if (context.service().getTrip(tripId.orElseThrow()).isEmpty()) {
            return new CommandResult(context.formatter().error(
                    "The selected Trip is no longer available.\n"
                            + context.organiseMenu()), false);
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
            return new CommandResult("Trip deletion cancelled.\n" + context.organiseMenu(), false);
        }

        try {
            context.service().deleteTrip(tripId.orElseThrow());
        } catch (IllegalArgumentException exception) {
            return new CommandResult(context.formatter().error(
                    exception.getMessage() + "\n" + context.organiseMenu()), false);
        }
        return new CommandResult("Trip deleted.\n" + context.organiseMenu(), false);
    }
}
