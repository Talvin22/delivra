package site.delivra.application.model.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum NavigationSessionStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}
