package site.delivra.application.model.request.user;


import lombok.Data;
import site.delivra.application.model.enums.UserSortField;

import java.io.Serializable;

@Data
public class UserSearchRequest implements Serializable {

    private String username;
    private String email;

    private Boolean deleted;
    private String keyword;
    private UserSortField userSortField;


}
