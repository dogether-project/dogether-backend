package site.dogether.dailytodo.exception;

public class NotDailyTodoOwnerException extends RuntimeException {

    public NotDailyTodoOwnerException(final String message) {
        super(message);
    }
}
