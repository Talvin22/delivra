package site.delivra.application.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import site.delivra.application.model.dto.role.RoleDTO;
import site.delivra.application.model.enums.RegistrationStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class UserProfileDTO implements Serializable {

    private Integer id;
    private String username;
    private String email;

    private RegistrationStatus registrationStatus;
    private LocalDateTime lastLogin;

    private String token;
    private String refreshToken;
    private List<RoleDTO> roles;
}
