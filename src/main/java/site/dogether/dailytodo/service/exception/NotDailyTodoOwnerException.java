package site.dogether.dailytodo.service.exception;

public class NotDailyTodoOwnerException extends RuntimeException {

    public NotDailyTodoOwnerException(final String message) {
        super(message);
    }
}
