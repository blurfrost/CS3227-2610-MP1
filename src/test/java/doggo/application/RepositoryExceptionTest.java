package doggo.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RepositoryExceptionTest {
    @Test
    void createException_messageOnly_storesMessageWithoutCause() {
        RepositoryException exception = new RepositoryException("Unable to read Trips.");

        assertEquals("Unable to read Trips.", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void createException_messageAndCause_preservesBoth() {
        Throwable cause = new IllegalStateException("Database unavailable.");

        RepositoryException exception = new RepositoryException("Unable to read Trips.", cause);

        assertEquals("Unable to read Trips.", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
