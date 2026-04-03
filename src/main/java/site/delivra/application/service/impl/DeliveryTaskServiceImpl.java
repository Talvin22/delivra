package site.delivra.application.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import site.delivra.application.exception.InvalidDataException;
import site.delivra.application.exception.NotDriverException;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.mapper.DeliveryTaskMapper;
import site.delivra.application.model.constants.ApiErrorMessage;
import site.delivra.application.model.dto.DeliveryTaskDTO;
import site.delivra.application.model.dto.RouteDTO;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.request.task.NewDeliveryTaskRequest;
import site.delivra.application.model.request.task.RouteRequest;
import site.delivra.application.model.request.task.SearchDeliveryTaskRequest;
import site.delivra.application.model.request.task.UpdateDeliveryTaskRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;
import site.delivra.application.model.enums.DeliveryTaskStatus;

import java.time.LocalDateTime;
import site.delivra.application.model.entities.User;
import site.delivra.application.repository.DeliveryTaskRepository;
import site.delivra.application.repository.UserRepository;
import site.delivra.application.repository.criteria.DeliveryTaskSearchCriteria;
import site.delivra.application.service.DeliveryTaskService;
import site.delivra.application.service.EmailService;
import site.delivra.application.service.HereApiService;
import site.delivra.application.service.model.DelivraServiceUserRole;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeliveryTaskServiceImpl implements DeliveryTaskService {

    private final DeliveryTaskRepository deliveryTaskRepository;
    private final UserRepository userRepository;
    private final DeliveryTaskMapper taskMapper;
    private final HereApiService hereApiService;
    private final EmailService emailService;


    @Override
    public DelivraResponse<DeliveryTaskDTO> getById(Integer id) {
        DeliveryTask task = deliveryTaskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.DELIVERY_NOT_FOUND_BY_ID.getMessage(id)));

        DeliveryTaskDTO tskDto = taskMapper.toDto(task);
        return DelivraResponse.createSuccessful(tskDto);
    }

    @Override
    public DelivraResponse<DeliveryTaskDTO> createDeliveryTask(NewDeliveryTaskRequest newDeliveryTaskRequest) {
        DeliveryTask task = taskMapper.createDeliveryTask(newDeliveryTaskRequest);

        if (newDeliveryTaskRequest.getDriverId() != null) {
            User driver = userRepository.findByIdAndDeletedFalse(newDeliveryTaskRequest.getDriverId())
                    .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(newDeliveryTaskRequest.getDriverId())));
            boolean isDriver = driver.getRoles().stream()
                    .anyMatch(role -> role.getUserSystemRole() == DelivraServiceUserRole.DRIVER);
            if (!isDriver) {
                throw new NotDriverException(ApiErrorMessage.USER_NOT_DRIVER.getMessage(driver.getId()));
            }
            task.setUser(driver);
        }

        if (task.getLatitude() == null || task.getLongitude() == null) {
            try {
                HereApiService.GeocodingResult result = hereApiService.geocodeAddress(task.getAddress());
                task.setLatitude(result.latitude());
                task.setLongitude(result.longitude());
            } catch (Exception e) {
                log.warn("Geocoding failed for address '{}', saving task without coordinates: {}",
                        task.getAddress(), e.getMessage(), e);
            }
        }

        DeliveryTask saved = deliveryTaskRepository.save(task);

        if (task.getUser() != null) {
            emailService.sendTaskAssignedNotification(saved, task.getUser());
        }

        return DelivraResponse.createSuccessful(taskMapper.toDto(saved));
    }

    @Override
    public DelivraResponse<DeliveryTaskDTO> updateDeliveryTaskById(Integer deliveryTaskId, UpdateDeliveryTaskRequest updateDeliveryTaskRequest) {
        DeliveryTask deliveryTask = deliveryTaskRepository.findByIdAndDeletedFalse(deliveryTaskId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.DELIVERY_NOT_FOUND_BY_ID.getMessage(deliveryTaskId)));

        taskMapper.updateDeliveryTask(updateDeliveryTaskRequest, deliveryTask);

        if (updateDeliveryTaskRequest.getStatus() != null) {
            if (updateDeliveryTaskRequest.getStatus() == DeliveryTaskStatus.IN_PROGRESS
                    && deliveryTask.getStartTime() == null) {
                deliveryTask.setStartTime(LocalDateTime.now());
            }
            if ((updateDeliveryTaskRequest.getStatus() == DeliveryTaskStatus.COMPLETED
                    || updateDeliveryTaskRequest.getStatus() == DeliveryTaskStatus.CANCELED)
                    && deliveryTask.getEndTime() == null) {
                deliveryTask.setEndTime(LocalDateTime.now());
            }
        }

        User assignedDriver = null;
        if (updateDeliveryTaskRequest.getDriverId() != null) {
            User driver = userRepository.findByIdAndDeletedFalse(updateDeliveryTaskRequest.getDriverId())
                    .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(updateDeliveryTaskRequest.getDriverId())));
            boolean isDriver = driver.getRoles().stream()
                    .anyMatch(role -> role.getUserSystemRole() == DelivraServiceUserRole.DRIVER);
            if (!isDriver) {
                throw new NotDriverException(ApiErrorMessage.USER_NOT_DRIVER.getMessage(driver.getId()));
            }
            deliveryTask.setUser(driver);
            assignedDriver = driver;
        }

        DeliveryTask updated = deliveryTaskRepository.save(deliveryTask);

        if (assignedDriver != null) {
            emailService.sendTaskAssignedNotification(updated, assignedDriver);
        } else if (updateDeliveryTaskRequest.getStatus() != null && deliveryTask.getUser() != null) {
            Integer driverId = deliveryTask.getUser().getId();
            userRepository.findByIdAndDeletedFalse(driverId)
                    .ifPresent(driver -> emailService.sendTaskStatusChangedNotification(updated, driver));
        }

        return DelivraResponse.createSuccessful(taskMapper.toDto(updated));

    }

    @Override
    public void softDeleteUserDeliveryTask(@NotNull Integer id) {
        DeliveryTask deliveryTask = deliveryTaskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.DELIVERY_NOT_FOUND_BY_ID.getMessage(id)));

        deliveryTask.setDeleted(true);
        deliveryTaskRepository.save(deliveryTask);
    }

    @Override
    public DelivraResponse<PaginationResponse<DeliveryTaskDTO>> findAllDeliveryTasks(Pageable pageable) {
        Page<DeliveryTaskDTO> all = deliveryTaskRepository.findAllByDeletedFalse(pageable)
                .map(taskMapper::toDto);

        return DelivraResponse.createSuccessful(PaginationResponse.<DeliveryTaskDTO>builder()
                .content(all.getContent())
                .pagination(PaginationResponse.Pagination.builder()
                        .total(all.getTotalElements())
                        .limit(all.getSize())
                        .page(all.getNumber() + 1)
                        .pages(all.getTotalPages())
                        .build())
                .build());
    }

    @Override
    public DelivraResponse<PaginationResponse<DeliveryTaskDTO>> searchDeliveryTasks(SearchDeliveryTaskRequest searchRequest, Pageable pageable) {
        Specification<DeliveryTask> specification = new DeliveryTaskSearchCriteria(searchRequest);

        Page<DeliveryTaskDTO> all = deliveryTaskRepository.findAll(specification, pageable)
                .map(taskMapper::toDto);

        return DelivraResponse.createSuccessful(PaginationResponse.<DeliveryTaskDTO>builder()
                .content(all.getContent())
                .pagination(PaginationResponse.Pagination.builder()
                        .total(all.getTotalElements())
                        .limit(all.getSize())
                        .page(all.getNumber() + 1)
                        .pages(all.getTotalPages())
                        .build())
                .build());
    }

    @Override
    public DelivraResponse<RouteDTO> getRouteForTask(RouteRequest routeRequest) {
        DeliveryTask task = deliveryTaskRepository.findByIdAndDeletedFalse(routeRequest.getTaskId())
                .orElseThrow(() -> new NotFoundException(
                        ApiErrorMessage.DELIVERY_NOT_FOUND_BY_ID.getMessage(routeRequest.getTaskId())));

        if (task.getLatitude() == null || task.getLongitude() == null) {
            throw new InvalidDataException(
                    ApiErrorMessage.TASK_MISSING_COORDINATES.getMessage(routeRequest.getTaskId()));
        }

        RouteDTO route = hereApiService.calculateTruckRoute(
                routeRequest.getOriginLatitude(),
                routeRequest.getOriginLongitude(),
                task.getLatitude(),
                task.getLongitude(),
                routeRequest.getGrossWeight(),
                routeRequest.getHeight(),
                routeRequest.getWidth(),
                routeRequest.getLength()
        );

        return DelivraResponse.createSuccessful(route);
    }
}
