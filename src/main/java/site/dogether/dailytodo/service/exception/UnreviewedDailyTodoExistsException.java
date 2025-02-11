package site.dogether.dailytodo.service.exception;

public class UnreviewedDailyTodoExistsException extends RuntimeException {

    public UnreviewedDailyTodoExistsException(final String message) {
        super(message);
    }
}
