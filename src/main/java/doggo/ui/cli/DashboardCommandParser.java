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
        if (command.matches("(?i)edit(?:\\s+.*)?")) {
            return IndexedCommandParser.parse(command, "edit", IndexedEntity.PLAN,
                    EditPlanCommand::new);
        }
        return new UnknownCommand(command);
    }
}
