package doggo.ui.cli;

final class BackCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        if (context.session().mode() == CliMode.TRIP) {
            context.session().enterOrganise();
            return new CommandResult(context.organiseMenu(), false);
        }
        context.session().enterMain();
        return new CommandResult(context.formatter().mainMenu(), false);
    }
}
