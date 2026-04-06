package site.delivra.application.model.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiErrorMessage {
    USER_NOT_FOUND_BY_ID("User with ID: %s was not found"),
    USERNAME_ALREADY_EXISTS("User with username: %s already exists"),
    USERNAME_NOT_FOUND("User with username: %s was not found"),
    EMAIL_ALREADY_EXISTS("User with email: %s already exists"),
    EMAIL_NOT_FOUND("User with email: %s was not found"),
    USER_ROLE_NOT_FOUND("User role %s not found"),
    USER_NOT_DRIVER("User with ID: %s is not a driver"),
    INVALID_TOKEN_SIGNATURE("Invalid token signature"),
    DELIVERY_NOT_FOUND_BY_ID("Delivery with ID: %s was not found"),

    ERROR_DURING_JWT_PROCESSING("An unexpected error occurred during JWT processing"),
    TOKEN_EXPIRED("Token expired."),
    UNEXPECTED_ERROR_OCCURRED("An unexpected error occurred. Please try again later."),

    INVALID_USER_OR_PASSWORD("Invalid email or password. Try again"),
    INVALID_USER_REGISTRATION_STATUS("Invalid user registration status: %s."),
    NOT_FOUND_REFRESH_TOKEN("Refresh token not found."),

    MISMATCH_PASSWORDS("Password does not match"),
    INVALID_PASSWORD("Invalid password. It must have: "
            + "length at least " + ApiConstants.REQUIRED_MIN_PASSWORD_LENGTH + ", including "
            + ApiConstants.REQUIRED_MIN_LETTERS_NUMBER_EVERY_CASE_IN_PASSWORD + " letter(s) in upper and lower cases, "
            + ApiConstants.REQUIRED_MIN_CHARACTERS_NUMBER_IN_PASSWORD + " character(s), "
            + ApiConstants.REQUIRED_MIN_DIGITS_NUMBER_IN_PASSWORD + " digit(s)."),

    HAVE_NO_ACCESS("You don't have the necessary permissions to perform this action."),

    GEOCODING_FAILED("Failed to geocode address: %s"),
    GEOCODING_NO_RESULTS("No geocoding results found for address: %s"),
    ROUTING_FAILED("Failed to calculate route from (%s, %s) to (%s, %s)"),
    ROUTING_NO_RESULTS("No route found from (%s, %s) to (%s, %s)"),
    TASK_MISSING_COORDINATES("Delivery task with ID: %s has no coordinates for routing"),

    NAVIGATION_SESSION_NOT_FOUND("Navigation session with ID: %s not found"),
    NAVIGATION_SESSION_ALREADY_ACTIVE("Delivery task with ID: %s already has an active navigation session"),
    NAVIGATION_SESSION_NOT_ACTIVE("Navigation session with ID: %s is not active"),
    CHAT_MESSAGE_NOT_FOUND("Chat message with ID: %s not found"),

    INVALID_OR_EXPIRED_TOKEN("Invalid or expired token"),
    TOKEN_ALREADY_USED("This token has already been used")
    ;

    private final String message;

    public String getMessage(Object... args) {
        try {
            return (args == null || args.length == 0) ? message : String.format(message, args);
        } catch (Exception e) {
            return message;
        }
    }
}
