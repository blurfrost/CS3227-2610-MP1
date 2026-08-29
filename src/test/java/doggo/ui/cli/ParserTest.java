package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class ParserTest {
    @Test
    void parse_globalExitAndBackCommands_returnsGlobalCommands() {
        Parser parser = new Parser();

        assertInstanceOf(ExitCommand.class, parser.parse(" EXIT ", CliMode.ORGANISE));
        assertInstanceOf(BackCommand.class, parser.parse(" back ", CliMode.TRIP));
    }

    @Test
    void parse_inputWithWhitespace_delegatesNormalizedCommand() {
        Parser parser = new Parser();

        assertInstanceOf(NewTripCommand.class, parser.parse("  new  ", CliMode.MAIN));
    }

    @Test
    void parse_nullInputOrMode_returnsUnknownCommand() {
        Parser parser = new Parser();

        assertInstanceOf(UnknownCommand.class, parser.parse(null, CliMode.MAIN));
        assertInstanceOf(UnknownCommand.class, parser.parse("new", null));
    }
}
