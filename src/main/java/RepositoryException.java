/**
 * Signals a failure while reading or writing Trip aggregates.
 */
final class RepositoryException extends RuntimeException {
    /**
     * Creates an exception with the specified message and cause.
     *
     * @param message Failure description.
     * @param cause Underlying infrastructure failure.
     */
    RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception with the specified message.
     *
     * @param message Failure description.
     */
    RepositoryException(String message) {
        super(message);
    }
}
