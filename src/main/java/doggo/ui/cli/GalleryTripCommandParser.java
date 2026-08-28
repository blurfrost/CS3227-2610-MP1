package doggo.ui.cli;

/**
 * Parses commands available while viewing a read-only Gallery Trip.
 */
final class GalleryTripCommandParser implements ModeCommandParser {
    @Override
    public Command parse(String command) {
        return new UnknownCommand(command);
    }
}
