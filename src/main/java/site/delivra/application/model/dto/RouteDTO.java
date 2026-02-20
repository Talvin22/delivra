package site.delivra.application.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.delivra.application.utils.FlexiblePolylineDecoder;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteDTO implements Serializable {

    private String polyline;
    private Integer durationInSeconds;
    private Integer distanceInMeters;
    private List<FlexiblePolylineDecoder.Waypoint> waypoints;
    private List<RouteInstructionDTO> instructions;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RouteInstructionDTO implements Serializable {
        private String action;
        private String instruction;
        private Integer durationInSeconds;
        private Integer distanceInMeters;
    }
}
