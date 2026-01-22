package com.post_hub.iam_service.model.request.user;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest implements Serializable {

    @NotBlank(message = "username must be not blank")
    private String username;
    @NotBlank(message = "email must be not blank")
    private String email;
}
