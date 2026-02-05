package site.delivra.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.delivra.application.model.dto.DeliveryTaskDTO;
import site.delivra.application.model.request.task.NewDeliveryTaskRequest;
import site.delivra.application.model.request.task.UpdateDeliveryTaskRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;
import site.delivra.application.repository.DeliveryTaskRepository;
import site.delivra.application.service.DeliveryTaskService;

@RequiredArgsConstructor
@Service
public class DeliveryTaskServiceImpl implements DeliveryTaskService {

    private final DeliveryTaskRepository deliveryTaskRepository;


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
    public void softDeleteUserDeliveryTask(Integer id) {

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
