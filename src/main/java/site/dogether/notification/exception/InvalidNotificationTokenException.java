package site.dogether.notification.exception;

public class InvalidNotificationTokenException extends RuntimeException {

    public InvalidNotificationTokenException(final String message) {
        super(message);
    }
}
