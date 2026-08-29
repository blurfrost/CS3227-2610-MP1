package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DashboardCommandTest {
    @Test
    void execute_fromAnyMode_entersDashboard() {
        CliContext context = CommandTestHelper.context();

        CommandResult result = new DashboardCommand().execute(context);

        assertEquals(CliMode.DASHBOARD, context.session().mode());
        assertTrue(result.message().contains("[MODE: DASHBOARD]"));
        assertFalse(result.shouldExit());
    }
}
