package site.delivra.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.delivra.application.model.constants.ApiLogMessage;
import site.delivra.application.model.dto.DeliveryTaskDTO;
import jakarta.validation.Valid;
import site.delivra.application.model.dto.RouteDTO;
import site.delivra.application.model.request.task.NewDeliveryTaskRequest;
import site.delivra.application.model.request.task.RouteRequest;
import site.delivra.application.model.request.task.SearchDeliveryTaskRequest;
import site.delivra.application.model.request.task.UpdateDeliveryTaskRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;
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
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<DeliveryTaskDTO> byId = deliveryTaskService.getById(id);

        return ResponseEntity.ok(byId);
    }

    @PostMapping("/create")
    public ResponseEntity<DelivraResponse<DeliveryTaskDTO>> createTask(@RequestBody NewDeliveryTaskRequest deliveryTaskRequest) {

        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<DeliveryTaskDTO> deliveryTask = deliveryTaskService.createDeliveryTask(deliveryTaskRequest);

        return ResponseEntity.ok(deliveryTask);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<DelivraResponse<DeliveryTaskDTO>> updateTask
            (@PathVariable(name = "id") Integer id,
             @RequestBody UpdateDeliveryTaskRequest deliveryTaskRequest) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<DeliveryTaskDTO> deliveryTask = deliveryTaskService.updateDeliveryTaskById(id, deliveryTaskRequest);
        return ResponseEntity.ok(deliveryTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DelivraResponse<DeliveryTaskDTO>> deleteTask(@PathVariable(name = "id") Integer id) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        deliveryTaskService.softDeleteUserDeliveryTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<DelivraResponse<PaginationResponse<DeliveryTaskDTO>>> getAllTasks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {

        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());
        Pageable pageable = PageRequest.of(page, limit);
        DelivraResponse<PaginationResponse<DeliveryTaskDTO>> allDeliveryTasks = deliveryTaskService.findAllDeliveryTasks(pageable);
        return ResponseEntity.ok(allDeliveryTasks);

    }

    @PostMapping("/search")
    public ResponseEntity<DelivraResponse<PaginationResponse<DeliveryTaskDTO>>> searchTasks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestBody SearchDeliveryTaskRequest searchDeliveryTaskRequest) {

        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());
        Pageable pageable = PageRequest.of(page, limit);
        DelivraResponse<PaginationResponse<DeliveryTaskDTO>> allDeliveryTasks = deliveryTaskService.searchDeliveryTasks(searchDeliveryTaskRequest, pageable);
        return ResponseEntity.ok(allDeliveryTasks);

    }

    @PostMapping("/{id}/route")
    public ResponseEntity<DelivraResponse<RouteDTO>> getRoute(
            @PathVariable(name = "id") Integer id,
            @RequestBody @Valid RouteRequest routeRequest) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        routeRequest.setTaskId(id);
        DelivraResponse<RouteDTO> route = deliveryTaskService.getRouteForTask(routeRequest);
        return ResponseEntity.ok(route);
    }
}
