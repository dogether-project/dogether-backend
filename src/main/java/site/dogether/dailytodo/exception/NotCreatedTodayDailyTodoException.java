package site.dogether.dailytodo.exception;

public class NotCreatedTodayDailyTodoException extends RuntimeException {

    public NotCreatedTodayDailyTodoException(final String message) {
        super(message);
    }
}
