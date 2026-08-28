package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class IndexedCommandParserTest {
    @Test
    void parseValidIndex_createsCommand() {
        Command command = IndexedCommandParser.parse("edit 1", "edit", IndexedEntity.TRIP,
                EditTripCommand::new);

        assertInstanceOf(EditTripCommand.class, command);
    }

    @Test
    void parseMissingOrExtraArguments_returnsUsageCommand() {
        assertInstanceOf(InvalidCommand.class, IndexedCommandParser.parse(
                "edit", "edit", IndexedEntity.TRIP, EditTripCommand::new));
        assertInstanceOf(InvalidCommand.class, IndexedCommandParser.parse(
                "edit 1 2", "edit", IndexedEntity.TRIP, EditTripCommand::new));
    }

    @Test
    void parseInvalidIndex_returnsIndexFeedbackCommand() {
        assertInstanceOf(InvalidIndexCommand.class, IndexedCommandParser.parse(
                "edit abc", "edit", IndexedEntity.TRIP, EditTripCommand::new));
        assertInstanceOf(InvalidIndexCommand.class, IndexedCommandParser.parse(
                "edit -1", "edit", IndexedEntity.TRIP, EditTripCommand::new));
        assertInstanceOf(InvalidIndexCommand.class, IndexedCommandParser.parse(
                "edit 999999999999999999999", "edit", IndexedEntity.TRIP,
                EditTripCommand::new));
    }
}
