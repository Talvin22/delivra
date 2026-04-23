package site.delivra.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import site.delivra.application.exception.InvalidDataException;
import site.delivra.application.exception.NotDriverException;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.mapper.DeliveryTaskMapper;
import site.delivra.application.model.dto.DeliveryTaskDTO;
import site.delivra.application.model.dto.RouteDTO;
import site.delivra.application.model.entities.Company;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.entities.Role;
import site.delivra.application.model.entities.User;
import site.delivra.application.model.enums.DeliveryTaskStatus;
import site.delivra.application.model.request.task.NewDeliveryTaskRequest;
import site.delivra.application.model.request.task.RouteRequest;
import site.delivra.application.model.request.task.SearchDeliveryTaskRequest;
import site.delivra.application.model.request.task.UpdateDeliveryTaskRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;
import site.delivra.application.repository.CompanyRepository;
import site.delivra.application.repository.DeliveryTaskRepository;
import site.delivra.application.repository.UserRepository;
import site.delivra.application.service.impl.DeliveryTaskServiceImpl;
import site.delivra.application.service.model.DelivraServiceUserRole;
import site.delivra.application.utils.ApiUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class DeliveryTaskServiceImplTest {

    @Mock private DeliveryTaskRepository deliveryTaskRepository;
    @Mock private UserRepository userRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private DeliveryTaskMapper taskMapper;
    @Mock private HereApiService hereApiService;
    @Mock private EmailService emailService;
    @Mock private ApiUtils apiUtils;

    @InjectMocks
    private DeliveryTaskServiceImpl service;

    private User driver;
    private User nonDriver;
    private DeliveryTask task;

    @BeforeEach
    void setUp() {
        Role driverRole = new Role();
        driverRole.setName("DRIVER");
        driverRole.setUserSystemRole(DelivraServiceUserRole.DRIVER);

        Role dispatcherRole = new Role();
        dispatcherRole.setName("DISPATCHER");
        dispatcherRole.setUserSystemRole(DelivraServiceUserRole.DISPATCHER);

        driver = new User();
        driver.setId(10);
        driver.setUsername("drv");
        driver.setRoles(Set.of(driverRole));
        driver.setDeleted(false);

        nonDriver = new User();
        nonDriver.setId(11);
        nonDriver.setUsername("dsp");
        nonDriver.setRoles(Set.of(dispatcherRole));
        nonDriver.setDeleted(false);

        task = new DeliveryTask();
        task.setId(1);
        task.setAddress("Addr");
        task.setStatus(DeliveryTaskStatus.PENDING);
        task.setLatitude(55.75);
        task.setLongitude(37.62);
        task.setDeleted(false);
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(99));
    }

    @Test
    void getById_found_returnsDto() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        DeliveryTaskDTO dto = new DeliveryTaskDTO();
        dto.setId(1);
        when(taskMapper.toDto(task)).thenReturn(dto);

        DelivraResponse<DeliveryTaskDTO> response = service.getById(1);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getPayload().getId());
    }

    @Test
    void createDeliveryTask_withDriver_isDriver_assignsAndNotifies() {
        NewDeliveryTaskRequest req = new NewDeliveryTaskRequest();
        req.setDriverId(10);
        req.setAddress("Some addr");
        req.setLatitude(55.75);
        req.setLongitude(37.62);

        DeliveryTask created = new DeliveryTask();
        created.setAddress("Some addr");
        created.setLatitude(55.75);
        created.setLongitude(37.62);
        when(taskMapper.createDeliveryTask(req)).thenReturn(created);
        when(userRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(driver));
        when(apiUtils.getCompanyIdFromAuthentication()).thenReturn(null);
        when(deliveryTaskRepository.save(any(DeliveryTask.class)))
                .thenAnswer(inv -> {
                    DeliveryTask t = inv.getArgument(0);
                    t.setId(100);
                    return t;
                });
        DeliveryTaskDTO dto = new DeliveryTaskDTO();
        dto.setId(100);
        when(taskMapper.toDto(any(DeliveryTask.class))).thenReturn(dto);

        DelivraResponse<DeliveryTaskDTO> response = service.createDeliveryTask(req);

        assertTrue(response.isSuccess());
        assertEquals(driver, created.getUser());
        verify(emailService).sendTaskAssignedNotification(any(DeliveryTask.class), eq(driver));
    }

    @Test
    void createDeliveryTask_withDriver_notDriver_throwsNotDriverException() {
        NewDeliveryTaskRequest req = new NewDeliveryTaskRequest();
        req.setDriverId(11);
        req.setAddress("Addr");

        DeliveryTask created = new DeliveryTask();
        created.setAddress("Addr");
        when(taskMapper.createDeliveryTask(req)).thenReturn(created);
        when(userRepository.findByIdAndDeletedFalse(11)).thenReturn(Optional.of(nonDriver));

        assertThrows(NotDriverException.class, () -> service.createDeliveryTask(req));
    }

    @Test
    void createDeliveryTask_driverNotFound_throwsNotFoundException() {
        NewDeliveryTaskRequest req = new NewDeliveryTaskRequest();
        req.setDriverId(999);
        req.setAddress("Addr");

        DeliveryTask created = new DeliveryTask();
        created.setAddress("Addr");
        when(taskMapper.createDeliveryTask(req)).thenReturn(created);
        when(userRepository.findByIdAndDeletedFalse(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createDeliveryTask(req));
    }

    @Test
    void createDeliveryTask_noCoordinates_geocodesAddress() {
        NewDeliveryTaskRequest req = new NewDeliveryTaskRequest();
        req.setAddress("Addr without coords");

        DeliveryTask created = new DeliveryTask();
        created.setAddress("Addr without coords");
        when(taskMapper.createDeliveryTask(req)).thenReturn(created);
        when(hereApiService.geocodeAddress("Addr without coords"))
                .thenReturn(new HereApiService.GeocodingResult(55.80, 37.70, "resolved"));
        when(apiUtils.getCompanyIdFromAuthentication()).thenReturn(null);
        when(deliveryTaskRepository.save(any(DeliveryTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(DeliveryTask.class))).thenReturn(new DeliveryTaskDTO());

        service.createDeliveryTask(req);

        assertEquals(55.80, created.getLatitude());
        assertEquals(37.70, created.getLongitude());
    }

    @Test
    void createDeliveryTask_geocodingFails_savesTaskWithoutCoordinates() {
        NewDeliveryTaskRequest req = new NewDeliveryTaskRequest();
        req.setAddress("bad addr");

        DeliveryTask created = new DeliveryTask();
        created.setAddress("bad addr");
        when(taskMapper.createDeliveryTask(req)).thenReturn(created);
        when(hereApiService.geocodeAddress("bad addr")).thenThrow(new RuntimeException("fail"));
        when(apiUtils.getCompanyIdFromAuthentication()).thenReturn(null);
        when(deliveryTaskRepository.save(any(DeliveryTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(DeliveryTask.class))).thenReturn(new DeliveryTaskDTO());

        DelivraResponse<DeliveryTaskDTO> response = service.createDeliveryTask(req);

        assertTrue(response.isSuccess());
        assertTrue(created.getLatitude() == null);
        assertTrue(created.getLongitude() == null);
    }

    @Test
    void createDeliveryTask_withCompany_assignsCompany() {
        NewDeliveryTaskRequest req = new NewDeliveryTaskRequest();
        req.setAddress("Addr");
        req.setLatitude(55.75);
        req.setLongitude(37.62);

        DeliveryTask created = new DeliveryTask();
        created.setAddress("Addr");
        created.setLatitude(55.75);
        created.setLongitude(37.62);
        when(taskMapper.createDeliveryTask(req)).thenReturn(created);

        Company company = new Company();
        company.setId(5);
        when(apiUtils.getCompanyIdFromAuthentication()).thenReturn(5);
        when(companyRepository.findByIdAndDeletedFalse(5)).thenReturn(Optional.of(company));
        when(deliveryTaskRepository.save(any(DeliveryTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(DeliveryTask.class))).thenReturn(new DeliveryTaskDTO());

        service.createDeliveryTask(req);

        assertEquals(company, created.getCompany());
    }

    @Test
    void updateDeliveryTaskById_notFound_throwsNotFoundException() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(99)).thenReturn(Optional.empty());
        UpdateDeliveryTaskRequest req = new UpdateDeliveryTaskRequest();

        assertThrows(NotFoundException.class, () -> service.updateDeliveryTaskById(99, req));
    }

    @Test
    void updateDeliveryTaskById_setsInProgressAndStartTime() {
        UpdateDeliveryTaskRequest req = new UpdateDeliveryTaskRequest();
        req.setStatus(DeliveryTaskStatus.IN_PROGRESS);

        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(deliveryTaskRepository.save(any(DeliveryTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(DeliveryTask.class))).thenReturn(new DeliveryTaskDTO());

        service.updateDeliveryTaskById(1, req);

        assertNotNull(task.getStartTime());
    }

    @Test
    void updateDeliveryTaskById_setsCompletedAndEndTime() {
        task.setUser(driver);
        task.setStartTime(java.time.LocalDateTime.now().minusHours(1));
        UpdateDeliveryTaskRequest req = new UpdateDeliveryTaskRequest();
        req.setStatus(DeliveryTaskStatus.COMPLETED);

        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(driver));
        when(deliveryTaskRepository.save(any(DeliveryTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(DeliveryTask.class))).thenReturn(new DeliveryTaskDTO());

        service.updateDeliveryTaskById(1, req);

        assertNotNull(task.getEndTime());
        verify(emailService).sendTaskStatusChangedNotification(any(DeliveryTask.class), eq(driver));
    }

    @Test
    void updateDeliveryTaskById_reassignDriver_verifiesIsDriverAndNotifies() {
        UpdateDeliveryTaskRequest req = new UpdateDeliveryTaskRequest();
        req.setDriverId(10);

        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(driver));
        when(deliveryTaskRepository.save(any(DeliveryTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(DeliveryTask.class))).thenReturn(new DeliveryTaskDTO());

        service.updateDeliveryTaskById(1, req);

        assertEquals(driver, task.getUser());
        verify(emailService).sendTaskAssignedNotification(any(DeliveryTask.class), eq(driver));
    }

    @Test
    void updateDeliveryTaskById_reassignToNonDriver_throwsNotDriverException() {
        UpdateDeliveryTaskRequest req = new UpdateDeliveryTaskRequest();
        req.setDriverId(11);

        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(11)).thenReturn(Optional.of(nonDriver));

        assertThrows(NotDriverException.class, () -> service.updateDeliveryTaskById(1, req));
    }

    @Test
    void softDeleteUserDeliveryTask_notFound_throwsNotFoundException() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.softDeleteUserDeliveryTask(99));
    }

    @Test
    void softDeleteUserDeliveryTask_marksTaskDeleted() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));

        service.softDeleteUserDeliveryTask(1);

        assertTrue(task.isDeleted());
        verify(deliveryTaskRepository).save(task);
    }

    @Test
    void findAllDeliveryTasks_noCompany_usesPlainFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DeliveryTask> page = new PageImpl<>(List.of(task), pageable, 1);
        when(apiUtils.getCompanyIdFromAuthentication()).thenReturn(null);
        when(deliveryTaskRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
        when(taskMapper.toDto(task)).thenReturn(new DeliveryTaskDTO());

        DelivraResponse<PaginationResponse<DeliveryTaskDTO>> response = service.findAllDeliveryTasks(pageable);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getPayload().getContent().size());
        verify(deliveryTaskRepository, never()).findAllByDeletedFalseAndCompany_Id(any(), any());
    }

    @Test
    void findAllDeliveryTasks_withCompany_filtersByCompanyId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DeliveryTask> page = new PageImpl<>(List.of(task), pageable, 1);
        when(apiUtils.getCompanyIdFromAuthentication()).thenReturn(5);
        when(deliveryTaskRepository.findAllByDeletedFalseAndCompany_Id(5, pageable)).thenReturn(page);
        when(taskMapper.toDto(task)).thenReturn(new DeliveryTaskDTO());

        service.findAllDeliveryTasks(pageable);

        verify(deliveryTaskRepository).findAllByDeletedFalseAndCompany_Id(5, pageable);
    }

    @Test
    void searchDeliveryTasks_withCompany_addsCompanyFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DeliveryTask> page = new PageImpl<>(List.of(task), pageable, 1);
        when(apiUtils.getCompanyIdFromAuthentication()).thenReturn(5);
        when(deliveryTaskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(taskMapper.toDto(task)).thenReturn(new DeliveryTaskDTO());

        DelivraResponse<PaginationResponse<DeliveryTaskDTO>> response =
                service.searchDeliveryTasks(new SearchDeliveryTaskRequest(), pageable);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getPayload().getContent().size());
    }

    @Test
    void getRouteForTask_taskNotFound_throwsNotFoundException() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(99)).thenReturn(Optional.empty());
        RouteRequest req = new RouteRequest();
        req.setTaskId(99);
        req.setOriginLatitude(55.0);
        req.setOriginLongitude(37.0);

        assertThrows(NotFoundException.class, () -> service.getRouteForTask(req));
    }

    @Test
    void getRouteForTask_missingCoordinates_throwsInvalidDataException() {
        task.setLatitude(null);
        task.setLongitude(null);
        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        RouteRequest req = new RouteRequest();
        req.setTaskId(1);
        req.setOriginLatitude(55.0);
        req.setOriginLongitude(37.0);

        assertThrows(InvalidDataException.class, () -> service.getRouteForTask(req));
    }

    @Test
    void getRouteForTask_valid_returnsRoute() {
        when(deliveryTaskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        RouteDTO route = RouteDTO.builder().polyline("abc").distanceInMeters(1000).build();
        when(hereApiService.calculateTruckRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                any(), any(), any(), any())).thenReturn(route);

        RouteRequest req = new RouteRequest();
        req.setTaskId(1);
        req.setOriginLatitude(55.0);
        req.setOriginLongitude(37.0);
        req.setGrossWeight(5000);

        DelivraResponse<RouteDTO> response = service.getRouteForTask(req);

        assertTrue(response.isSuccess());
        assertEquals("abc", response.getPayload().getPolyline());
    }
}