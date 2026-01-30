package site.delivra.application.service.impl;

import org.springframework.data.domain.Pageable;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;
import site.delivra.application.service.DeliveryTaskService;

public class DeliveryTaskServiceImpl implements DeliveryTaskService {
    @Override
    public DelivraResponse<DeliveryTaskDTO> getById(Integer id) {
        return null;
    }

    @Override
    public DelivraResponse<DeliveryTaskDTO> createDeliveryTask(NewDeliveryTaskRequest newDeliveryTaskRequest) {
        return null;
    }

    @Override
    public DelivraResponse<DeliveryTaskDTO> updateDeliveryTaskById(Integer userId, UpdateDeliveryTaskRequest updateDeliveryTaskRequest) {
        return null;
    }

    @Override
    public void softDeleteUser(Integer id) {

    }

    @Override
    public DelivraResponse<PaginationResponse<DeliveryTaskDTO>> findAllDeliveryTasks(Pageable pageable) {
        return null;
    }

    @Override
    public DelivraResponse<PaginationResponse<DeliveryTaskDTO>> searchDeliveryTasks(searchDeliveryTaskRequest searchRequest, Pageable pageable) {
        return null;
    }
}
