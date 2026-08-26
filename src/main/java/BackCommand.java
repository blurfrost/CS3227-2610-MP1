final class BackCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        context.session().setMode(CliMode.MAIN);
        return new CommandResult(context.formatter().mainMenu(), false);
    }
}
