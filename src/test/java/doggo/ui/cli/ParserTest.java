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

    @Test
    void parseGallery_routesViewAndEditCommands() {
        Parser parser = new Parser();

        assertInstanceOf(ViewGalleryTripCommand.class,
                parser.parse("view 1", CliMode.GALLERY));
        assertInstanceOf(InvalidCommand.class,
                parser.parse("view 1 2", CliMode.GALLERY));
        assertInstanceOf(InvalidIndexCommand.class,
                parser.parse("view abc", CliMode.GALLERY));
        assertInstanceOf(EditTripCommand.class,
                parser.parse("edit 1", CliMode.GALLERY));
        assertInstanceOf(InvalidCommand.class,
                parser.parse("edit 1 2", CliMode.GALLERY));
        assertInstanceOf(InvalidIndexCommand.class,
                parser.parse("edit abc", CliMode.GALLERY));
        assertInstanceOf(EditPlanCommand.class,
                parser.parse("edit 1", CliMode.GALLERY_TRIP));
        assertInstanceOf(InvalidCommand.class,
                parser.parse("edit 1 2", CliMode.GALLERY_TRIP));
        assertInstanceOf(InvalidIndexCommand.class,
                parser.parse("edit abc", CliMode.GALLERY_TRIP));
        assertInstanceOf(ReviewTripCommand.class,
                parser.parse("review 1", CliMode.GALLERY));
        assertInstanceOf(InvalidCommand.class,
                parser.parse("review 1 2", CliMode.GALLERY));
        assertInstanceOf(InvalidIndexCommand.class,
                parser.parse("review 0", CliMode.GALLERY));
        assertInstanceOf(UnknownCommand.class,
                parser.parse("review 1", CliMode.GALLERY_TRIP));
        assertInstanceOf(DeletePlanCommand.class,
                parser.parse("delete 1", CliMode.GALLERY_TRIP));
        assertInstanceOf(InvalidIndexCommand.class,
                parser.parse("delete abc", CliMode.GALLERY_TRIP));
        assertInstanceOf(InvalidIndexCommand.class,
                parser.parse("delete 0", CliMode.GALLERY_TRIP));
    }
}
