package site.delivra.application.model.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiErrorMessage {
    POST_NOT_FOUND_BY_ID("Post with ID: %s was not found"),
    POST_ALREADY_EXISTS("Post with title: %s is  already exists"),
    USER_NOT_FOUND_BY_ID("User with ID: %s was not found"),
    USERNAME_ALREADY_EXISTS("User with username: %s already exists"),
    USERNAME_NOT_FOUND("User with username: %s was not found"),
    EMAIL_ALREADY_EXISTS("User with email: %s already exists"),
    EMAIL_NOT_FOUND("User with email: %s was not found"),
    USER_ROLE_NOT_FOUND("User role %s not found"),
    INVALID_TOKEN_SIGNATURE("Invalid token signature"),
    COMMENT_NOT_FOUND_BY_ID("Comment with ID: %s was not found"),

    ERROR_DURING_JWT_PROCESSING("An unexpected error occurred during JWT processing"),
    TOKEN_EXPIRED("Token expired."),
    UNEXPECTED_ERROR_OCCURRED("An unexpected error occurred. Please try again later."),

    AUTHENTICATION_FAILED_FOR_USER("Authentication failed for user: {}. "),
    INVALID_USER_OR_PASSWORD("Invalid email or password. Try again"),
    INVALID_USER_REGISTRATION_STATUS("Invalid user registration status: %s. "),
    NOT_FOUND_REFRESH_TOKEN("Refresh token not found."),

    UNEXPECTED_ERROR("An unexpected error occurred. Please try again later."),

    MISMATCH_PASSWORDS("Password does not match"),
    INVALID_PASSWORD("Invalid password. It must have: "
            + "length at least " + ApiConstants.REQUIRED_MIN_PASSWORD_LENGTH + ", including "
            + ApiConstants.REQUIRED_MIN_LETTERS_NUMBER_EVERY_CASE_IN_PASSWORD + " letter(s) in upper and lower cases, "
            + ApiConstants.REQUIRED_MIN_CHARACTERS_NUMBER_IN_PASSWORD + " character(s), "
            + ApiConstants.REQUIRED_MIN_DIGITS_NUMBER_IN_PASSWORD + " digit(s). "),

    HAVE_NO_ACCESS("You don't have the necessary permissions to perform this action.")
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
