package doggo.ui.cli;

final class BackCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        switch (context.session().mode()) {
        case TRIP:
            context.session().enterOrganise();
            return new CommandResult(context.organiseMenu(), false);
        case MAIN:
        case ORGANISE:
        case DASHBOARD:
            context.session().enterMain();
            return new CommandResult(context.formatter().mainMenu(), false);
        default:
            throw new IllegalStateException("Unsupported CLI mode.");
        }
    }
}
