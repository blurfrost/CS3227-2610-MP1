import java.io.PrintWriter;

record CliContext(DoggoService service, CliSession session, CliPrompter prompter,
                  CliFormatter formatter, PrintWriter output) {
}
