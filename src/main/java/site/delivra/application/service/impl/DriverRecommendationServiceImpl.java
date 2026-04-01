package site.delivra.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.model.constants.ApiErrorMessage;
import site.delivra.application.model.dto.recommendation.DriverRecommendationDTO;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.entities.NavigationSession;
import site.delivra.application.model.entities.User;
import site.delivra.application.model.enums.DeliveryTaskStatus;
import site.delivra.application.model.enums.NavigationSessionStatus;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.repository.DeliveryTaskRepository;
import site.delivra.application.repository.NavigationSessionRepository;
import site.delivra.application.repository.UserRepository;
import site.delivra.application.service.DriverRecommendationService;
import site.delivra.application.utils.GeoUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverRecommendationServiceImpl implements DriverRecommendationService {

    private static final double WEIGHT_PROXIMITY = 0.40;
    private static final double WEIGHT_WORKLOAD = 0.30;
    private static final double WEIGHT_SUCCESS_RATE = 0.20;
    private static final double WEIGHT_RECENCY = 0.10;

    private static final long MAX_INACTIVE_HOURS = 720;

    private final UserRepository userRepository;
    private final DeliveryTaskRepository deliveryTaskRepository;
    private final NavigationSessionRepository navigationSessionRepository;

    @Override
    public DelivraResponse<ArrayList<DriverRecommendationDTO>> recommendDrivers(Integer taskId, int limit) {
        DeliveryTask task = deliveryTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.DELIVERY_NOT_FOUND_BY_ID.getMessage(taskId)));

        List<User> availableDrivers = userRepository.findAvailableDrivers();
        log.debug("Found {} available drivers for task {}", availableDrivers.size(), taskId);

        if (availableDrivers.isEmpty()) {
            return DelivraResponse.createSuccessful(new ArrayList<>());
        }

        List<RawMetrics> metrics = availableDrivers.stream()
                .map(driver -> collectMetrics(driver, task))
                .toList();

        ArrayList<DriverRecommendationDTO> result = applyWeightedScoring(metrics).stream()
                .sorted(Comparator.comparingDouble(DriverRecommendationDTO::getTotalScore).reversed())
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));

        return DelivraResponse.createSuccessful(result);
    }

    private static final List<DeliveryTaskStatus> ACTIVE_STATUSES =
            List.of(DeliveryTaskStatus.PENDING, DeliveryTaskStatus.IN_PROGRESS);

    private RawMetrics collectMetrics(User driver, DeliveryTask task) {
        Double distanceMeters = resolveDriverDistance(driver, task);
        long activeCount = deliveryTaskRepository.countActiveTasksForDriver(driver.getId(), ACTIVE_STATUSES);
        boolean busy = deliveryTaskRepository.countActiveTasksForDriver(
                driver.getId(), List.of(DeliveryTaskStatus.IN_PROGRESS)) > 0;
        double successRate = computeSuccessRate(driver.getId());
        Long hoursSinceActivity = resolveHoursSinceLastActivity(driver);

        return new RawMetrics(driver, distanceMeters, activeCount, busy, successRate, hoursSinceActivity);
    }

    private Double resolveDriverDistance(User driver, DeliveryTask task) {
        if (task.getLatitude() == null || task.getLongitude() == null) {
            return null;
        }
        List<NavigationSession> sessions = navigationSessionRepository
                .findLastCompletedSessionsForDriver(driver.getId(), PageRequest.of(0, 1));
        if (sessions.isEmpty()) {
            return null;
        }
        NavigationSession last = sessions.get(0);
        if (last.getCurrentLatitude() == null || last.getCurrentLongitude() == null) {
            return null;
        }
        return GeoUtils.distanceMeters(
                last.getCurrentLatitude(), last.getCurrentLongitude(),
                task.getLatitude(), task.getLongitude());
    }

    private double computeSuccessRate(Integer driverId) {
        List<Object[]> counts = deliveryTaskRepository.countTasksByStatusForDriver(driverId);
        Map<String, Long> byStatus = counts.stream()
                .collect(Collectors.toMap(
                        row -> ((DeliveryTaskStatus) row[0]).name(),
                        row -> (Long) row[1]));
        long completed = byStatus.getOrDefault(DeliveryTaskStatus.COMPLETED.name(), 0L);
        long canceled = byStatus.getOrDefault(DeliveryTaskStatus.CANCELED.name(), 0L);
        long total = completed + canceled;
        return total == 0 ? 1.0 : (double) completed / total;
    }

    private Long resolveHoursSinceLastActivity(User driver) {
        List<NavigationSession> sessions = navigationSessionRepository
                .findLastCompletedSessionsForDriver(driver.getId(), PageRequest.of(0, 1));
        if (sessions.isEmpty() || sessions.get(0).getEndedAt() == null) {
            return null;
        }
        return ChronoUnit.HOURS.between(sessions.get(0).getEndedAt(), LocalDateTime.now());
    }

    private List<DriverRecommendationDTO> applyWeightedScoring(List<RawMetrics> allMetrics) {
        double maxDistance = allMetrics.stream()
                .map(RawMetrics::distanceMeters)
                .filter(d -> d != null)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);

        long maxActive = allMetrics.stream()
                .mapToLong(RawMetrics::activeCount)
                .max()
                .orElse(0L);

        List<DriverRecommendationDTO> result = new ArrayList<>();
        for (RawMetrics m : allMetrics) {
            double proximityScore = computeProximityScore(m.distanceMeters(), maxDistance);
            double workloadScore = computeWorkloadScore(m.activeCount(), maxActive);
            double successRateScore = m.successRate();
            double recencyScore = computeRecencyScore(m.hoursSinceActivity());

            double totalScore = (WEIGHT_PROXIMITY * proximityScore
                    + WEIGHT_WORKLOAD * workloadScore
                    + WEIGHT_SUCCESS_RATE * successRateScore
                    + WEIGHT_RECENCY * recencyScore) * 100.0;

            result.add(DriverRecommendationDTO.builder()
                    .driverId(m.driver().getId())
                    .driverUsername(m.driver().getUsername())
                    .driverEmail(m.driver().getEmail())
                    .busy(m.busy())
                    .totalScore(Math.round(totalScore * 100.0) / 100.0)
                    .proximityScore(Math.round(proximityScore * 10000.0) / 100.0)
                    .workloadScore(Math.round(workloadScore * 10000.0) / 100.0)
                    .successRateScore(Math.round(successRateScore * 10000.0) / 100.0)
                    .recencyScore(Math.round(recencyScore * 10000.0) / 100.0)
                    .distanceMeters(m.distanceMeters())
                    .pendingTasksCount(m.activeCount())
                    .successRate(Math.round(m.successRate() * 10000.0) / 10000.0)
                    .hoursSinceLastActivity(m.hoursSinceActivity())
                    .build());
        }
        return result;
    }

    private double computeProximityScore(Double distanceMeters, double maxDistance) {
        if (distanceMeters == null) {
            return 0.0;
        }
        if (maxDistance == 0.0) {
            return 1.0;
        }
        return Math.max(0.0, 1.0 - (distanceMeters / maxDistance));
    }

    private double computeWorkloadScore(long activeCount, long maxActive) {
        if (maxActive == 0) {
            return 1.0;
        }
        return 1.0 - ((double) activeCount / maxActive);
    }

    private double computeRecencyScore(Long hoursSinceActivity) {
        if (hoursSinceActivity == null) {
            return 0.1;
        }
        return Math.max(0.0, 1.0 - ((double) hoursSinceActivity / MAX_INACTIVE_HOURS));
    }

    private record RawMetrics(
            User driver,
            Double distanceMeters,
            long activeCount,
            boolean busy,
            double successRate,
            Long hoursSinceActivity) {
    }
}
