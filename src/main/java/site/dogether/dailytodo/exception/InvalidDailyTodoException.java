package site.dogether.dailytodo.exception;

public class InvalidDailyTodoException extends RuntimeException {

    public InvalidDailyTodoException(final String message) {
        super(message);
    }
}
