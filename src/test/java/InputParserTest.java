import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class InputParserTest {
    @Test
    void parseDate_validDate_returnsDate() {
        assertEquals(LocalDate.of(2027, 1, 1), InputParser.parseDate("01/01/2027"));
    }

    @Test
    void parseDate_wrongFormat_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseDate("2027-01-01"));
    }

    @Test
    void parseDate_impossibleDate_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseDate("31/02/2027"));
    }
}
