package site.dogether.dailytodo.exception;

public class NotCreatedTodayDailyTodoException extends DailyTodoException {

    public NotCreatedTodayDailyTodoException(final String message) {
        super(message);
    }
}
