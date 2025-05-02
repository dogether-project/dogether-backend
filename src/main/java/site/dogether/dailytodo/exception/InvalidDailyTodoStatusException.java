package site.dogether.dailytodo.exception;

public class InvalidDailyTodoStatusException extends RuntimeException {
  public InvalidDailyTodoStatusException(String message) {
    super(message);
  }
}
