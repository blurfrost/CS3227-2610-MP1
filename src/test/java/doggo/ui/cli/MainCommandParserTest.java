package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class MainCommandParserTest {
    private final MainCommandParser parser = new MainCommandParser();

    @Test
    void parse_supportedCommands_createsExpectedCommands() {
        assertInstanceOf(OrganiseCommand.class, parser.parse("organise"));
        assertInstanceOf(NewTripCommand.class, parser.parse("new"));
        assertInstanceOf(DashboardCommand.class, parser.parse("dashboard"));
        assertInstanceOf(GalleryCommand.class, parser.parse("gallery"));
    }

    @Test
    void parse_commandWithDifferentCase_createsExpectedCommand() {
        assertInstanceOf(DashboardCommand.class, parser.parse("DaShBoArD"));
    }

    @Test
    void parse_unsupportedCommand_returnsUnknownCommand() {
        assertInstanceOf(UnknownCommand.class, parser.parse("view 1"));
    }
}
