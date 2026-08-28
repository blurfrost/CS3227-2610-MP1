package doggo.ui.cli;

/**
 * Parses commands available while viewing a read-only Gallery Trip.
 */
final class GalleryTripCommandParser implements ModeCommandParser {
    @Override
    public Command parse(String command) {
        if (command.equalsIgnoreCase("new")) {
            return new NewPlanCommand();
        }
        return new UnknownCommand(command);
    }
}
