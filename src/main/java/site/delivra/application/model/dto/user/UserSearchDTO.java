package com.post_hub.iam_service.model.dto.user;

import com.post_hub.iam_service.model.dto.role.RoleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
