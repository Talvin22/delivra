package site.delivra.application.model.dto.here;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HereGeocodingResponse {

    private List<GeocodingItem> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocodingItem {
        private String title;
        private Position position;
        private Address address;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Position {
        private Double lat;
        private Double lng;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String label;
    }
}
