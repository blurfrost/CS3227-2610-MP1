package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UnknownCommandTest {
    @Test
    void execute_withUnsupportedInput_returnsErrorAndCurrentView() {
        CliContext context = CommandTestHelper.context();
        context.session().enterGallery();

        CommandResult result = new UnknownCommand("archive").execute(context);

        assertTrue(result.message().contains("Error: Unknown command \"archive\"."));
        assertTrue(result.message().contains("[MODE: GALLERY]"));
        assertFalse(result.shouldExit());
    }
}
