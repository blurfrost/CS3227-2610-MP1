package doggo.ui.cli;

/**
 * Parses commands available in the Gallery mode.
 */
final class GalleryCommandParser implements ModeCommandParser {
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
                    ViewGalleryTripCommand::new);
        }
        return new UnknownCommand(command);
    }
}
