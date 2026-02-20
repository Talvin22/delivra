package site.delivra.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import site.delivra.application.config.HereApiConfig;
import site.delivra.application.exception.HereApiException;
import site.delivra.application.model.constants.ApiErrorMessage;
import site.delivra.application.model.dto.RouteDTO;
import site.delivra.application.model.dto.here.HereGeocodingResponse;
import site.delivra.application.model.dto.here.HereRoutingResponse;
import site.delivra.application.service.HereApiService;
import site.delivra.application.utils.FlexiblePolylineDecoder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HereApiServiceImpl implements HereApiService {

    private final RestTemplate hereRestTemplate;
    private final HereApiConfig hereApiConfig;

    @Override
    public GeocodingResult geocodeAddress(String address) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(hereApiConfig.getGeocodingBaseUrl())
                .queryParam("q", address)
                .queryParam("apiKey", hereApiConfig.getApiKey())
                .build()
                .toUri();

        try {
            log.debug("Geocoding address: {}", address);
            HereGeocodingResponse response = hereRestTemplate.getForObject(uri, HereGeocodingResponse.class);

            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                throw new HereApiException(ApiErrorMessage.GEOCODING_NO_RESULTS.getMessage(address));
            }

            HereGeocodingResponse.GeocodingItem firstResult = response.getItems().get(0);
            HereGeocodingResponse.Position position = firstResult.getPosition();

            String resolvedAddress = Optional.ofNullable(firstResult.getAddress())
                    .map(HereGeocodingResponse.Address::getLabel)
                    .orElse(address);

            return new GeocodingResult(position.getLat(), position.getLng(), resolvedAddress);
        } catch (RestClientException e) {
            log.error("HERE Geocoding API call failed for address: {}", address, e);
            throw new HereApiException(ApiErrorMessage.GEOCODING_FAILED.getMessage(address), e);
        }
    }

    @Override
    public RouteDTO calculateTruckRoute(
            Double originLat, Double originLng,
            Double destLat, Double destLng,
            Integer grossWeight, Integer height,
            Integer width, Integer length
    ) {
        int effectiveWeight = Optional.ofNullable(grossWeight).orElse(hereApiConfig.getDefaultGrossWeight());
        int effectiveHeight = Optional.ofNullable(height).orElse(hereApiConfig.getDefaultHeight());
        int effectiveWidth = Optional.ofNullable(width).orElse(hereApiConfig.getDefaultWidth());
        int effectiveLength = Optional.ofNullable(length).orElse(hereApiConfig.getDefaultLength());

        URI uri = UriComponentsBuilder
                .fromHttpUrl(hereApiConfig.getRoutingBaseUrl())
                .queryParam("transportMode", "truck")
                .queryParam("origin", originLat + "," + originLng)
                .queryParam("destination", destLat + "," + destLng)
                .queryParam("return", "polyline,summary,actions,instructions")
                .queryParam("truck[grossWeight]", effectiveWeight)
                .queryParam("truck[height]", effectiveHeight)
                .queryParam("truck[width]", effectiveWidth)
                .queryParam("truck[length]", effectiveLength)
                .queryParam("apiKey", hereApiConfig.getApiKey())
                .build()
                .toUri();

        try {
            log.debug("Calculating truck route: ({},{}) -> ({},{})", originLat, originLng, destLat, destLng);
            HereRoutingResponse response = hereRestTemplate.getForObject(uri, HereRoutingResponse.class);

            if (response == null || response.getRoutes() == null || response.getRoutes().isEmpty()) {
                throw new HereApiException(
                        ApiErrorMessage.ROUTING_NO_RESULTS.getMessage(originLat, originLng, destLat, destLng));
            }

            return mapToRouteDTO(response.getRoutes().get(0));
        } catch (RestClientException e) {
            log.error("HERE Routing API call failed: ({},{}) -> ({},{})", originLat, originLng, destLat, destLng, e);
            throw new HereApiException(
                    ApiErrorMessage.ROUTING_FAILED.getMessage(originLat, originLng, destLat, destLng), e);
        }
    }

    private RouteDTO mapToRouteDTO(HereRoutingResponse.Route route) {
        StringBuilder polylineBuilder = new StringBuilder();
        int totalDuration = 0;
        int totalDistance = 0;
        List<RouteDTO.RouteInstructionDTO> allInstructions = new ArrayList<>();
        List<FlexiblePolylineDecoder.Waypoint> allWaypoints = new ArrayList<>();

        for (HereRoutingResponse.Section section : route.getSections()) {
            if (section.getPolyline() != null) {
                if (!polylineBuilder.isEmpty()) {
                    polylineBuilder.append(";");
                }
                polylineBuilder.append(section.getPolyline());
                allWaypoints.addAll(FlexiblePolylineDecoder.decode(section.getPolyline()));
            }

            if (section.getSummary() != null) {
                totalDuration += Optional.ofNullable(section.getSummary().getDuration()).orElse(0);
                totalDistance += Optional.ofNullable(section.getSummary().getLength()).orElse(0);
            }

            if (section.getActions() != null) {
                section.getActions().forEach(action ->
                        allInstructions.add(RouteDTO.RouteInstructionDTO.builder()
                                .action(action.getAction())
                                .instruction(action.getInstruction())
                                .durationInSeconds(action.getDuration())
                                .distanceInMeters(action.getLength())
                                .build()));
            }
        }

        return RouteDTO.builder()
                .polyline(polylineBuilder.toString())
                .durationInSeconds(totalDuration)
                .distanceInMeters(totalDistance)
                .waypoints(allWaypoints)
                .instructions(allInstructions)
                .build();
    }
}
