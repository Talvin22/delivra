package site.delivra.application.model.dto.navigation;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverPositionRequest {

    @NotNull(message = "latitude cannot be null")
    private Double latitude;

    @NotNull(message = "longitude cannot be null")
    private Double longitude;
}
