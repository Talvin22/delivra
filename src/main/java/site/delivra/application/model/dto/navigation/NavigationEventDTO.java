package site.delivra.application.model.dto.navigation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.delivra.application.model.dto.RouteDTO;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NavigationEventDTO implements Serializable {

    public enum Type {
        POSITION,
        ROUTE_UPDATE
    }

    private Type type;
    private Integer taskId;
    private Double latitude;
    private Double longitude;
    private boolean onRoute;

    /** Populated only when type == ROUTE_UPDATE */
    private RouteDTO route;
}
