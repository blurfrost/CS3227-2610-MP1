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
        return new UnknownCommand(command);
    }
}
