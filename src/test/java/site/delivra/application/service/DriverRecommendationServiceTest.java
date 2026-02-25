package site.delivra.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import site.delivra.application.model.constants.ApiErrorMessage;
import site.delivra.application.model.dto.recommendation.DriverRecommendationDTO;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.entities.NavigationSession;
import site.delivra.application.model.entities.User;
import site.delivra.application.model.enums.DeliveryTaskStatus;
import site.delivra.application.model.enums.NavigationSessionStatus;
import site.delivra.application.model.enums.RegistrationStatus;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.repository.DeliveryTaskRepository;
import site.delivra.application.repository.NavigationSessionRepository;
import site.delivra.application.repository.UserRepository;
import site.delivra.application.service.impl.DriverRecommendationServiceImpl;
import site.delivra.application.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverRecommendationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DeliveryTaskRepository deliveryTaskRepository;
    @Mock
    private NavigationSessionRepository navigationSessionRepository;

    @InjectMocks
    private DriverRecommendationServiceImpl service;

    private DeliveryTask task;
    private User driver1;
    private User driver2;

    @BeforeEach
    void setUp() {
        task = new DeliveryTask();
        task.setId(1);
        task.setLatitude(55.75);
        task.setLongitude(37.62);
        task.setStatus(DeliveryTaskStatus.PENDING);
        task.setDeleted(false);

        driver1 = new User();
        driver1.setId(10);
        driver1.setUsername("driver_near");
        driver1.setEmail("near@example.com");
        driver1.setStatus(RegistrationStatus.ACTIVE);
        driver1.setDeleted(false);

        driver2 = new User();
        driver2.setId(11);
        driver2.setUsername("driver_far");
        driver2.setEmail("far@example.com");
        driver2.setStatus(RegistrationStatus.ACTIVE);
        driver2.setDeleted(false);
    }

    @Test
    void recommendDrivers_taskNotFound_throwsNotFoundException() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.recommendDrivers(99, 5));
    }

    @Test
    void recommendDrivers_noAvailableDrivers_returnsEmptyList() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findAvailableDrivers()).thenReturn(List.of());

        DelivraResponse<List<DriverRecommendationDTO>> result = service.recommendDrivers(1, 5);

        assertTrue(result.getPayload().isEmpty());
        assertTrue(result.isSuccess());
    }

    @Test
    void recommendDrivers_nearerDriverRankedHigher() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findAvailableDrivers()).thenReturn(List.of(driver1, driver2));

        NavigationSession nearSession = buildSession(driver1, 55.76, 37.63, 1);
        NavigationSession farSession = buildSession(driver2, 51.50, -0.12, 1);

        when(navigationSessionRepository.findLastCompletedSessionsForDriver(eq(10), any()))
                .thenReturn(List.of(nearSession));
        when(navigationSessionRepository.findLastCompletedSessionsForDriver(eq(11), any()))
                .thenReturn(List.of(farSession));

        when(deliveryTaskRepository.countTasksByStatusForDriver(anyInt())).thenReturn(List.of());
        when(deliveryTaskRepository.countPendingTasksForDriver(anyInt(), any())).thenReturn(0L);

        DelivraResponse<List<DriverRecommendationDTO>> result = service.recommendDrivers(1, 5);

        List<DriverRecommendationDTO> ranked = result.getPayload();
        assertEquals(2, ranked.size());
        assertEquals(driver1.getId(), ranked.get(0).getDriverId(),
                "Nearer driver should be ranked first");
        assertTrue(ranked.get(0).getTotalScore() > ranked.get(1).getTotalScore());
    }

    @Test
    void recommendDrivers_limitApplied() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findAvailableDrivers()).thenReturn(List.of(driver1, driver2));

        when(navigationSessionRepository.findLastCompletedSessionsForDriver(anyInt(), any()))
                .thenReturn(List.of());
        when(deliveryTaskRepository.countTasksByStatusForDriver(anyInt())).thenReturn(List.of());
        when(deliveryTaskRepository.countPendingTasksForDriver(anyInt(), any())).thenReturn(0L);

        DelivraResponse<List<DriverRecommendationDTO>> result = service.recommendDrivers(1, 1);

        assertEquals(1, result.getPayload().size());
    }

    @Test
    void recommendDrivers_driverWithLessWorkloadScoresHigher() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findAvailableDrivers()).thenReturn(List.of(driver1, driver2));

        when(navigationSessionRepository.findLastCompletedSessionsForDriver(anyInt(), any()))
                .thenReturn(List.of());

        when(deliveryTaskRepository.countTasksByStatusForDriver(anyInt())).thenReturn(List.of());
        when(deliveryTaskRepository.countPendingTasksForDriver(eq(10), any())).thenReturn(0L);
        when(deliveryTaskRepository.countPendingTasksForDriver(eq(11), any())).thenReturn(5L);

        DelivraResponse<List<DriverRecommendationDTO>> result = service.recommendDrivers(1, 5);

        List<DriverRecommendationDTO> ranked = result.getPayload();
        assertEquals(driver1.getId(), ranked.get(0).getDriverId(),
                "Driver with no pending tasks should rank higher");
    }

    @Test
    void recommendDrivers_taskWithoutCoordinates_proximityScoreIsZeroForAll() {
        task.setLatitude(null);
        task.setLongitude(null);

        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findAvailableDrivers()).thenReturn(List.of(driver1));

        when(navigationSessionRepository.findLastCompletedSessionsForDriver(anyInt(), any()))
                .thenReturn(List.of());
        when(deliveryTaskRepository.countTasksByStatusForDriver(anyInt())).thenReturn(List.of());
        when(deliveryTaskRepository.countPendingTasksForDriver(anyInt(), any())).thenReturn(0L);

        DelivraResponse<List<DriverRecommendationDTO>> result = service.recommendDrivers(1, 5);

        DriverRecommendationDTO dto = result.getPayload().get(0);
        assertEquals(0.0, dto.getProximityScore(), 0.001);
        assertNull(dto.getDistanceMeters());
    }

    @Test
    void recommendDrivers_responseIsSuccessful() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findAvailableDrivers()).thenReturn(List.of());

        DelivraResponse<List<DriverRecommendationDTO>> result = service.recommendDrivers(1, 5);

        assertTrue(result.isSuccess());
        assertNotNull(result.getPayload());
    }

    private NavigationSession buildSession(User driver, double lat, double lng, int hoursAgo) {
        DeliveryTask dt = new DeliveryTask();
        dt.setUser(driver);

        NavigationSession session = new NavigationSession();
        session.setDeliveryTask(dt);
        session.setCurrentLatitude(lat);
        session.setCurrentLongitude(lng);
        session.setStatus(NavigationSessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now().minusHours(hoursAgo));
        return session;
    }
}
