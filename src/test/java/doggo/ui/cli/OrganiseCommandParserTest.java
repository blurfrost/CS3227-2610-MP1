package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class OrganiseCommandParserTest {
    private final OrganiseCommandParser parser = new OrganiseCommandParser();

    @Test
    void parse_supportedCommands_createsExpectedCommands() {
        assertInstanceOf(NewTripCommand.class, parser.parse("new"));
        assertInstanceOf(EditTripCommand.class, parser.parse("edit 1"));
        assertInstanceOf(ViewTripCommand.class, parser.parse("view 1"));
        assertInstanceOf(DeleteTripCommand.class, parser.parse("delete 1"));
        assertInstanceOf(ReviewTripCommand.class, parser.parse("review 1"));
    }

    @Test
    void parse_malformedIndexedCommand_returnsValidationCommand() {
        assertInstanceOf(InvalidCommand.class, parser.parse("view"));
        assertInstanceOf(InvalidIndexCommand.class, parser.parse("delete 0"));
    }

    @Test
    void parse_unsupportedCommand_returnsUnknownCommand() {
        assertInstanceOf(UnknownCommand.class, parser.parse("archive 1"));
    }
}
