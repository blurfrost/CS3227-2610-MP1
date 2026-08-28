package doggo.ui.cli;

/**
 * Reports an invalid index using the currently displayed entity snapshot.
 */
final class InvalidIndexCommand implements Command {
    private final String action;
    private final IndexedEntity entity;

    InvalidIndexCommand(String action, IndexedEntity entity) {
        this.action = action;
        this.entity = entity;
    }

    @Override
    public CommandResult execute(CliContext context) {
        int displayedCount = entity == IndexedEntity.TRIP
                ? context.session().displayedTripCount()
                : context.session().displayedPlanCount();
        String currentView = currentView(context);
        return new CommandResult(context.formatter().error(
                context.formatter().invalidIndex(action, entity, displayedCount)
                        + "\n" + currentView), false);
    }

    /**
     * Refreshes the active view after reporting the invalid index.
     *
     * @param context CLI dependencies.
     * @return Refreshed active view.
     */
    private String currentView(CliContext context) {
        if (entity == IndexedEntity.TRIP) {
            return context.organiseMenu();
        }
        return context.session().selectedTripId()
                .flatMap(context.service()::getTrip)
                .map(context::selectedTripView)
                .orElseGet(() -> {
                    context.session().enterOrganise();
                    return context.organiseMenu();
                });
    }
}
