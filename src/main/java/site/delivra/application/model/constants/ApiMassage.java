package site.delivra.application.model.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiMassage {
    TOKEN_CREATED_OR_UPDATED("User's token has been created or updated! "),
    ;

    private final String  message;


}
