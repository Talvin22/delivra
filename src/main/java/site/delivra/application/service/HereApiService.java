package site.delivra.application.service;

import site.delivra.application.model.dto.RouteDTO;

public interface HereApiService {

    GeocodingResult geocodeAddress(String address);

    RouteDTO calculateTruckRoute(
            Double originLat, Double originLng,
            Double destLat, Double destLng,
            Integer grossWeight, Integer height,
            Integer width, Integer length
    );

    record GeocodingResult(Double latitude, Double longitude, String resolvedAddress) {}
}
