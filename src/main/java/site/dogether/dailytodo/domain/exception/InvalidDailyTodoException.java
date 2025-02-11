package site.dogether.dailytodo.domain.exception;

public class InvalidDailyTodoException extends RuntimeException {

    public InvalidDailyTodoException(final String message) {
        super(message);
    }
}
