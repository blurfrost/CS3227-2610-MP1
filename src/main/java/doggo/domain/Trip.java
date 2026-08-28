package doggo.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an overall journey and its itinerary Plans.
 */
public final class Trip {
    private final UUID id;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<Plan> plans;

    /**
     * Creates a Trip with the specified title and inclusive date range.
     *
     * @param id Trip identity.
     * @param title Trip title.
     * @param startDate Trip start date.
     * @param endDate Trip end date.
     */
    public Trip(UUID id, String title, LocalDate startDate, LocalDate endDate) {
        this(id, title, startDate, endDate, List.of());
    }

    private Trip(UUID id, String title, LocalDate startDate, LocalDate endDate, List<Plan> plans) {
        this.id = Objects.requireNonNull(id);
        this.title = requireText(title, "Trip title");
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = Objects.requireNonNull(endDate);
        this.plans = List.copyOf(plans);
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Trip end date cannot be before its start date.");
        }
        if (this.plans.stream().anyMatch(plan -> plan.date().isBefore(startDate)
                || plan.date().isAfter(endDate))) {
            throw new IllegalArgumentException("Plan date must fall within the Trip dates.");
        }
    }

    public UUID id() {
        return id;
    }

    public String title() {
        return title;
    }

    public LocalDate startDate() {
        return startDate;
    }

    public LocalDate endDate() {
        return endDate;
    }

    public List<Plan> plans() {
        return plans;
    }

    /**
     * Returns this Trip's status relative to the specified current date.
     * A Trip is current when the date falls within its inclusive date range.
     *
     * @param currentDate Date used to classify the Trip.
     * @return Trip status relative to the current date.
     */
    public TripStatus statusOn(LocalDate currentDate) {
        Objects.requireNonNull(currentDate);
        if (endDate.isBefore(currentDate)) {
            return TripStatus.PAST;
        }
        if (startDate.isAfter(currentDate)) {
            return TripStatus.FUTURE;
        }
        return TripStatus.CURRENT;
    }

    /**
     * Returns a copy with a Plan when its date falls within this Trip's inclusive date range.
     *
     * @param plan Plan to add.
     * @return Copy of this Trip containing the Plan.
     */
    public Trip withAddedPlan(Plan plan) {
        Objects.requireNonNull(plan);
        if (plan.date().isBefore(startDate) || plan.date().isAfter(endDate)) {
            throw new IllegalArgumentException("Plan date must fall within the Trip dates.");
        }
        List<Plan> updatedPlans = new ArrayList<>(plans);
        updatedPlans.add(plan);
        return new Trip(id, title, startDate, endDate, updatedPlans);
    }

    /**
     * Returns a copy without the Plan with the specified identity.
     *
     * @param planId Plan identity.
     * @return Copy of this Trip without the selected Plan.
     * @throws IllegalArgumentException If the Plan does not belong to this Trip.
     */
    public Trip withRemovedPlan(UUID planId) {
        Objects.requireNonNull(planId);
        List<Plan> updatedPlans = plans.stream()
                .filter(plan -> !plan.id().equals(planId))
                .toList();
        if (updatedPlans.size() == plans.size()) {
            throw new IllegalArgumentException("Plan not found.");
        }
        return new Trip(id, title, startDate, endDate, updatedPlans);
    }

    /**
     * Returns a copy with updated Trip details.
     *
     * @param updatedTitle Updated Trip title.
     * @param updatedStartDate Updated inclusive start date.
     * @param updatedEndDate Updated inclusive end date.
     * @return Copy of this Trip with updated details.
     */
    public Trip withUpdatedDetails(String updatedTitle, LocalDate updatedStartDate,
                                   LocalDate updatedEndDate) {
        return new Trip(id, updatedTitle, updatedStartDate, updatedEndDate, plans);
    }

    /**
     * Returns a copy with the Plan matching the replacement's identity replaced.
     *
     * @param replacement Replacement Plan.
     * @return Copy of this Trip with the replacement Plan.
     * @throws IllegalArgumentException If the replacement Plan does not belong to this Trip.
     */
    public Trip withReplacedPlan(Plan replacement) {
        Objects.requireNonNull(replacement);
        List<Plan> updatedPlans = new ArrayList<>(plans);
        for (int index = 0; index < updatedPlans.size(); index++) {
            if (updatedPlans.get(index).id().equals(replacement.id())) {
                if (replacement.date().isBefore(startDate)
                        || replacement.date().isAfter(endDate)) {
                    throw new IllegalArgumentException("Plan date must fall within the Trip dates.");
                }
                updatedPlans.set(index, replacement);
                return new Trip(id, title, startDate, endDate, updatedPlans);
            }
        }
        throw new IllegalArgumentException("Plan not found.");
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value.trim();
    }
}
