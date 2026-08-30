package doggo.ui.cli;

import java.util.Optional;

import doggo.domain.Plan;
import doggo.domain.Review;
import doggo.domain.Trip;

/**
 * Adds, updates, or removes a Review on a Plan displayed in a Plan view.
 */
final class ReviewPlanCommand implements Command {
    private final int index;

    ReviewPlanCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        Optional<PlanTarget> planTarget = context.session().planTargetAt(index);
        if (planTarget.isEmpty()) {
            return planResult(context, context.formatter().error(
                    context.formatter().invalidIndex("review", IndexedEntity.PLAN,
                            context.session().displayedPlanCount())));
        }

        PlanTarget target = planTarget.orElseThrow();
        String targetError = validateSelectedTrip(context, target);
        if (targetError != null) {
            return planResult(context, context.formatter().error(targetError));
        }

        Optional<Plan> plan = findPlan(context, target);
        if (plan.isEmpty()) {
            return planResult(context, context.formatter().error(
                    "The selected Plan is no longer available."));
        }
        ReviewInputHelper.Result input = ReviewInputHelper.prompt(context,
                plan.orElseThrow().review());
        if (input.endOfInput()) {
            return new CommandResult("Bye!", true);
        }

        targetError = validateSelectedTrip(context, target);
        if (targetError != null) {
            return planResult(context, context.formatter().error(targetError));
        }
        Optional<Plan> refreshedPlan = findPlan(context, target);
        if (refreshedPlan.isEmpty()) {
            return planResult(context, context.formatter().error(
                    "The selected Plan is no longer available."));
        }
        Optional<Review> existingReview = refreshedPlan.orElseThrow().review();
        Optional<Review> submittedReview = input.review();
        if (existingReview.equals(submittedReview)) {
            return planResult(context, "No changes made.");
        }

        try {
            if (submittedReview.isEmpty()) {
                context.service().removePlanReview(target.tripId(), target.planId());
                return planResult(context, "Review removed.");
            }
            context.service().setPlanReview(target.tripId(), target.planId(),
                    submittedReview.orElseThrow());
            String message = existingReview.isEmpty() ? "Review added." : "Review updated.";
            return planResult(context, message);
        } catch (IllegalArgumentException exception) {
            return planResult(context, context.formatter().error(exception.getMessage()));
        }
    }

    /**
     * Validates that a selected Trip still owns the Plan target.
     *
     * @param context CLI dependencies.
     * @param target Composite Plan target.
     * @return Error message, or null when the selected Trip is valid.
     */
    private static String validateSelectedTrip(CliContext context, PlanTarget target) {
        CliMode mode = context.session().mode();
        if (mode == CliMode.DASHBOARD) {
            return isDisplayedDashboardTarget(context, target)
                    ? null
                    : "The selected Plan is no longer available.";
        }
        if (mode != CliMode.TRIP && mode != CliMode.GALLERY_TRIP) {
            return "Selected Trip could not be found.";
        }
        Optional<Trip> selectedTrip = context.selectedTripForMode();
        if (selectedTrip.isEmpty()) {
            return context.session().selectedTripId()
                    .flatMap(context.service()::getTrip)
                    .isEmpty()
                            ? "Selected Trip could not be found."
                            : "Selected Trip is no longer available.";
        }
        return selectedTrip.orElseThrow().id().equals(target.tripId())
                ? null
                : "The selected Plan is no longer available.";
    }

    /**
     * Finds the Plan represented by a retained composite target.
     *
     * @param context CLI dependencies.
     * @param target Composite Plan target.
     * @return Matching Plan, if the owning Trip and Plan still exist.
     */
    private static Optional<Plan> findPlan(CliContext context, PlanTarget target) {
        return context.service().getTrip(target.tripId())
                .flatMap(trip -> trip.plans().stream()
                        .filter(plan -> plan.id().equals(target.planId()))
                        .findFirst());
    }

    /**
     * Checks that a Dashboard target remains in today's Dashboard snapshot.
     *
     * @param context CLI dependencies.
     * @param target Composite Plan target.
     * @return True when both target identities are present in the Dashboard.
     */
    private static boolean isDisplayedDashboardTarget(CliContext context, PlanTarget target) {
        return context.service().getDashboardEntries().stream()
                .anyMatch(entry -> entry.tripId().equals(target.tripId())
                        && entry.plan().id().equals(target.planId()));
    }

    /**
     * Refreshes the active Plan view after a review operation.
     *
     * @param context CLI dependencies.
     * @param message Operation result message.
     * @return Result containing the message and refreshed view.
     */
    private static CommandResult planResult(CliContext context, String message) {
        return new CommandResult(message + "\n" + context.refreshCurrentView(), false);
    }
}
