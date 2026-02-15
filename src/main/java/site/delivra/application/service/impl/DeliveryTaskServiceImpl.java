package site.delivra.application.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.mapper.DeliveryTaskMapper;
import site.delivra.application.model.constants.ApiErrorMessage;
import site.delivra.application.model.dto.DeliveryTaskDTO;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.request.task.NewDeliveryTaskRequest;
import site.delivra.application.model.request.task.SearchDeliveryTaskRequest;
import site.delivra.application.model.request.task.UpdateDeliveryTaskRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;
import site.delivra.application.model.entities.User;
import site.delivra.application.repository.DeliveryTaskRepository;
import site.delivra.application.repository.UserRepository;
import site.delivra.application.service.DeliveryTaskService;
import site.delivra.application.service.model.DelivraServiceUserRole;

@RequiredArgsConstructor
@Service
public class DeliveryTaskServiceImpl implements DeliveryTaskService {

    private final DeliveryTaskRepository deliveryTaskRepository;
    private final UserRepository userRepository;
    private final DeliveryTaskMapper taskMapper;


    @Override
    public DelivraResponse<DeliveryTaskDTO> getById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (id < 0) {
            throw new IllegalArgumentException("Id should be positive");
        }

        DeliveryTask task = deliveryTaskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.DELIVERY_NOT_FOUND_BY_ID.getMessage(id)));

        DeliveryTaskDTO tskDto = taskMapper.toDto(task);
        return DelivraResponse.createSuccessful(tskDto);
    }

    @Override
    public DelivraResponse<DeliveryTaskDTO> createDeliveryTask(NewDeliveryTaskRequest newDeliveryTaskRequest) {
        if (newDeliveryTaskRequest == null) {
            throw new IllegalArgumentException("DeliveryTask cannot be null");
        }

        User driver = userRepository.findByIdAndDeletedFalse(newDeliveryTaskRequest.getDriverId())
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(newDeliveryTaskRequest.getDriverId())));

        boolean isDriver = driver.getRoles().stream()
                .anyMatch(role -> role.getUserSystemRole() == DelivraServiceUserRole.DRIVER);
        if (!isDriver) {
            throw new IllegalArgumentException("User with ID: " + driver.getId() + " is not a driver");
        }

        DeliveryTask task = taskMapper.createDeliveryTask(newDeliveryTaskRequest);
        task.setUser(driver);

        DeliveryTask saved = deliveryTaskRepository.save(task);
        return DelivraResponse.createSuccessful(taskMapper.toDto(saved));
    }

    @Override
    public DelivraResponse<DeliveryTaskDTO> updateDeliveryTaskById(Integer deliveryTaskId, UpdateDeliveryTaskRequest updateDeliveryTaskRequest) {
        DeliveryTask deliveryTask = deliveryTaskRepository.findByIdAndDeletedFalse(deliveryTaskId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.DELIVERY_NOT_FOUND_BY_ID.getMessage(deliveryTaskId)));

        taskMapper.updateDeliveryTask(updateDeliveryTaskRequest, deliveryTask);
        DeliveryTask updated = deliveryTaskRepository.save(deliveryTask);
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
        return null;

    }

    @Override
    public DelivraResponse<PaginationResponse<DeliveryTaskDTO>> searchDeliveryTasks(SearchDeliveryTaskRequest searchRequest, Pageable pageable) {
        return null;
    }
}
