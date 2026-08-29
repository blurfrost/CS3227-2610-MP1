package doggo.ui.cli;

/**
 * Parses commands available while viewing a selected Gallery Trip.
 */
final class GalleryTripCommandParser implements ModeCommandParser {
    @Override
    public Command parse(String command) {
        if (command.equalsIgnoreCase("new")) {
            return new NewPlanCommand();
        }
        if (command.matches("(?i)edit(?:\\s+.*)?")) {
            return IndexedCommandParser.parse(command, "edit", IndexedEntity.PLAN,
                    EditPlanCommand::new);
        }
        if (command.matches("(?i)delete(?:\\s+.*)?")) {
            return IndexedCommandParser.parse(command, "delete", IndexedEntity.PLAN,
                    DeletePlanCommand::new);
        }
        if (command.matches("(?i)review(?:\\s+.*)?")) {
            return IndexedCommandParser.parse(command, "review", IndexedEntity.PLAN,
                    ReviewPlanCommand::new);
        }
        return new UnknownCommand(command);
    }
}
