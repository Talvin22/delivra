package site.delivra.application.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import site.delivra.application.model.constants.ApiMassage;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DelivraResponse<P extends Serializable> implements Serializable {
    private String message;
    private P payload;
    private boolean success;


    public static <P extends Serializable> DelivraResponse<P> createSuccessful(P payload) {
        return new DelivraResponse<>(StringUtils.EMPTY, payload, true);
    }

    public static <P extends Serializable> DelivraResponse<P> createSuccessfulWithNewToken(P payload) {
        return new DelivraResponse<>(ApiMassage.TOKEN_CREATED_OR_UPDATED.getMessage(), payload, true);
    }
}
