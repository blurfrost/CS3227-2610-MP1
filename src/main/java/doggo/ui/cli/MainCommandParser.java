package doggo.ui.cli;

/**
 * Parses commands available in the Main mode.
 */
final class MainCommandParser implements ModeCommandParser {
    @Override
    public Command parse(String command) {
        if (command.equalsIgnoreCase("organise")) {
            return new OrganiseCommand();
        }
        if (command.equalsIgnoreCase("new")) {
            return new NewTripCommand();
        }
        if (command.equalsIgnoreCase("dashboard")) {
            return new DashboardCommand();
        }
        return new UnknownCommand(command);
    }
}
