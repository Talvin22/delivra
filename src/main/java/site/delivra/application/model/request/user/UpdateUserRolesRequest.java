package site.delivra.application.model.request.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRolesRequest implements Serializable {

    @NotEmpty(message = "roles must not be empty")
    private List<String> roles;
}
