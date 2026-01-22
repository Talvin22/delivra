package site.delivra.application.security.validation;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import site.delivra.application.exception.DataExistException;
import site.delivra.application.exception.InvalidDataException;
import site.delivra.application.exception.InvalidPasswordException;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.model.constants.ApiErrorMessage;
import site.delivra.application.model.entities.User;
import site.delivra.application.repository.UserRepository;
import site.delivra.application.service.model.DelivraServiceUserRole;
import site.delivra.application.utils.ApiUtils;
import site.delivra.application.utils.PasswordUtils;

import java.nio.file.AccessDeniedException;

@Component
@RequiredArgsConstructor
public class AccessValidator {
    private final UserRepository userRepository;
    private final ApiUtils apiUtils;

    public void validateNewUser(String username, String email, String password, String confirmPassword) {

        userRepository.findByUsername(username).ifPresent(user -> {
            throw new DataExistException(ApiErrorMessage.USERNAME_ALREADY_EXISTS.getMessage(username));
        });

        userRepository.findByEmail(email).ifPresent(user -> {
            throw new DataExistException(ApiErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(email));
        });

        if (!password.equals(confirmPassword)) {
            throw new InvalidDataException(ApiErrorMessage.MISMATCH_PASSWORDS.getMessage());
        }

        if (PasswordUtils.isNotValidPassword(password)) {
            throw new InvalidPasswordException(ApiErrorMessage.INVALID_PASSWORD.getMessage());
        }
    }

    public boolean isAdminOrSuperAdmin(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));

        return user.getRoles().stream().map(role -> DelivraServiceUserRole.fromName(role.getName())).anyMatch(role -> role == DelivraServiceUserRole.ADMIN || role == DelivraServiceUserRole.SUPER_ADMIN);
    }

    @SneakyThrows
    public void validateAdminOrOwnerAccess(Integer ownerId) {
        Integer currentUserId = apiUtils.getUserIdFromAuthentication();

        if (!currentUserId.equals(ownerId) && !isAdminOrSuperAdmin(currentUserId)) {
            throw new AccessDeniedException(ApiErrorMessage.HAVE_NO_ACCESS.getMessage());
        }


    }
}
