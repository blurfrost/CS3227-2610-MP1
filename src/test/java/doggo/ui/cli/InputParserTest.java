package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class InputParserTest {
    @Test
    void parseDate_validDate_returnsDate() {
        assertEquals(LocalDate.of(2027, 1, 1), InputParser.parseDate("01/01/2027"));
    }

    @Test
    void parseDate_surroundingWhitespace_returnsDate() {
        assertEquals(LocalDate.of(2027, 1, 1), InputParser.parseDate(" 01/01/2027 "));
    }

    @Test
    void parseDate_wrongFormat_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseDate("2027-01-01"));
    }

    @Test
    void parseDate_impossibleDate_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseDate("31/02/2027"));
    }

    @Test
    void parseDate_validLeapDay_returnsDate() {
        assertEquals(LocalDate.of(2028, 2, 29), InputParser.parseDate("29/02/2028"));
    }

    @Test
    void parseDate_invalidLeapDay_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseDate("29/02/2027"));
    }

    @Test
    void parseDate_invalidMonth_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseDate("01/13/2027"));
    }

    @Test
    void parseDate_singleDigitDay_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseDate("1/01/2027"));
    }

    @Test
    void parseTime_validTime_returnsTime() {
        assertEquals(LocalTime.of(9, 0), InputParser.parseTime("09:00"));
    }

    @Test
    void parseDateAndTime_nullValue_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseDate(null));
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseTime(null));
    }

    @Test
    void parseTime_wrongFormat_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseTime("9:00"));
    }

    @Test
    void parseTime_includesSeconds_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseTime("09:00:30"));
    }

    @Test
    void parseTime_invalidHour_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseTime("24:00"));
    }
}
