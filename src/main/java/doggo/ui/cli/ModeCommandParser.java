package doggo.ui.cli;

/**
 * Parses commands belonging to one CLI mode.
 */
@FunctionalInterface
interface ModeCommandParser {
    /**
     * Parses the specified normalized command.
     *
     * @param command Command text to parse.
     * @return Parsed command.
     */
    Command parse(String command);
}
