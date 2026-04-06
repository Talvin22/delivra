package site.delivra.application.model.request.navigation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class StartNavigationRequest implements Serializable {

    @NotNull(message = "originLatitude cannot be null")
    @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "latitude must be <= 90")
    private Double originLatitude;

    @NotNull(message = "originLongitude cannot be null")
    @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "longitude must be <= 180")
    private Double originLongitude;

    private Integer grossWeight;
    private Integer height;
    private Integer width;
    private Integer length;
}
