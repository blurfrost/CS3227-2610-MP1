final class OrganiseCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        context.session().clearSelectedTripId();
        context.session().setMode(CliMode.ORGANISE);
        return new CommandResult(context.organiseMenu(), false);
    }
}
