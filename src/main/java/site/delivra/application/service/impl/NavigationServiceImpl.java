package site.delivra.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.delivra.application.exception.InvalidDataException;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.model.constants.ApiErrorMessage;
import site.delivra.application.model.dto.RouteDTO;
import site.delivra.application.model.dto.navigation.DriverPositionRequest;
import site.delivra.application.model.dto.navigation.NavigationEventDTO;
import site.delivra.application.model.dto.navigation.NavigationSessionDTO;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.entities.NavigationSession;
import site.delivra.application.model.enums.DeliveryTaskStatus;
import site.delivra.application.model.enums.NavigationSessionStatus;
import site.delivra.application.model.request.navigation.StartNavigationRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.repository.DeliveryTaskRepository;
import site.delivra.application.repository.NavigationSessionRepository;
import site.delivra.application.service.HereApiService;
import site.delivra.application.service.NavigationService;
import site.delivra.application.utils.FlexiblePolylineDecoder;
import site.delivra.application.utils.GeoUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NavigationServiceImpl implements NavigationService {

    private final NavigationSessionRepository sessionRepository;
    private final DeliveryTaskRepository taskRepository;
    private final HereApiService hereApiService;

    @Value("${navigation.off-route-threshold-meters:200}")
    private double offRouteThresholdMeters;

    private final Map<Integer, Long> lastRerouteBySession = new ConcurrentHashMap<>();
    private static final long REROUTE_COOLDOWN_MS = 30_000L;

    @Override
    @Transactional
    public DelivraResponse<NavigationSessionDTO> startNavigation(Integer taskId, StartNavigationRequest request) {
        DeliveryTask task = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.DELIVERY_NOT_FOUND_BY_ID.getMessage(taskId)));

        if (sessionRepository.existsByDeliveryTaskIdAndStatus(taskId, NavigationSessionStatus.ACTIVE)) {
            throw new InvalidDataException(ApiErrorMessage.NAVIGATION_SESSION_ALREADY_ACTIVE.getMessage(taskId));
        }

        if (task.getLatitude() == null || task.getLongitude() == null) {
            throw new InvalidDataException(ApiErrorMessage.TASK_MISSING_COORDINATES.getMessage(taskId));
        }

        RouteDTO route = null;
        try {
            route = hereApiService.calculateTruckRoute(
                    request.getOriginLatitude(), request.getOriginLongitude(),
                    task.getLatitude(), task.getLongitude(),
                    request.getGrossWeight(), request.getHeight(),
                    request.getWidth(), request.getLength()
            );
        } catch (Exception e) {
            log.warn("Route calculation failed for task {}, starting session without route: {}", taskId, e.getMessage());
        }

        // Mark task as IN_PROGRESS when navigation starts
        task.setStatus(DeliveryTaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        NavigationSession session = new NavigationSession();
        session.setDeliveryTask(task);
        session.setCurrentLatitude(request.getOriginLatitude());
        session.setCurrentLongitude(request.getOriginLongitude());
        if (route != null) session.setEncodedPolyline(route.getPolyline());
        session.setStatus(NavigationSessionStatus.ACTIVE);
        session = sessionRepository.save(session);

        log.info("Navigation started: sessionId={}, taskId={}", session.getId(), taskId);

        return DelivraResponse.createSuccessful(toDto(session, route));
    }

    @Override
    @Transactional
    public DelivraResponse<NavigationSessionDTO> endNavigation(Integer taskId) {
        NavigationSession session = sessionRepository
                .findByDeliveryTaskIdAndStatus(taskId, NavigationSessionStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(
                        ApiErrorMessage.NAVIGATION_SESSION_NOT_FOUND.getMessage(taskId)));

        session.setStatus(NavigationSessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        sessionRepository.save(session);
        lastRerouteBySession.remove(session.getId());

        log.info("Navigation ended: sessionId={}, taskId={}", session.getId(), taskId);
        return DelivraResponse.createSuccessful(toDto(session, null));
    }

    @Override
    public DelivraResponse<NavigationSessionDTO> getActiveSession(Integer taskId) {
        NavigationSession session = sessionRepository
                .findByDeliveryTaskIdAndStatus(taskId, NavigationSessionStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(
                        ApiErrorMessage.NAVIGATION_SESSION_NOT_FOUND.getMessage(taskId)));

        RouteDTO route = null;
        if (session.getEncodedPolyline() != null) {
            List<FlexiblePolylineDecoder.Waypoint> waypoints = FlexiblePolylineDecoder.decode(session.getEncodedPolyline());
            route = RouteDTO.builder()
                    .polyline(session.getEncodedPolyline())
                    .waypoints(waypoints)
                    .build();
        }

        return DelivraResponse.createSuccessful(toDto(session, route));
    }

    @Override
    @Transactional
    public NavigationEventDTO handlePositionUpdate(Integer sessionId, DriverPositionRequest positionRequest) {
        NavigationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException(
                        ApiErrorMessage.NAVIGATION_SESSION_NOT_FOUND.getMessage(sessionId)));

        if (session.getStatus() != NavigationSessionStatus.ACTIVE) {
            throw new InvalidDataException(
                    ApiErrorMessage.NAVIGATION_SESSION_NOT_ACTIVE.getMessage(sessionId));
        }

        double lat = positionRequest.getLatitude();
        double lng = positionRequest.getLongitude();

        session.setCurrentLatitude(lat);
        session.setCurrentLongitude(lng);
        sessionRepository.save(session);

        Integer taskId = session.getDeliveryTask().getId();

        if (session.getEncodedPolyline() == null) {
            return NavigationEventDTO.builder()
                    .type(NavigationEventDTO.Type.POSITION)
                    .taskId(taskId)
                    .latitude(lat)
                    .longitude(lng)
                    .onRoute(true)
                    .build();
        }

        List<FlexiblePolylineDecoder.Waypoint> waypoints = FlexiblePolylineDecoder.decode(session.getEncodedPolyline());
        double distanceFromRoute = GeoUtils.minDistanceToPolyline(lat, lng, waypoints);
        boolean onRoute = distanceFromRoute < offRouteThresholdMeters;


        if (onRoute) {
            log.debug("Driver on route: sessionId={}, dist={}m", sessionId, distanceFromRoute);
            return NavigationEventDTO.builder()
                    .type(NavigationEventDTO.Type.POSITION)
                    .taskId(taskId)
                    .latitude(lat)
                    .longitude(lng)
                    .onRoute(true)
                    .build();
        }

        long now = System.currentTimeMillis();
        Long lastReroute = lastRerouteBySession.get(sessionId);
        if (lastReroute != null && now - lastReroute < REROUTE_COOLDOWN_MS) {
            log.debug("Reroute cooldown active for session {}, skipping", sessionId);
            return NavigationEventDTO.builder()
                    .type(NavigationEventDTO.Type.POSITION)
                    .taskId(taskId)
                    .latitude(lat)
                    .longitude(lng)
                    .onRoute(false)
                    .build();
        }

        log.info("Driver off route: sessionId={}, dist={}m — recalculating", sessionId, distanceFromRoute);
        lastRerouteBySession.put(sessionId, now);

        try {
            DeliveryTask task = session.getDeliveryTask();
            RouteDTO newRoute = hereApiService.calculateTruckRoute(
                    lat, lng,
                    task.getLatitude(), task.getLongitude(),
                    null, null, null, null
            );

            session.setEncodedPolyline(newRoute.getPolyline());
            sessionRepository.save(session);

            return NavigationEventDTO.builder()
                    .type(NavigationEventDTO.Type.ROUTE_UPDATE)
                    .taskId(taskId)
                    .latitude(lat)
                    .longitude(lng)
                    .onRoute(false)
                    .route(newRoute)
                    .build();
        } catch (Exception e) {
            log.warn("Reroute failed for session {}: {}", sessionId, e.getMessage());
            return NavigationEventDTO.builder()
                    .type(NavigationEventDTO.Type.POSITION)
                    .taskId(taskId)
                    .latitude(lat)
                    .longitude(lng)
                    .onRoute(false)
                    .build();
        }
    }

    private NavigationSessionDTO toDto(NavigationSession session, RouteDTO route) {
        return NavigationSessionDTO.builder()
                .sessionId(session.getId())
                .taskId(session.getDeliveryTask().getId())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .route(route)
                .build();
    }
}
