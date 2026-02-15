package site.delivra.application.model.request.task;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteRequest implements Serializable {

    private Integer taskId;

    @NotNull(message = "origin latitude cannot be null")
    private Double originLatitude;

    @NotNull(message = "origin longitude cannot be null")
    private Double originLongitude;

    private Integer grossWeight;
    private Integer height;
    private Integer width;
    private Integer length;
}
