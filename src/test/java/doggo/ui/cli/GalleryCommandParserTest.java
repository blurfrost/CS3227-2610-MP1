package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class GalleryCommandParserTest {
    private final GalleryCommandParser parser = new GalleryCommandParser();

    @Test
    void parse_supportedCommands_createsExpectedCommands() {
        assertInstanceOf(NewTripCommand.class, parser.parse("new"));
        assertInstanceOf(ViewGalleryTripCommand.class, parser.parse("view 1"));
        assertInstanceOf(EditTripCommand.class, parser.parse("edit 1"));
        assertInstanceOf(DeleteTripCommand.class, parser.parse("delete 1"));
        assertInstanceOf(ReviewTripCommand.class, parser.parse("review 1"));
    }

    @Test
    void parse_malformedIndexedCommand_returnsValidationCommand() {
        assertInstanceOf(InvalidCommand.class, parser.parse("delete"));
        assertInstanceOf(InvalidIndexCommand.class, parser.parse("view abc"));
    }

    @Test
    void parse_unsupportedCommand_returnsUnknownCommand() {
        assertInstanceOf(UnknownCommand.class, parser.parse("new 1"));
    }
}
