package site.delivra.application.model.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiLogMassage {
    POST_INFO_BY_ID("Receiving post with id: {}"),
    NAME_OF_CURRENT_METHOD("Current method: {}"),
    ;
    private final String value;

}
