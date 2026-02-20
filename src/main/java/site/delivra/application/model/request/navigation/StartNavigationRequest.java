package site.delivra.application.model.request.navigation;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class StartNavigationRequest implements Serializable {

    @NotNull(message = "originLatitude cannot be null")
    private Double originLatitude;

    @NotNull(message = "originLongitude cannot be null")
    private Double originLongitude;

    private Integer grossWeight;
    private Integer height;
    private Integer width;
    private Integer length;
}
