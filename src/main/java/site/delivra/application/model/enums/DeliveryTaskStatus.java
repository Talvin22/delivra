package site.delivra.application.model.enums;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum DeliveryTaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELED
}
