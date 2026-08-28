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
        assertInstanceOf(InvalidIndexCommand.class, parser.parse("delete 0", CliMode.ORGANISE));
        assertInstanceOf(InvalidIndexCommand.class, parser.parse("delete -1", CliMode.TRIP));
        assertInstanceOf(InvalidIndexCommand.class,
                parser.parse("edit 999999999999999999999", CliMode.TRIP));
    }

    @Test
    void parseMalformedView_returnsIndexFeedbackCommand() {
        Parser parser = new Parser();

        assertInstanceOf(InvalidCommand.class, parser.parse("view 1 2", CliMode.ORGANISE));
        assertInstanceOf(InvalidIndexCommand.class, parser.parse("view abc", CliMode.ORGANISE));
        assertInstanceOf(InvalidIndexCommand.class, parser.parse("view 0", CliMode.ORGANISE));
    }

    @Test
    void parseDashboard_delegatesToDashboardParser() {
        Parser parser = new Parser();

        assertInstanceOf(NewTripCommand.class, parser.parse("new", CliMode.DASHBOARD));
        assertInstanceOf(EditPlanCommand.class, parser.parse("edit 1", CliMode.DASHBOARD));
        assertInstanceOf(DeletePlanCommand.class, parser.parse("delete 1", CliMode.DASHBOARD));
    }

    @Test
    void parseDashboardMalformedEdit_returnsIndexFeedbackCommand() {
        Parser parser = new Parser();

        assertInstanceOf(InvalidCommand.class,
                parser.parse("edit 1 2", CliMode.DASHBOARD));
        assertInstanceOf(InvalidIndexCommand.class,
                parser.parse("edit abc", CliMode.DASHBOARD));
        assertInstanceOf(InvalidIndexCommand.class,
                parser.parse("delete 0", CliMode.DASHBOARD));
    }
}
