package doggo.ui.cli;

/**
 * Parses commands available in the Dashboard mode.
 */
final class DashboardCommandParser implements ModeCommandParser {
    @Override
    public Command parse(String command) {
        if (command.equalsIgnoreCase("new")) {
            return new NewTripCommand();
        }
        return new UnknownCommand(command);
    }
}
