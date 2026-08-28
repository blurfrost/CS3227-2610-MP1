package doggo.ui.cli;

final class InvalidCommand implements Command {
    private final String usage;

    InvalidCommand(String usage) {
        this.usage = usage;
    }

    @Override
    public CommandResult execute(CliContext context) {
        return new CommandResult(context.formatter().error(
                usage + "\n" + context.refreshCurrentView()), false);
    }
}
