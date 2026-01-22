package site.delivra.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DelivraServiceUserRole {

    USER("USER"),
    ADMIN("ADMIN"),
    SUPER_ADMIN("SUPER_ADMIN");

    private final String role;

    public static DelivraServiceUserRole fromName(String name) {
        return DelivraServiceUserRole.valueOf(name);
    }


}
