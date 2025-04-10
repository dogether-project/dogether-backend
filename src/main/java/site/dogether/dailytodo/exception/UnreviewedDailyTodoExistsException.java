package site.dogether.dailytodo.exception;

public class UnreviewedDailyTodoExistsException extends RuntimeException {

    public UnreviewedDailyTodoExistsException(final String message) {
        super(message);
    }
}
