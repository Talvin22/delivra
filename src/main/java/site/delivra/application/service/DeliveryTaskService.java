package site.delivra.application.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import site.delivra.application.model.dto.DeliveryTaskDTO;
import site.delivra.application.model.request.task.NewDeliveryTaskRequest;
import site.delivra.application.model.request.task.RouteRequest;
import site.delivra.application.model.request.task.SearchDeliveryTaskRequest;
import site.delivra.application.model.request.task.UpdateDeliveryTaskRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;
import site.delivra.application.model.dto.RouteDTO;

public interface DeliveryTaskService {

    DelivraResponse<DeliveryTaskDTO> getById(@NotNull Integer id);

    DelivraResponse<DeliveryTaskDTO> createDeliveryTask(@NotNull NewDeliveryTaskRequest newDeliveryTaskRequest);

    DelivraResponse<DeliveryTaskDTO> updateDeliveryTaskById(@NotNull Integer deliveryTaskId, UpdateDeliveryTaskRequest updateDeliveryTaskRequest);

    void softDeleteUserDeliveryTask(@NotNull Integer id);

    DelivraResponse<PaginationResponse<DeliveryTaskDTO>> findAllDeliveryTasks(Pageable pageable);

    DelivraResponse<PaginationResponse<DeliveryTaskDTO>> searchDeliveryTasks(SearchDeliveryTaskRequest searchRequest, Pageable pageable);

    DelivraResponse<RouteDTO> getRouteForTask(RouteRequest routeRequest);
}
