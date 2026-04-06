package site.delivra.application.model.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 6, max = 100)
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}
