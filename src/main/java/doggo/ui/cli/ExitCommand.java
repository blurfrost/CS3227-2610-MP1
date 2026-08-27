package doggo.ui.cli;

final class ExitCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        return new CommandResult("Bye!", true);
    }
}
