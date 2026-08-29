package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GalleryCommandTest {
    @Test
    void execute_fromAnyMode_entersGallery() {
        CliContext context = CommandTestHelper.context();

        CommandResult result = new GalleryCommand().execute(context);

        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("[MODE: GALLERY]"));
        assertFalse(result.shouldExit());
    }
}
