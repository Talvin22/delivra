package site.delivra.application.model.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class LoginRequest implements Serializable {

    @Email
    @NotNull
    private String email;

    @NotEmpty
    @Size(max = 100, message = "password too long")
    private String password;

}
