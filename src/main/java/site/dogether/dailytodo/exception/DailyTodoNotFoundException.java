package site.dogether.dailytodo.exception;

public class DailyTodoNotFoundException extends RuntimeException{

    public DailyTodoNotFoundException(final String message) {
        super(message);
    }
}
