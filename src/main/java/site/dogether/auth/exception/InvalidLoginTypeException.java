package site.dogether.auth.exception;

public class InvalidLoginTypeException extends AuthException {

    public InvalidLoginTypeException(final String message) {
        super(message);
    }
}
