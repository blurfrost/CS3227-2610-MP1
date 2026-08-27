package doggo.ui.cli;

interface Command {
    CommandResult execute(CliContext context);
}
