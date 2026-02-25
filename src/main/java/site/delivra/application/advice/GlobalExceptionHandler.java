package site.delivra.application.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.delivra.application.exception.DataExistException;
import site.delivra.application.exception.HereApiException;
import site.delivra.application.exception.InvalidDataException;
import site.delivra.application.exception.InvalidPasswordException;
import site.delivra.application.exception.NotDriverException;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.model.response.DelivraResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public DelivraResponse<String> handleNotFound(NotFoundException ex) {
        return new DelivraResponse<>(ex.getMessage(), null, false);
    }

    @ExceptionHandler({InvalidDataException.class, DataExistException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public DelivraResponse<String> handleConflict(RuntimeException ex) {
        return new DelivraResponse<>(ex.getMessage(), null, false);
    }

    @ExceptionHandler({InvalidPasswordException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public DelivraResponse<String> handleBadRequest(RuntimeException ex) {
        return new DelivraResponse<>(ex.getMessage(), null, false);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public DelivraResponse<String> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .findFirst()
                .orElse("Validation failed");
        return new DelivraResponse<>(message, null, false);
    }

    @ExceptionHandler(NotDriverException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public DelivraResponse<String> handleForbidden(NotDriverException ex) {
        return new DelivraResponse<>(ex.getMessage(), null, false);
    }

    @ExceptionHandler(HereApiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public DelivraResponse<String> handleHereApi(HereApiException ex) {
        log.error("HERE API error", ex);
        return new DelivraResponse<>(ex.getMessage(), null, false);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public DelivraResponse<String> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return new DelivraResponse<>("An unexpected error occurred", null, false);
    }
}
