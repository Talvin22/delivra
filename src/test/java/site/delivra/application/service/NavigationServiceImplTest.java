package site.delivra.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import site.delivra.application.exception.InvalidDataException;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.model.dto.RouteDTO;
import site.delivra.application.model.dto.navigation.DriverPositionRequest;
import site.delivra.application.model.dto.navigation.NavigationEventDTO;
import site.delivra.application.model.dto.navigation.NavigationSessionDTO;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.entities.NavigationSession;
import site.delivra.application.model.entities.User;
import site.delivra.application.model.enums.DeliveryTaskStatus;
import site.delivra.application.model.enums.NavigationSessionStatus;
import site.delivra.application.model.request.navigation.StartNavigationRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.repository.DeliveryTaskRepository;
import site.delivra.application.repository.NavigationSessionRepository;
import site.delivra.application.repository.UserRepository;
import site.delivra.application.service.impl.NavigationServiceImpl;
import site.delivra.application.utils.ApiUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NavigationServiceImplTest {

    @Mock private NavigationSessionRepository sessionRepository;
    @Mock private DeliveryTaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private HereApiService hereApiService;
    @Mock private ApiUtils apiUtils;

    @InjectMocks
    private NavigationServiceImpl service;

    private DeliveryTask task;
    private User driver;
    private StartNavigationRequest startRequest;

    @BeforeEach
    void setUp() {
        driver = new User();
        driver.setId(10);
        driver.setUsername("driver");
        driver.setDeleted(false);

        task = new DeliveryTask();
        task.setId(1);
        task.setAddress("Some Address");
        task.setLatitude(55.76);
        task.setLongitude(37.63);
        task.setStatus(DeliveryTaskStatus.PENDING);
        task.setUser(driver);
        task.setDeleted(false);

        startRequest = new StartNavigationRequest();
        startRequest.setOriginLatitude(55.75);
        startRequest.setOriginLongitude(37.62);

        ReflectionTestUtils.setField(service, "offRouteThresholdMeters", 200.0);
        ReflectionTestUtils.setField(service, "rerouteCooldownSeconds", 30L);
    }

    @Test
    void startNavigation_taskNotFound_throwsNotFoundException() {
        when(taskRepository.findByIdAndDeletedFalse(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.startNavigation(99, startRequest));
    }

    @Test
    void startNavigation_notTaskOwner_throwsInvalidDataException() {
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(apiUtils.getUserIdFromAuthentication()).thenReturn(999);

        assertThrows(InvalidDataException.class, () -> service.startNavigation(1, startRequest));
    }

    @Test
    void startNavigation_taskHasNoDriver_throwsInvalidDataException() {
        task.setUser(null);
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(apiUtils.getUserIdFromAuthentication()).thenReturn(10);

        assertThrows(InvalidDataException.class, () -> service.startNavigation(1, startRequest));
    }

    @Test
    void startNavigation_alreadyActiveSession_throwsInvalidDataException() {
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(apiUtils.getUserIdFromAuthentication()).thenReturn(10);
        when(sessionRepository.existsByDeliveryTaskIdAndStatus(1, NavigationSessionStatus.ACTIVE))
                .thenReturn(true);

        InvalidDataException ex = assertThrows(InvalidDataException.class,
                () -> service.startNavigation(1, startRequest));
        assertTrue(ex.getMessage().toLowerCase().contains("active navigation session"));
    }

    @Test
    void startNavigation_missingCoordinates_geocodesOnTheFly() {
        task.setLatitude(null);
        task.setLongitude(null);
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(apiUtils.getUserIdFromAuthentication()).thenReturn(10);
        when(sessionRepository.existsByDeliveryTaskIdAndStatus(1, NavigationSessionStatus.ACTIVE))
                .thenReturn(false);
        when(hereApiService.geocodeAddress(task.getAddress()))
                .thenReturn(new HereApiService.GeocodingResult(55.80, 37.70, "resolved"));
        when(userRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(driver));
        when(hereApiService.calculateTruckRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                any(), any(), any(), any()))
                .thenReturn(RouteDTO.builder().polyline("poly").build());
        when(sessionRepository.save(any(NavigationSession.class)))
                .thenAnswer(inv -> {
                    NavigationSession s = inv.getArgument(0);
                    s.setId(100);
                    return s;
                });

        DelivraResponse<NavigationSessionDTO> response = service.startNavigation(1, startRequest);

        assertTrue(response.isSuccess());
        assertEquals(55.80, task.getLatitude());
        verify(hereApiService).geocodeAddress("Some Address");
    }

    @Test
    void startNavigation_geocodingFails_throwsInvalidDataException() {
        task.setLatitude(null);
        task.setLongitude(null);
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(apiUtils.getUserIdFromAuthentication()).thenReturn(10);
        when(sessionRepository.existsByDeliveryTaskIdAndStatus(1, NavigationSessionStatus.ACTIVE))
                .thenReturn(false);
        when(hereApiService.geocodeAddress(task.getAddress()))
                .thenThrow(new RuntimeException("geocode failed"));

        InvalidDataException ex = assertThrows(InvalidDataException.class,
                () -> service.startNavigation(1, startRequest));
        assertTrue(ex.getMessage().toLowerCase().contains("no coordinates"));
    }

    @Test
    void startNavigation_routingFails_sessionStillCreatedWithoutRoute() {
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(apiUtils.getUserIdFromAuthentication()).thenReturn(10);
        when(sessionRepository.existsByDeliveryTaskIdAndStatus(1, NavigationSessionStatus.ACTIVE))
                .thenReturn(false);
        when(userRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(driver));
        when(hereApiService.calculateTruckRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                any(), any(), any(), any()))
                .thenThrow(new RuntimeException("routing failed"));
        when(sessionRepository.save(any(NavigationSession.class)))
                .thenAnswer(inv -> {
                    NavigationSession s = inv.getArgument(0);
                    s.setId(200);
                    return s;
                });

        DelivraResponse<NavigationSessionDTO> response = service.startNavigation(1, startRequest);

        assertTrue(response.isSuccess());
        assertNotNull(response.getPayload());
        assertEquals(DeliveryTaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test
    void startNavigation_usesDriverTruckProfileWhenRequestFieldsNull() {
        driver.setTruckGrossWeight(7000);
        driver.setTruckHeight(380);
        driver.setTruckWidth(240);
        driver.setTruckLength(1100);

        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(apiUtils.getUserIdFromAuthentication()).thenReturn(10);
        when(sessionRepository.existsByDeliveryTaskIdAndStatus(1, NavigationSessionStatus.ACTIVE))
                .thenReturn(false);
        when(userRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(driver));
        when(hereApiService.calculateTruckRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                eq(7000), eq(380), eq(240), eq(1100)))
                .thenReturn(RouteDTO.builder().polyline("p").build());
        when(sessionRepository.save(any(NavigationSession.class)))
                .thenAnswer(inv -> {
                    NavigationSession s = inv.getArgument(0);
                    s.setId(1);
                    return s;
                });

        service.startNavigation(1, startRequest);

        verify(hereApiService).calculateTruckRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                eq(7000), eq(380), eq(240), eq(1100));
    }

    @Test
    void endNavigation_sessionNotFound_throwsNotFoundException() {
        when(sessionRepository.findByDeliveryTaskIdAndStatus(1, NavigationSessionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.endNavigation(1));
    }

    @Test
    void endNavigation_notOwner_throwsInvalidDataException() {
        NavigationSession session = new NavigationSession();
        session.setId(5);
        session.setStatus(NavigationSessionStatus.ACTIVE);
        session.setDeliveryTask(task);
        when(sessionRepository.findByDeliveryTaskIdAndStatus(1, NavigationSessionStatus.ACTIVE))
                .thenReturn(Optional.of(session));
        when(apiUtils.getUserIdFromAuthentication()).thenReturn(999);

        assertThrows(InvalidDataException.class, () -> service.endNavigation(1));
    }

    @Test
    void endNavigation_success_marksSessionCompleted() {
        NavigationSession session = new NavigationSession();
        session.setId(5);
        session.setStatus(NavigationSessionStatus.ACTIVE);
        session.setDeliveryTask(task);
        when(sessionRepository.findByDeliveryTaskIdAndStatus(1, NavigationSessionStatus.ACTIVE))
                .thenReturn(Optional.of(session));
        when(apiUtils.getUserIdFromAuthentication()).thenReturn(10);

        DelivraResponse<NavigationSessionDTO> response = service.endNavigation(1);

        assertTrue(response.isSuccess());
        assertEquals(NavigationSessionStatus.COMPLETED, session.getStatus());
        assertNotNull(session.getEndedAt());
    }

    @Test
    void handlePositionUpdate_sessionNotFound_throwsNotFoundException() {
        when(sessionRepository.findById(77)).thenReturn(Optional.empty());
        DriverPositionRequest req = new DriverPositionRequest();
        req.setLatitude(55.75);
        req.setLongitude(37.62);

        assertThrows(NotFoundException.class, () -> service.handlePositionUpdate(77, req));
    }

    @Test
    void handlePositionUpdate_sessionNotActive_throwsInvalidDataException() {
        NavigationSession session = new NavigationSession();
        session.setId(5);
        session.setStatus(NavigationSessionStatus.COMPLETED);
        session.setDeliveryTask(task);
        when(sessionRepository.findById(5)).thenReturn(Optional.of(session));
        DriverPositionRequest req = new DriverPositionRequest();
        req.setLatitude(55.75);
        req.setLongitude(37.62);

        assertThrows(InvalidDataException.class, () -> service.handlePositionUpdate(5, req));
    }

    @Test
    void handlePositionUpdate_noPolyline_returnsPositionEvent() {
        NavigationSession session = new NavigationSession();
        session.setId(5);
        session.setStatus(NavigationSessionStatus.ACTIVE);
        session.setDeliveryTask(task);
        session.setEncodedPolyline(null);
        when(sessionRepository.findById(5)).thenReturn(Optional.of(session));
        DriverPositionRequest req = new DriverPositionRequest();
        req.setLatitude(55.75);
        req.setLongitude(37.62);

        NavigationEventDTO event = service.handlePositionUpdate(5, req);

        assertEquals(NavigationEventDTO.Type.POSITION, event.getType());
        assertTrue(event.isOnRoute());
        assertEquals(task.getId(), event.getTaskId());
        verify(hereApiService, never()).calculateTruckRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                any(), any(), any(), any());
    }

    @Test
    void handlePositionUpdate_driverFarOffRoute_triggersReroute() {
        NavigationSession session = new NavigationSession();
        session.setId(5);
        session.setStatus(NavigationSessionStatus.ACTIVE);
        session.setDeliveryTask(task);
        session.setEncodedPolyline("BFoz5xJ67i1B1B7PzIhaxL7Y");
        when(sessionRepository.findById(5)).thenReturn(Optional.of(session));
        when(hereApiService.calculateTruckRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                any(), any(), any(), any()))
                .thenReturn(RouteDTO.builder().polyline("new-poly").build());

        DriverPositionRequest req = new DriverPositionRequest();
        req.setLatitude(-33.87);
        req.setLongitude(151.21);

        NavigationEventDTO event = service.handlePositionUpdate(5, req);

        assertEquals(NavigationEventDTO.Type.ROUTE_UPDATE, event.getType());
        assertFalse(event.isOnRoute());
        assertNotNull(event.getRoute());
        assertEquals("new-poly", event.getRoute().getPolyline());
    }

    @Test
    void handlePositionUpdate_rerouteCooldownActive_skipsReroute() {
        NavigationSession session = new NavigationSession();
        session.setId(5);
        session.setStatus(NavigationSessionStatus.ACTIVE);
        session.setDeliveryTask(task);
        session.setEncodedPolyline("BFoz5xJ67i1B1B7PzIhaxL7Y");
        when(sessionRepository.findById(5)).thenReturn(Optional.of(session));
        when(hereApiService.calculateTruckRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                any(), any(), any(), any()))
                .thenReturn(RouteDTO.builder().polyline("new-poly").build());

        DriverPositionRequest req = new DriverPositionRequest();
        req.setLatitude(-33.87);
        req.setLongitude(151.21);

        // first call triggers reroute
        service.handlePositionUpdate(5, req);
        // second call within cooldown should not reroute
        NavigationEventDTO event = service.handlePositionUpdate(5, req);

        assertEquals(NavigationEventDTO.Type.POSITION, event.getType());
        assertFalse(event.isOnRoute());
    }

    @Test
    void handlePositionUpdate_rerouteApiFails_returnsPositionEvent() {
        NavigationSession session = new NavigationSession();
        session.setId(5);
        session.setStatus(NavigationSessionStatus.ACTIVE);
        session.setDeliveryTask(task);
        session.setEncodedPolyline("BFoz5xJ67i1B1B7PzIhaxL7Y");
        when(sessionRepository.findById(5)).thenReturn(Optional.of(session));
        when(hereApiService.calculateTruckRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                any(), any(), any(), any()))
                .thenThrow(new RuntimeException("HERE down"));

        DriverPositionRequest req = new DriverPositionRequest();
        req.setLatitude(-33.87);
        req.setLongitude(151.21);

        NavigationEventDTO event = service.handlePositionUpdate(5, req);

        assertEquals(NavigationEventDTO.Type.POSITION, event.getType());
        assertFalse(event.isOnRoute());
    }

    @Test
    void getActiveSession_notFound_throwsNotFoundException() {
        when(sessionRepository.findByDeliveryTaskIdAndStatus(1, NavigationSessionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getActiveSession(1));
    }

    @Test
    void getActiveSession_notOwner_throwsInvalidDataException() {
        NavigationSession session = new NavigationSession();
        session.setId(5);
        session.setStatus(NavigationSessionStatus.ACTIVE);
        session.setDeliveryTask(task);
        when(sessionRepository.findByDeliveryTaskIdAndStatus(1, NavigationSessionStatus.ACTIVE))
                .thenReturn(Optional.of(session));
        when(apiUtils.getUserIdFromAuthentication()).thenReturn(999);

        assertThrows(InvalidDataException.class, () -> service.getActiveSession(1));
    }

    @Test
    void getActiveSession_withPolyline_returnsSessionWithRoute() {
        NavigationSession session = new NavigationSession();
        session.setId(5);
        session.setStatus(NavigationSessionStatus.ACTIVE);
        session.setDeliveryTask(task);
        session.setEncodedPolyline("BFoz5xJ67i1B1B7PzIhaxL7Y");
        when(sessionRepository.findByDeliveryTaskIdAndStatus(1, NavigationSessionStatus.ACTIVE))
                .thenReturn(Optional.of(session));
        when(apiUtils.getUserIdFromAuthentication()).thenReturn(10);

        DelivraResponse<NavigationSessionDTO> response = service.getActiveSession(1);

        assertTrue(response.isSuccess());
        assertNotNull(response.getPayload().getRoute());
        assertEquals("BFoz5xJ67i1B1B7PzIhaxL7Y", response.getPayload().getRoute().getPolyline());
    }
}