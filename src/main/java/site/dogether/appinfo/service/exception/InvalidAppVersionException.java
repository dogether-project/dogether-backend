package site.dogether.appinfo.service.exception;

public class InvalidAppVersionException extends RuntimeException {
    public InvalidAppVersionException(String message) {
        super(message);
    }
}
