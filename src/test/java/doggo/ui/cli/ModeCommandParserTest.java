package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class ModeCommandParserTest {
    @Test
    void mainCommandParser_routesOrganiseCommand() {
        ModeCommandParser parser = new MainCommandParser();

        assertInstanceOf(OrganiseCommand.class, parser.parse("organise"));
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
}
