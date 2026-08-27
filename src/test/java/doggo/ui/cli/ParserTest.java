package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class ParserTest {
    @Test
    void parseEdit_routesToTripOrPlanCommand() {
        Parser parser = new Parser();

        assertInstanceOf(EditTripCommand.class, parser.parse("edit 1", CliMode.ORGANISE));
        assertInstanceOf(EditPlanCommand.class, parser.parse("edit 1", CliMode.TRIP));
    }

    @Test
    void parseMalformedEditOrDelete_returnsUsageCommand() {
        Parser parser = new Parser();

        assertInstanceOf(InvalidCommand.class, parser.parse("edit 1 2", CliMode.ORGANISE));
        assertInstanceOf(InvalidCommand.class, parser.parse("delete 0", CliMode.ORGANISE));
        assertInstanceOf(InvalidCommand.class, parser.parse("delete -1", CliMode.TRIP));
        assertInstanceOf(InvalidCommand.class,
                parser.parse("edit 999999999999999999999", CliMode.TRIP));
    }
}
