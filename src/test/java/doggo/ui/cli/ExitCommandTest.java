package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ExitCommandTest {
    @Test
    void execute_anyMode_returnsExitResult() {
        CommandResult result = new ExitCommand().execute(CommandTestHelper.context());

        assertEquals("Bye!", result.message());
        assertTrue(result.shouldExit());
    }
}
