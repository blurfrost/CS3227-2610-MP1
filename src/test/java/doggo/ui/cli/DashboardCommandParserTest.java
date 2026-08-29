package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class DashboardCommandParserTest {
    private final DashboardCommandParser parser = new DashboardCommandParser();

    @Test
    void parse_supportedCommands_createsExpectedCommands() {
        assertInstanceOf(NewTripCommand.class, parser.parse("new"));
        assertInstanceOf(EditPlanCommand.class, parser.parse("edit 1"));
        assertInstanceOf(DeletePlanCommand.class, parser.parse("delete 1"));
        assertInstanceOf(ReviewPlanCommand.class, parser.parse("review 1"));
    }

    @Test
    void parse_malformedIndexedCommand_returnsValidationCommand() {
        assertInstanceOf(InvalidCommand.class, parser.parse("delete"));
        assertInstanceOf(InvalidIndexCommand.class, parser.parse("edit 0"));
    }

    @Test
    void parse_unsupportedCommand_returnsUnknownCommand() {
        assertInstanceOf(UnknownCommand.class, parser.parse("view 1"));
    }
}
