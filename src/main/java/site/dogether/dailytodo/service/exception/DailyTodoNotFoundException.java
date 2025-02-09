package site.dogether.dailytodo.service.exception;

public class DailyTodoNotFoundException extends RuntimeException{

    public DailyTodoNotFoundException(final String message) {
        super(message);
    }
}
