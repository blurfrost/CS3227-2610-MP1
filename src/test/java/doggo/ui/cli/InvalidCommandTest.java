package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InvalidCommandTest {
    @Test
    void execute_withUsage_returnsErrorAndCurrentView() {
        CliContext context = CommandTestHelper.context();
        context.session().enterDashboard();

        CommandResult result = new InvalidCommand("Usage: edit NUMBER").execute(context);

        assertTrue(result.message().contains("Error: Usage: edit NUMBER"));
        assertTrue(result.message().contains("[MODE: DASHBOARD]"));
        assertFalse(result.shouldExit());
    }
}
