final class OrganiseCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        context.session().enterOrganise();
        return new CommandResult(context.organiseMenu(), false);
    }
}
