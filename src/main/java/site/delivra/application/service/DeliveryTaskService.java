package site.delivra.application.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import site.delivra.application.model.dto.DeliveryTaskDTO;
import site.delivra.application.model.request.task.NewDeliveryTaskRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;

public interface DeliveryTaskService {

    DelivraResponse<DeliveryTaskDTO> getById(@NotNull Integer id);

    DelivraResponse<DeliveryTaskDTO> createDeliveryTask(@NotNull NewDeliveryTaskRequest newDeliveryTaskRequest);

    DelivraResponse<DeliveryTaskDTO> updateDeliveryTaskById(@NotNull Integer userId, UpdateDeliveryTaskRequest updateDeliveryTaskRequest);

    void softDeleteUser(@NotNull Integer id);

    DelivraResponse<PaginationResponse<DeliveryTaskDTO>> findAllDeliveryTasks(Pageable pageable);

    DelivraResponse<PaginationResponse<DeliveryTaskDTO>> searchDeliveryTasks(searchDeliveryTaskRequest searchRequest, Pageable pageable);
}
