package site.delivra.application.model.dto.here;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HereRoutingResponse {

    private List<Route> routes;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {
        private List<Section> sections;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Section {
        private String polyline;
        private Summary summary;
        private List<Action> actions;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {
        private Integer duration;
        private Integer length;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Action {
        private String action;
        private String instruction;
        private Integer duration;
        private Integer length;
        private Integer offset;
    }
}
