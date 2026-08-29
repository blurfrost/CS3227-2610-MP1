package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrganiseCommandTest {
    @Test
    void execute_fromAnyMode_entersOrganise() {
        CliContext context = CommandTestHelper.context();

        CommandResult result = new OrganiseCommand().execute(context);

        assertEquals(CliMode.ORGANISE, context.session().mode());
        assertTrue(result.message().contains("[MODE: ORGANISE]"));
        assertFalse(result.shouldExit());
    }
}
