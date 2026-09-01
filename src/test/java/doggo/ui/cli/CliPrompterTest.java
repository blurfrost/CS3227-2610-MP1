package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

class CliPrompterTest {
    @Test
    void prompt_inputAvailable_returnsInputAndWritesPrompt() {
        StringWriter output = new StringWriter();
        CliPrompter prompter = new CliPrompter(
                new BufferedReader(new StringReader("answer\n")), new PrintWriter(output));

        String answer = prompter.prompt("Enter a value:");

        assertEquals("answer", answer);
        assertEquals(String.join(System.lineSeparator(), "---", "Enter a value:", "> "),
                output.toString());
    }

    @Test
    void prompt_inputReadFails_returnsNull() {
        BufferedReader input = new BufferedReader(new StringReader("")) {
            @Override
            public String readLine() throws IOException {
                throw new IOException("Input unavailable.");
            }
        };
        CliPrompter prompter = new CliPrompter(input, new PrintWriter(new StringWriter()));

        assertNull(prompter.prompt("Enter a value:"));
    }
}
