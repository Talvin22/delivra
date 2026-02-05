package site.delivra.application.model.request.task;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryTaskRequest {
    private String address;

    private Double latitude;

    private Double longitude;

    private String status;
}
