package site.delivra.application.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.delivra.application.model.dto.role.RoleDTO;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchDTO implements Serializable {
    private Integer id;
    private String username;
    private String email;
    private Boolean deleted;

    private List<RoleDTO> roles;
}
