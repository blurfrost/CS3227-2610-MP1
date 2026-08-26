import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

/**
 * Parses the strict date format used by the bare CLI.
 */
final class InputParser {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private InputParser() {
    }

    /**
     * Parses a date in exactly the DD/MM/YYYY format.
     *
     * @param value Text to parse.
     * @return Parsed date.
     * @throws IllegalArgumentException If the value is not a real date in the required format.
     */
    static LocalDate parseDate(String value) {
        return parse(value, "date", DATE_FORMATTER, LocalDate::from, "\\d{2}/\\d{2}/\\d{4}");
    }

    private static <T> T parse(String value, String name, DateTimeFormatter formatter,
                               Function<TemporalAccessor, T> converter,
                               String requiredPattern) {
        if (value == null || !value.trim().matches(requiredPattern)) {
            throw new IllegalArgumentException("Invalid " + name + " format.");
        }
        try {
            return converter.apply(formatter.parse(value.trim()));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid " + name + ".", exception);
        }
    }
}
