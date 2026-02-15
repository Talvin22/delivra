package site.delivra.application.exception;

public class HereApiException extends RuntimeException {

    public HereApiException(String message) {
        super(message);
    }

    public HereApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
