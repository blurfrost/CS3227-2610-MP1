package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class ModeCommandParserTest {
    @Test
    void mainCommandParser_routesOrganiseCommand() {
        ModeCommandParser parser = new MainCommandParser();

        assertInstanceOf(OrganiseCommand.class, parser.parse("organise"));
        assertInstanceOf(NewTripCommand.class, parser.parse("new"));
        assertInstanceOf(DashboardCommand.class, parser.parse("DaShBoArD"));
        assertInstanceOf(GalleryCommand.class, parser.parse("GaLlErY"));
    }

    @Test
    void organiseCommandParser_routesTripCommands() {
        ModeCommandParser parser = new OrganiseCommandParser();

        assertInstanceOf(NewTripCommand.class, parser.parse("new"));
        assertInstanceOf(EditTripCommand.class, parser.parse("edit 1"));
        assertInstanceOf(ViewTripCommand.class, parser.parse("view 1"));
        assertInstanceOf(DeleteTripCommand.class, parser.parse("delete 1"));
    }

    @Test
    void tripCommandParser_routesPlanCommands() {
        ModeCommandParser parser = new TripCommandParser();

        assertInstanceOf(NewPlanCommand.class, parser.parse("new"));
        assertInstanceOf(EditPlanCommand.class, parser.parse("edit 1"));
        assertInstanceOf(DeletePlanCommand.class, parser.parse("delete 1"));
    }

    @Test
    void dashboardCommandParser_routesCreationEditingAndDeletion() {
        ModeCommandParser parser = new DashboardCommandParser();

        assertInstanceOf(NewTripCommand.class, parser.parse("NeW"));
        assertInstanceOf(EditPlanCommand.class, parser.parse("EdIt 1"));
        assertInstanceOf(DeletePlanCommand.class, parser.parse("DeLeTe 1"));
        assertInstanceOf(UnknownCommand.class, parser.parse("new 1"));
    }

    @Test
    void galleryCommandParsers_routeTripCreationNavigationAndEditing() {
        ModeCommandParser galleryParser = new GalleryCommandParser();
        ModeCommandParser galleryTripParser = new GalleryTripCommandParser();

        assertInstanceOf(NewTripCommand.class, galleryParser.parse("NeW"));
        assertInstanceOf(ViewGalleryTripCommand.class, galleryParser.parse("ViEw 1"));
        assertInstanceOf(UnknownCommand.class, galleryParser.parse("new 1"));
        assertInstanceOf(EditTripCommand.class, galleryParser.parse("edit 1"));
        assertInstanceOf(DeleteTripCommand.class, galleryParser.parse("delete 1"));
        assertInstanceOf(InvalidCommand.class, galleryParser.parse("delete"));
        assertInstanceOf(NewPlanCommand.class, galleryTripParser.parse("NeW"));
        assertInstanceOf(EditPlanCommand.class, galleryTripParser.parse("EdIt 1"));
        assertInstanceOf(DeletePlanCommand.class, galleryTripParser.parse("DeLeTe 1"));
        assertInstanceOf(UnknownCommand.class, galleryTripParser.parse("new 1"));
        assertInstanceOf(InvalidCommand.class, galleryTripParser.parse("delete"));
    }
}
