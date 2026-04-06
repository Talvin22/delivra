package site.delivra.application.model.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationUserRequest implements Serializable {

    @NotBlank(message = "username cannot be empty")
    private String username;

    @Email
    @NotNull(message = "email cannot be empty")
    private String email;

    @NotEmpty(message = "password cannot be empty")
    @Size(max = 100, message = "password too long")
    private String password;

    @NotEmpty(message = "confirm password cannot be empty")
    @Size(max = 100, message = "confirm password too long")
    private String confirmPassword;
}
