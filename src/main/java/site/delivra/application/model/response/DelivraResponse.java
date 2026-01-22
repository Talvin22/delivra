package com.post_hub.iam_service.model.response;

import com.post_hub.iam_service.model.constants.ApiMassage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IamResponse<P extends Serializable> implements Serializable {
    private String message;
    private P payload;
    private boolean success;


    public static <P extends Serializable> IamResponse<P> createSuccessful(P payload) {
        return new IamResponse<>(StringUtils.EMPTY, payload, true);
    }

    public static <P extends Serializable> IamResponse<P> createSuccessfulWithNewToken(P payload) {
        return new IamResponse<>(ApiMassage.TOKEN_CREATED_OR_UPDATED.getMessage(), payload, true);
    }
}
