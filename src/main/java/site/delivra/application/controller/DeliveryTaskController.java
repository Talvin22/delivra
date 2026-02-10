package site.delivra.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.delivra.application.model.constants.ApiLogMassage;
import site.delivra.application.model.dto.DeliveryTaskDTO;
import site.delivra.application.model.request.task.NewDeliveryTaskRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.service.DeliveryTaskService;
import site.delivra.application.utils.ApiUtils;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class DeliveryTaskController {

    private final DeliveryTaskService deliveryTaskService;

    @GetMapping("/{id}")
    public ResponseEntity<DelivraResponse<DeliveryTaskDTO>> getById(@PathVariable(name = "id") Integer id) {
        log.trace(ApiLogMassage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<DeliveryTaskDTO> byId = deliveryTaskService.getById(id);

        return ResponseEntity.ok(byId);
    }

    @PostMapping("/create")
    public ResponseEntity<DelivraResponse<DeliveryTaskDTO>> createTask(@RequestBody NewDeliveryTaskRequest deliveryTaskRequest) {

        log.trace(ApiLogMassage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<DeliveryTaskDTO> deliveryTask = deliveryTaskService.createDeliveryTask(deliveryTaskRequest);

        return ResponseEntity.ok(deliveryTask);
    }
}
