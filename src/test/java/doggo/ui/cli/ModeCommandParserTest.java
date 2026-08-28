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
}
