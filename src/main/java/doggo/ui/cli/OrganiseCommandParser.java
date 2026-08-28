package doggo.ui.cli;

/**
 * Parses commands available in the Organise mode.
 */
final class OrganiseCommandParser implements ModeCommandParser {
    @Override
    public Command parse(String command) {
        if (command.equalsIgnoreCase("new")) {
            return new NewTripCommand();
        }
        if (command.matches("(?i)edit(?:\\s+.*)?")) {
            return IndexedCommandParser.parse(command, "edit", IndexedEntity.TRIP,
                    EditTripCommand::new);
        }
        if (command.matches("(?i)view(?:\\s+.*)?")) {
            return IndexedCommandParser.parse(command, "view", IndexedEntity.TRIP,
                    ViewTripCommand::new);
        }
        if (command.matches("(?i)delete(?:\\s+.*)?")) {
            return IndexedCommandParser.parse(command, "delete", IndexedEntity.TRIP,
                    DeleteTripCommand::new);
        }
        return new UnknownCommand(command);
    }
}
