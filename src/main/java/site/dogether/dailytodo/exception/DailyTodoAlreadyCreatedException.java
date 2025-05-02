package site.dogether.dailytodo.exception;

public class DailyTodoAlreadyCreatedException extends RuntimeException {
    public DailyTodoAlreadyCreatedException(String message) {
        super(message);
    }
}
