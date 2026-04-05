package site.delivra.application.model.request.company;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyRegistrationRequest {

    @NotBlank
    @Size(max = 100)
    private String companyName;

    @NotBlank
    @Size(max = 30)
    private String adminUsername;

    @NotBlank
    @Email
    @Size(max = 50)
    private String adminEmail;

    @NotBlank
    @Size(min = 6, max = 80)
    private String adminPassword;

    @NotBlank
    private String confirmPassword;
}
